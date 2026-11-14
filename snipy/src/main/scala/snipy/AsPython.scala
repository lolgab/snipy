package snipy

import scala.util.control.NonFatal
import java.nio.charset.StandardCharsets.UTF_8

import scala.collection.{Map as AnyMap, Set as AnySet}
import scala.scalanative.unsafe.*

import snipy.CApi.*
import snipy.Python.{check, withGil}

/** Converts Scala values of type `T` to Python objects.
 *
 *  Implement [[newReference]] to add a conversion for your own types.
 */
trait AsPython[-T] {

  /** Creates the Python object for `value` and returns a new (owned) reference
   *  to it. Called with the GIL held. Throw [[PythonException]] or any other
   *  exception on failure.
   */
  def newReference(value: T): CApi.PyPtr

  /** Converts `value` to a [[Py]]. */
  final def asPython(value: T): Py = withGil(Py.steal(newReference(value)))

  /** A conversion for `U` that first maps it to `T`. */
  final def contramap[U](f: U => T): AsPython[U] = u => newReference(f(u))
}

object AsPython extends AsPythonLowPriority {
  inline def apply[T](using conv: AsPython[T]): AsPython[T] = conv

  given AsPython[Py] = { py =>
    val p = py.ptr
    Py_IncRef(p)
    Python.keepAlive(py)
    p
  }

  given AsPython[Unit] = _ => {
    val p = Python.None.ptr
    Py_IncRef(p)
    p
  }

  given AsPython[Boolean] = b => check(PyBool_FromLong(if (b) 1 else 0))
  given AsPython[Byte] = b => check(PyLong_FromLongLong(b))
  given AsPython[Short] = s => check(PyLong_FromLongLong(s))
  given AsPython[Int] = i => check(PyLong_FromLongLong(i))
  given AsPython[Long] = l => check(PyLong_FromLongLong(l))
  given AsPython[Float] = f => check(PyFloat_FromDouble(f))
  given AsPython[Double] = d => check(PyFloat_FromDouble(d))
  given AsPython[Char] = c => fromString(c.toString)
  given AsPython[String] = fromString(_)

  given AsPython[BigInt] = b => Zone(check(PyLong_FromString(toCString(b.toString), null, 10)))

  /** `Array[Byte]` becomes `bytes`. */
  given AsPython[Array[Byte]] = bytes =>
    check(
      PyBytes_FromStringAndSize(if (bytes.isEmpty) null else bytes.at(0), bytes.length)
    )

  /** `None` becomes `None`; `Some(x)` becomes `x`. */
  given [T](using conv: AsPython[T]): AsPython[Option[T]] = {
    case Some(value) => conv.newReference(value)
    case scala.None  => AsPython[Unit].newReference(())
  }

  given [K, V](using k: AsPython[K], v: AsPython[V]): AsPython[AnyMap[K, V]] = map => {
    val d = check(PyDict_New())
    try {
      map.foreach { (key, value) =>
        val pk = k.newReference(key)
        try {
          val pv = v.newReference(value)
          try check(PyDict_SetItem(d, pk, pv))
          finally Py_DecRef(pv)
        } finally Py_DecRef(pk)
      }
      d
    } catch {
      case NonFatal(e) =>
        Py_DecRef(d)
        throw e
    }
  }

  given [T](using conv: AsPython[T]): AsPython[AnySet[T]] = set => {
    val s = check(PySet_New(null))
    try {
      set.foreach { item =>
        val p = conv.newReference(item)
        try check(PySet_Add(s, p))
        finally Py_DecRef(p)
      }
      s
    } catch {
      case NonFatal(e) =>
        Py_DecRef(s)
        throw e
    }
  }

  /** Any other collection becomes a `list`. */
  given [T](using conv: AsPython[T]): AsPython[Iterable[T]] = items =>
    fromIterator(items.iterator, items.size)

  /** Tuples become Python tuples. */
  given [T <: Tuple](using items: TupleItems[T]): AsPython[T] = tuple => {
    val t = check(PyTuple_New(items.size))
    try {
      items.write(t, 0, tuple.asInstanceOf[Product])
      t
    } catch {
      case NonFatal(e) =>
        Py_DecRef(t)
        throw e
    }
  }

  /** Converts the elements of a tuple, writing them into a Python tuple. */
  trait TupleItems[T <: Tuple] {
    def size: Int
    def write(target: PyPtr, index: Int, tuple: Product): Unit
  }

  object TupleItems {
    given TupleItems[EmptyTuple] with {
      def size = 0
      def write(target: PyPtr, index: Int, tuple: Product): Unit = ()
    }

    given [H, T <: Tuple](using head: AsPython[H], tail: TupleItems[T]): TupleItems[H *: T] with {
      def size = 1 + tail.size
      def write(target: PyPtr, index: Int, tuple: Product): Unit = {
        val item = head.newReference(tuple.productElement(index).asInstanceOf[H])
        PyTuple_SetItem(target, index, item)
        tail.write(target, index + 1, tuple)
      }
    }
  }

  private def fromString(s: String): PyPtr = {
    val bytes = s.getBytes(UTF_8)
    check(PyUnicode_FromStringAndSize(if (bytes.isEmpty) null else bytes.at(0), bytes.length))
  }

  private[snipy] def fromIterator[T](items: Iterator[T], size: Int)(using
      conv: AsPython[T]
  ): PyPtr = {
    val l = check(PyList_New(size))
    try {
      var i = 0
      while (i < size) {
        PyList_SetItem(l, i, conv.newReference(items.next()))
        i += 1
      }
      l
    } catch {
      case NonFatal(e) =>
        Py_DecRef(l)
        throw e
    }
  }
}

private[snipy] trait AsPythonLowPriority {

  /** Arrays other than `Array[Byte]` become a `list`. */
  given [T](using conv: AsPython[T]): AsPython[Array[T]] = items =>
    AsPython.fromIterator(items.iterator, items.length)

  /** Scala functions become Python callables. */
  given [R](using r: AsPython[R]): AsPython[() => R] = f =>
    Callbacks.newReference(_ => r.asPython(f()))

  given [A, R](using a: AsScala[A], r: AsPython[R]): AsPython[A => R] = f =>
    Callbacks.newReference(args => r.asPython(f(Callbacks.arg[A](args, 0, 1))))

  given [A, B, R](using a: AsScala[A], b: AsScala[B], r: AsPython[R]): AsPython[(A, B) => R] =
    f =>
      Callbacks.newReference { args =>
        r.asPython(f(Callbacks.arg[A](args, 0, 2), Callbacks.arg[B](args, 1, 2)))
      }

  given [A, B, C, R](using
      a: AsScala[A],
      b: AsScala[B],
      c: AsScala[C],
      r: AsPython[R]
  ): AsPython[(A, B, C) => R] = f =>
    Callbacks.newReference { args =>
      r.asPython(
        f(
          Callbacks.arg[A](args, 0, 3),
          Callbacks.arg[B](args, 1, 3),
          Callbacks.arg[C](args, 2, 3)
        )
      )
    }
}
