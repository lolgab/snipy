package snipy

import java.lang.ref.Cleaner

import scala.collection.mutable.ArrayBuffer
import scala.language.dynamics
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

import snipy.CApi.*
import snipy.Python.{check, keepAlive, withGil}

/** A reference to a Python object.
 *
 *  Attributes and methods are reached dynamically: `obj.attr`, `obj.method(a, b)`,
 *  `obj.method(a, key = b)`, `obj.attr = v`. Arguments are converted with
 *  [[AsPython]]; results are converted back to Scala with [[as]].
 *
 *  A `Py` owns a strong reference to its object. The reference is released
 *  when the `Py` is garbage collected, when [[dispose]] is called, or when the
 *  enclosing [[Py.local]] block ends.
 *
 *  Names defined here (`as`, `apply`, `dispose`, `iterator`, `bracketAccess`,
 *  ...) shadow Python attributes with the same name; reach those with
 *  `obj.selectDynamic("name")` or `obj.callMethod("name", args)`.
 */
final class Py private (private val raw: PyPtr) extends Dynamic {
  private var disposed = false
  private val handle: Runnable | Cleaner.Cleanable = Python.track(this, raw)

  /** The underlying `PyObject*`. Callers must keep this `Py` reachable while using it. */
  private[snipy] def ptr: PyPtr = {
    if (disposed) throw new IllegalStateException("This Python object has been disposed")
    raw
  }

  /** Releases the Python object now instead of waiting for the GC. Using this
   *  `Py` afterwards throws `IllegalStateException`. Idempotent.
   */
  def dispose(): Unit = if (!disposed) {
    disposed = true
    Python.release(handle)
    withGil(())
  }

  /** Whether [[dispose]] has been called. */
  def isDisposed: Boolean = disposed

  /** Converts to a Scala value. Throws [[PythonException]] if Python refuses the conversion. */
  def as[T](using conv: AsScala[T]): T = withGil {
    val result = conv.fromPython(ptr)
    keepAlive(this)
    result
  }

  /** Whether this is Python's `None`. */
  def isNone: Boolean = withGil(ptr == Python.None.ptr)

  // ---------------------------------------------------------------------------
  // Dynamic access

  def selectDynamic(name: String): Py = withGil {
    val result = PyObject_GetAttr(ptr, Python.name(name))
    keepAlive(this)
    Py.steal(check(result))
  }

  inline def updateDynamic[T](name: String)(value: T)(using conv: AsPython[T]): Unit =
    setAttr(name, Py.of(value))

  /** Python's `setattr(obj, name, value)`. */
  def setAttr(name: String, value: Py): Unit = withGil {
    check(PyObject_SetAttr(ptr, Python.name(name), value.ptr))
    keepAlive(this)
    keepAlive(value)
  }

  inline def applyDynamic(inline name: String)(inline args: Any*): Py =
    ${ Macros.applyDynamic('this, 'name, 'args) }

  inline def applyDynamicNamed(inline name: String)(inline args: (String, Any)*): Py =
    ${ Macros.applyDynamicNamed('this, 'name, 'args) }

  /** Calls this object: `f(a, b)` is Python's `f(a, b)`. */
  inline def apply(inline args: Any*): Py = ${ Macros.apply('this, 'args) }

  /** Calls this object with keyword arguments: `f.applyNamed("x" -> 1, "y" -> 2)`
   *  is Python's `f(x=1, y=2)`. Pairs with an empty name are positional.
   */
  inline def applyNamed(inline args: (String, Any)*): Py = ${ Macros.applyNamed('this, 'args) }

  /** Calls method `name` with already converted arguments. Also reaches
   *  methods shadowed by a member of `Py`: `df.callMethod("apply", Seq(f))`.
   */
  def callMethod(name: String, args: Seq[Py]): Py = withGil {
    val n = args.length
    val argv = stackalloc[PyPtr](n + 1)
    argv(0) = ptr
    var i = 0
    while (i < n) {
      argv(i + 1) = args(i).ptr
      i += 1
    }
    val result = PyObject_VectorcallMethod(Python.name(name), argv, (n + 1).toCSize, null)
    keepAlive(this)
    keepAlive(args)
    Py.steal(check(result))
  }

  /** Calls method `name` with already converted, possibly named, arguments. */
  def callMethodNamed(name: String, args: Seq[(String, Py)]): Py =
    Py.callNamed(args) { (argv, nargs, kwnames) =>
      argv(0) = ptr
      val result =
        PyObject_VectorcallMethod(Python.name(name), argv, (nargs + 1).toCSize, kwnames)
      keepAlive(this)
      result
    }

  /** Calls this object with already converted arguments. */
  def call(args: Seq[Py]): Py = withGil {
    val n = args.length
    val argv = stackalloc[PyPtr](n + 1)
    var i = 0
    while (i < n) {
      argv(i + 1) = args(i).ptr
      i += 1
    }
    val result = PyObject_Vectorcall(ptr, argv + 1, n.toCSize, null)
    keepAlive(this)
    keepAlive(args)
    Py.steal(check(result))
  }

  /** Calls this object with already converted, possibly named, arguments. */
  def callNamed(args: Seq[(String, Py)]): Py =
    Py.callNamed(args) { (argv, nargs, kwnames) =>
      val result = PyObject_Vectorcall(ptr, argv + 1, nargs.toCSize, kwnames)
      keepAlive(this)
      result
    }

  // ---------------------------------------------------------------------------
  // Subscripting

  /** Python's `obj[key]`. */
  inline def bracketAccess[K](key: K)(using conv: AsPython[K]): Py =
    binary(Py.of(key))(PyObject_GetItem)

  /** Python's `obj[key] = value`. */
  inline def bracketUpdate[K, V](key: K, value: V)(using AsPython[K], AsPython[V]): Unit =
    setItem(Py.of(key), Py.of(value))

  private def setItem(key: Py, value: Py): Unit = withGil {
    check(PyObject_SetItem(ptr, key.ptr, value.ptr))
    keepAlive(this)
    keepAlive(key)
    keepAlive(value)
  }

  /** Python's `del obj[key]`. */
  inline def bracketDelete[K](key: K)(using conv: AsPython[K]): Unit = delItem(Py.of(key))

  private def delItem(key: Py): Unit = withGil {
    check(PyObject_DelItem(ptr, key.ptr))
    keepAlive(this)
    keepAlive(key)
  }

  /** Iterates the object, like Python's `for x in obj`. */
  def iterator: Iterator[Py] = new Iterator[Py] {
    private val it = withGil {
      val result = PyObject_GetIter(ptr)
      keepAlive(Py.this)
      Py.steal(check(result))
    }
    private var nextItem: Py | Null = null
    private var done = false

    def hasNext: Boolean = {
      if (nextItem == null && !done) withGil {
        val item = PyIter_Next(it.ptr)
        keepAlive(it)
        if (item != null) nextItem = Py.steal(item)
        else {
          Python.checkError()
          done = true
          it.dispose()
        }
      }
      nextItem != null
    }

    def next(): Py = {
      if (!hasNext) throw new NoSuchElementException("Python iterator exhausted")
      val item = nextItem.nn
      nextItem = null
      item
    }
  }

  // ---------------------------------------------------------------------------
  // Operators

  inline def +[T](other: T)(using AsPython[T]): Py = binary(Py.of(other))(PyNumber_Add)
  inline def -[T](other: T)(using AsPython[T]): Py = binary(Py.of(other))(PyNumber_Subtract)
  inline def *[T](other: T)(using AsPython[T]): Py = binary(Py.of(other))(PyNumber_Multiply)
  inline def /[T](other: T)(using AsPython[T]): Py = binary(Py.of(other))(PyNumber_TrueDivide)
  inline def %[T](other: T)(using AsPython[T]): Py = binary(Py.of(other))(PyNumber_Remainder)
  inline def **[T](other: T)(using AsPython[T]): Py = power(Py.of(other))

  private def power(other: Py): Py = withGil {
    val result = PyNumber_Power(ptr, other.ptr, Python.None.ptr)
    keepAlive(this)
    keepAlive(other)
    Py.steal(check(result))
  }
  inline def <<[T](other: T)(using AsPython[T]): Py = binary(Py.of(other))(PyNumber_Lshift)
  inline def >>[T](other: T)(using AsPython[T]): Py = binary(Py.of(other))(PyNumber_Rshift)
  inline def &[T](other: T)(using AsPython[T]): Py = binary(Py.of(other))(PyNumber_And)
  inline def |[T](other: T)(using AsPython[T]): Py = binary(Py.of(other))(PyNumber_Or)
  inline def ^[T](other: T)(using AsPython[T]): Py = binary(Py.of(other))(PyNumber_Xor)

  /** Python's `a // b`. */
  inline def floorDiv[T](other: T)(using AsPython[T]): Py =
    binary(Py.of(other))(PyNumber_FloorDivide)

  /** Python's `a @ b`. */
  inline def matmul[T](other: T)(using AsPython[T]): Py =
    binary(Py.of(other))(PyNumber_MatrixMultiply)

  def unary_- : Py = unary(PyNumber_Negative)
  def unary_+ : Py = unary(PyNumber_Positive)
  def unary_~ : Py = unary(PyNumber_Invert)

  inline def <[T](other: T)(using AsPython[T]): Py = compare(Py.of(other), 0)
  inline def <=[T](other: T)(using AsPython[T]): Py = compare(Py.of(other), 1)
  inline def >[T](other: T)(using AsPython[T]): Py = compare(Py.of(other), 4)
  inline def >=[T](other: T)(using AsPython[T]): Py = compare(Py.of(other), 5)

  private def binary(other: Py)(f: (PyPtr, PyPtr) => PyPtr): Py = withGil {
    val result = f(ptr, other.ptr)
    keepAlive(this)
    keepAlive(other)
    Py.steal(check(result))
  }

  private def unary(f: PyPtr => PyPtr): Py = withGil {
    val result = f(ptr)
    keepAlive(this)
    Py.steal(check(result))
  }

  private def compare(other: Py, op: Int): Py = withGil {
    val result = PyObject_RichCompare(ptr, other.ptr, op)
    keepAlive(this)
    keepAlive(other)
    Py.steal(check(result))
  }

  // ---------------------------------------------------------------------------
  // java.lang.Object

  /** Python's `str(obj)`. */
  override def toString: String =
    if (disposed) "<disposed Python object>"
    else withGil(Py.steal(check(PyObject_Str(ptr))).as[String])

  /** Python's `repr(obj)`. */
  def repr: String = withGil(Py.steal(check(PyObject_Repr(ptr))).as[String])

  /** Python's `==`. */
  override def equals(other: Any): Boolean = other match {
    case that: Py =>
      (this eq that) || withGil {
        val result = PyObject_RichCompareBool(ptr, that.ptr, 2)
        keepAlive(this)
        keepAlive(that)
        check(result) == 1
      }
    case _ => false
  }

  /** Python's `hash(obj)`; identity-based for unhashable objects. */
  override def hashCode: Int = withGil {
    val h = PyObject_Hash(ptr)
    if (h == -1) {
      CApi.PyErr_Clear()
      raw.toLong.##
    } else h.toLong.##
  }
}

object Py {

  /** Wraps a new reference, taking ownership of it. */
  private[snipy] def steal(ptr: PyPtr): Py = new Py(ptr)

  /** Wraps a borrowed reference, taking a new reference to it. */
  private[snipy] def borrow(ptr: PyPtr): Py = {
    Py_IncRef(ptr)
    new Py(ptr)
  }

  /** Converts a Scala value to Python. */
  def apply[T](value: T)(using conv: AsPython[T]): Py = conv.asPython(value)

  /** Converts `value`, without wrapping again when it already is a `Py`. */
  private[snipy] inline def of[T](value: T)(using conv: AsPython[T]): Py =
    inline compiletime.erasedValue[T] match {
      case _: Py => value.asInstanceOf[Py]
      case _     => conv.asPython(value)
    }

  /** Builds a Python tuple: `Py.tuple(1, "a", 2.0)`. */
  inline def tuple(inline items: Any*): Py = ${ Macros.tuple('items) }

  /** Builds a Python list: `Py.list(1, "a", 2.0)`. */
  inline def list(inline items: Any*): Py = ${ Macros.list('items) }

  /** Builds a Python tuple from already converted values. */
  def tupleOf(items: Seq[Py]): Py = withGil {
    val t = check(PyTuple_New(items.length))
    var i = 0
    while (i < items.length) {
      val item = items(i).ptr
      Py_IncRef(item)
      PyTuple_SetItem(t, i, item)
      i += 1
    }
    keepAlive(items)
    steal(t)
  }

  /** Builds a Python list from already converted values. */
  def listOf(items: Seq[Py]): Py = withGil {
    val l = check(PyList_New(items.length))
    var i = 0
    while (i < items.length) {
      val item = items(i).ptr
      Py_IncRef(item)
      PyList_SetItem(l, i, item)
      i += 1
    }
    keepAlive(items)
    steal(l)
  }

  /** Builds a Python dict: `Py.dict("a" -> 1, "b" -> 2)`. */
  def dict[K, V](items: (K, V)*)(using AsPython[K], AsPython[V]): Py =
    AsPython[collection.Map[K, V]].asPython(collection.immutable.ListMap.from(items))

  /** Wraps a Scala function as a Python callable taking positional arguments. */
  def function(f: Seq[Py] => Py): Py = Callbacks.create(f)

  /** Runs `body` and then disposes every `Py` created on this thread while it ran,
   *  except the result itself when it is a `Py`.
   *
   *  Use it around loops that create many short-lived Python objects so they
   *  are released right away instead of when the GC runs.
   */
  def local[T](body: => T): T = {
    val arena = Local.push()
    var keep: Any = null
    try {
      val result = body
      keep = result
      result
    } catch {
      case e: PythonException =>
        keep = e.value
        throw e
    } finally Local.pop(arena, keep)
  }

  /** Runs `body` without registering the objects it creates in any enclosing
   *  [[local]] block. Use it for long-lived objects, such as a module stored
   *  in a `lazy val`, that may first be created inside a `local` block.
   */
  def global[T](body: => T): T = Local.suspended(body)

  private[snipy] def callNamed(args: Seq[(String, Py)])(
      call: (Ptr[PyPtr], Int, PyPtr) => PyPtr
  ): Py = withGil {
    val n = args.length
    val nkw = args.count(_._1.nonEmpty)
    val npos = n - nkw
    val argv = stackalloc[PyPtr](n + 1)
    val kwnames = if (nkw == 0) null else check(PyTuple_New(nkw))
    try {
      var i = 0
      while (i < n) {
        val (name, value) = args(i)
        if (i < npos) {
          if (name.nonEmpty)
            throw new IllegalArgumentException(
              "Positional arguments must come before keyword arguments"
            )
        } else {
          val key = Python.name(name)
          Py_IncRef(key)
          PyTuple_SetItem(kwnames, i - npos, key)
        }
        argv(i + 1) = value.ptr
        i += 1
      }
      val result = call(argv, npos, kwnames)
      keepAlive(args)
      steal(check(result))
    } finally if (kwnames != null) Py_DecRef(kwnames)
  }
}

/** Per-thread stack of [[Py.local]] arenas. */
private[snipy] object Local {
  @volatile private var depth = 0
  private val arenas = new ThreadLocal[ArrayBuffer[Py] | Null]

  final class Arena(val parent: ArrayBuffer[Py] | Null, val items: ArrayBuffer[Py])

  def active: Boolean = depth > 0

  def add(py: Py): Unit = {
    val current = arenas.get()
    if (current != null) current += py
  }

  def push(): Arena = {
    val arena = new Arena(arenas.get(), ArrayBuffer.empty)
    arenas.set(arena.items)
    synchronized(depth += 1)
    arena
  }

  def suspended[T](body: => T): T = {
    val current = arenas.get()
    if (current == null) body
    else {
      arenas.set(null)
      try body
      finally arenas.set(current)
    }
  }

  def pop(arena: Arena, keep: Any): Unit = {
    arenas.set(arena.parent)
    synchronized(depth -= 1)
    Python.withGil {
      arena.items.foreach { py =>
        if (py.asInstanceOf[AnyRef] ne keep.asInstanceOf[AnyRef]) py.dispose()
        else if (arena.parent != null) arena.parent.nn += py
      }
    }
  }
}
