package snipy

import scala.util.control.NonFatal
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdlib.malloc
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.{Intrinsics, fromRawPtr, toRawPtr}
import scala.scalanative.unsafe.*

import snipy.CApi.*
import snipy.Python.check

/** Scala functions exposed to Python as builtin functions.
 *
 *  Each function gets an id; Python sees a `builtin_function_or_method` whose
 *  `self` is a capsule holding that id. When Python frees the function the
 *  capsule's destructor drops the Scala closure.
 */
private[snipy] object Callbacks {
  private val functions = new ConcurrentHashMap[Long, Seq[Py] => Py]()
  private val ids = new AtomicLong(0)

  private val capsuleName: CString = c"snipy.function"

  // Python calls back into Scala from inside a @blocking extern call, where the
  // thread is "unmanaged" for the GC. Switch to managed while running Scala,
  // like Scala Native does when a blocking call returns.
  @alwaysinline private def enterManaged(): Unit =
    if (LinktimeInfo.isMultithreadingEnabled) {
      scalanative_GC_yield()
      scalanative_GC_set_mutator_thread_state(0)
    }

  @alwaysinline private def leaveManaged(): Unit =
    if (LinktimeInfo.isMultithreadingEnabled) scalanative_GC_set_mutator_thread_state(1)

  private val trampoline = CFuncPtr2.fromScalaFunction[PyPtr, PyPtr, PyPtr] { (self, args) =>
    enterManaged()
    try call(self, args)
    finally leaveManaged()
  }

  private def call(self: PyPtr, args: PyPtr): PyPtr =
    try {
      val id = Intrinsics.castRawPtrToLong(toRawPtr(PyCapsule_GetPointer(self, capsuleName)))
      val f = functions.get(id)
      val n = PyTuple_Size(args).toInt
      val params = Vector.tabulate(n)(i => Py.borrow(PyTuple_GetItem(args, i)))
      val result = f(params)
      val p = result.ptr
      Py_IncRef(p)
      Python.keepAlive(result)
      p
    } catch {
      case e: PythonException =>
        val v = e.value.ptr
        Py_IncRef(v)
        PyErr_SetRaisedException(v)
        null
      case NonFatal(e) =>
        val exc = Python.builtins.RuntimeError(s"${e.getClass.getName}: ${e.getMessage}")
        val v = exc.ptr
        Py_IncRef(v)
        PyErr_SetRaisedException(v)
        null
    }

  private val destructor = CFuncPtr1.fromScalaFunction[PyPtr, Unit] { capsule =>
    enterManaged()
    try {
      val id = Intrinsics.castRawPtrToLong(toRawPtr(PyCapsule_GetPointer(capsule, capsuleName)))
      functions.remove(id)
    } finally leaveManaged()
    ()
  }

  private val methodDef: Ptr[PyMethodDef] = {
    val d = malloc(sizeof[PyMethodDef]).asInstanceOf[Ptr[PyMethodDef]]
    d._1 = c"scala_function"
    d._2 = trampoline
    d._3 = 1 // METH_VARARGS
    d._4 = null
    d
  }

  def newReference(f: Seq[Py] => Py): PyPtr = {
    val id = ids.incrementAndGet()
    functions.put(id, f)
    val capsule =
      PyCapsule_New(fromRawPtr[Byte](Intrinsics.castLongToRawPtr(id)), capsuleName, destructor)
    if (capsule == null) {
      functions.remove(id)
      Python.raise()
    }
    try check(PyCFunction_NewEx(methodDef, capsule, null))
    finally Py_DecRef(capsule)
  }

  /** Number of functions still referenced from Python. */
  def liveCount: Int = functions.size

  def create(f: Seq[Py] => Py): Py = Python.withGil(Py.steal(newReference(f)))

  /** Converts argument `index` of a call expecting `arity` arguments. */
  def arg[T](args: Seq[Py], index: Int, arity: Int)(using conv: AsScala[T]): T = {
    if (args.length != arity)
      throw PythonException.typeError(s"expected $arity arguments, got ${args.length}")
    args(index).as[T]
  }
}
