package snipy

import java.lang.ref.Cleaner
import java.util.concurrent.{ConcurrentHashMap, ConcurrentLinkedQueue}

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.unsafe.*

import snipy.CApi.*

/** Entry point to the embedded CPython interpreter.
 *
 *  The interpreter is started lazily the first time any Python object is
 *  touched. Right after start-up the GIL is released, and every snipy
 *  operation acquires it for its own duration, so Python can be used from any
 *  thread.
 */
object Python {
  private final val PyGILStateUnlocked = 1
  private final val PyEvalInput = 258
  private final val PyFileInput = 257

  private lazy val initialized: Boolean = {
    if (Py_IsInitialized() == 0) {
      Py_InitializeEx(0)
      PyEval_SaveThread()
    }
    true
  }

  private def ensureInitialized(): Unit = if (!initialized) ()

  /** Runs `body` holding the GIL.
   *
   *  Every snipy operation already does this on its own; wrap a block of many
   *  operations in `withGil` to pay for the lock only once.
   */
  inline def withGil[T](inline body: T): T = {
    ensureInitialized()
    val state = PyGILState_Ensure()
    val outermost = state == PyGILStateUnlocked
    if (outermost) releasePending()
    try body
    finally {
      if (outermost) releasePending()
      PyGILState_Release(state)
    }
  }

  /** Imports a module, like Python's `import name`. */
  def module(name: String): Py = withGil {
    Zone(Py.steal(check(PyImport_ImportModule(toCString(name)))))
  }

  /** The `builtins` module: `Python.builtins.len(xs)`. */
  lazy val builtins: Py = Py.global(module("builtins"))

  /** Python's `None`. */
  lazy val None: Py = Py.global(eval("None"))

  /** The version string of the embedded interpreter, as in `sys.version`. */
  lazy val version: String = module("sys").version.as[String]

  private lazy val mainGlobals: Py = Py.global {
    withGil {
      val main = module("__main__")
      Py.borrow(PyModule_GetDict(main.ptr))
    }
  }

  /** Evaluates a Python expression in the `__main__` module and returns its value. */
  def eval(expression: String): Py = run(expression, PyEvalInput, mainGlobals)

  /** Evaluates a Python expression using `globals` (a `dict`) as its namespace. */
  def eval(expression: String, globals: Py): Py = run(expression, PyEvalInput, globals)

  /** Executes Python statements in the `__main__` module. */
  def exec(code: String): Unit = run(code, PyFileInput, mainGlobals).dispose()

  /** Executes Python statements using `globals` (a `dict`) as their namespace. */
  def exec(code: String, globals: Py): Unit = run(code, PyFileInput, globals).dispose()

  private def run(code: String, start: Int, globals: Py): Py = withGil {
    val g = globals.ptr
    val result = Zone(PyRun_String(toCString(code), start, g, g))
    keepAlive(globals)
    Py.steal(check(result))
  }

  // ---------------------------------------------------------------------------
  // Reference management
  //
  // Each Py owns one strong reference. A java.lang.ref.Cleaner notices when the
  // Py becomes unreachable and enqueues its pointer; the queue is drained, with
  // the GIL held, whenever a thread enters or leaves its outermost snipy
  // operation. The cleaner itself never touches Python: it may run inside the
  // GC or on a thread that doesn't hold the GIL.

  private final class Release(val ptr: PyPtr) extends Runnable {
    def run(): Unit = pending.add(this)
  }

  private val pending = new ConcurrentLinkedQueue[Release]()

  // Boehm supports weak references, but on Scala Native 0.5.12 registering many
  // of them from several threads deadlocks inside its finalizer notification.
  private val cleaner: Cleaner | Null =
    if (LinktimeInfo.isWeakReferenceSupported && !LinktimeInfo.gc.isBoehm) Cleaner.create(null)
    else null

  private[snipy] def track(owner: Py, ptr: PyPtr): Runnable | Cleaner.Cleanable = {
    if (Local.active) Local.add(owner)
    val release = new Release(ptr)
    val c = cleaner
    if (c != null) c.register(owner, release) else release
  }

  /** Releases an object now, or as soon as the GIL is free. */
  private[snipy] def release(handle: Runnable | Cleaner.Cleanable): Unit =
    handle match {
      case c: Cleaner.Cleanable => c.clean()
      case r: Runnable          => r.run()
    }

  private def releasePending(): Unit = {
    var r = pending.poll()
    while (r != null) {
      Py_DecRef(r.ptr)
      r = pending.poll()
    }
  }

  /** Keeps `x` reachable, and so its Python object alive, up to this call. */
  @noinline private[snipy] def keepAlive(x: Any): Unit = if (x == null) ()

  // ---------------------------------------------------------------------------
  // Errors

  /** Converts the pending Python exception into a [[PythonException]]. */
  private[snipy] def raise(): Nothing = {
    val exc = PyErr_GetRaisedException()
    if (exc == null)
      throw new IllegalStateException("A Python call failed without setting an exception")
    throw PythonException(Py.steal(exc))
  }

  /** Runs `f` on a new reference, releasing it afterwards. */
  private[snipy] inline def owning[T](ptr: PyPtr)(inline f: PyPtr => T): T =
    try f(ptr)
    finally Py_DecRef(ptr)

  @inline private[snipy] def check(ptr: PyPtr): PyPtr =
    if (ptr == null) raise() else ptr

  @inline private[snipy] def check(status: CInt): CInt =
    if (status == -1) raise() else status

  /** Raises if a Python error is pending; for C functions with ambiguous error values. */
  @inline private[snipy] def checkError(): Unit =
    if (PyErr_Occurred() != null) raise()

  // ---------------------------------------------------------------------------
  // Interned attribute names, shared by every attribute access and method call

  private val names = new ConcurrentHashMap[String, PyPtr]()

  private[snipy] def name(s: String): PyPtr = {
    val cached = names.get(s)
    if (cached != null) cached
    else {
      val interned = Zone(check(PyUnicode_InternFromString(toCString(s))))
      val previous = names.putIfAbsent(s, interned)
      if (previous == null) interned
      else {
        Py_DecRef(interned)
        previous
      }
    }
  }
}
