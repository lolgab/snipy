package snipy

import java.nio.charset.StandardCharsets.UTF_8

import scala.collection.mutable.ArrayBuffer
import scala.scalanative.libc.string.memcpy
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

import snipy.CApi.*
import snipy.Python.{check, checkError, owning}

/** Converts Python objects to Scala values of type `T`.
 *
 *  Implement [[fromPython]] to add a conversion for your own types.
 */
trait AsScala[T] {

  /** Converts the object at `ptr`, a borrowed reference. Called with the GIL
   *  held. Throw [[PythonException]] or any other exception on failure.
   */
  def fromPython(ptr: CApi.PyPtr): T

  /** A conversion that maps the result of this one. */
  final def map[U](f: T => U): AsScala[U] = p => f(fromPython(p))
}

object AsScala {
  inline def apply[T](using conv: AsScala[T]): AsScala[T] = conv

  given AsScala[Py] = Py.borrow(_)

  given AsScala[Unit] = _ => ()

  /** Python truthiness: `bool(obj)`. */
  given AsScala[Boolean] = p => check(PyObject_IsTrue(p)) == 1

  given AsScala[Long] = p => {
    val l = PyLong_AsLongLong(p)
    if (l == -1) checkError()
    l
  }

  given AsScala[Int] = narrow(Int.MinValue, Int.MaxValue, "Int")(_.toInt)
  given AsScala[Short] = narrow(Short.MinValue, Short.MaxValue, "Short")(_.toShort)
  given AsScala[Byte] = narrow(Byte.MinValue, Byte.MaxValue, "Byte")(_.toByte)

  given AsScala[Double] = p => {
    val d = PyFloat_AsDouble(p)
    if (d == -1.0) checkError()
    d
  }

  given AsScala[Float] = AsScala[Double].map(_.toFloat)

  /** Only accepts `str`; use `py.toString` for Python's `str(obj)`. */
  given AsScala[String] = p => {
    val size = stackalloc[CSSize]()
    val utf8 = PyUnicode_AsUTF8AndSize(p, size)
    if (utf8 == null) Python.raise()
    val n = (!size).toInt
    val bytes = new Array[Byte](n)
    if (n > 0) memcpy(bytes.at(0), utf8, n.toCSize)
    new String(bytes, UTF_8)
  }

  given AsScala[Char] = AsScala[String].map { s =>
    if (s.length != 1)
      throw new IllegalArgumentException(s"Expected a string of length 1, got ${s.length}")
    s.charAt(0)
  }

  given AsScala[BigInt] = p =>
    owning(check(PyNumber_Long(p))) { int =>
      owning(check(PyObject_Str(int)))(digits => BigInt(AsScala[String].fromPython(digits)))
    }

  /** Only accepts `bytes`. */
  given AsScala[Array[Byte]] = p => {
    val buffer = stackalloc[CString]()
    val size = stackalloc[CSSize]()
    check(PyBytes_AsStringAndSize(p, buffer, size))
    val n = (!size).toInt
    val bytes = new Array[Byte](n)
    if (n > 0) memcpy(bytes.at(0), !buffer, n.toCSize)
    bytes
  }

  /** `None` becomes `None`, anything else is converted with `T`'s conversion. */
  given [T](using conv: AsScala[T]): AsScala[Option[T]] = p =>
    if (p == Python.None.ptr) scala.None else Some(conv.fromPython(p))

  /** Accepts any iterable. */
  given [T](using conv: AsScala[T]): AsScala[List[T]] = iterate(_).toList
  given [T](using conv: AsScala[T]): AsScala[Vector[T]] = iterate(_).toVector
  given [T](using conv: AsScala[T]): AsScala[Seq[T]] = iterate(_).toList
  given [T](using conv: AsScala[T]): AsScala[Set[T]] = iterate(_).toSet

  /** Accepts any mapping. */
  given [K, V](using k: AsScala[K], v: AsScala[V]): AsScala[Map[K, V]] = p =>
    owning(check(PyMapping_Items(p))) { items =>
      val builder = Map.newBuilder[K, V]
      foreach(items) { item =>
        builder += k.fromPython(PyTuple_GetItem(item, 0)) -> v.fromPython(PyTuple_GetItem(item, 1))
      }
      builder.result()
    }

  /** Accepts any sequence of the right length. */
  given [T <: Tuple](using items: TupleItems[T]): AsScala[T] = p =>
    owning(check(PySequence_Tuple(p))) { t =>
      val size = PyTuple_Size(t).toInt
      if (size != items.size)
        throw new IllegalArgumentException(s"Expected a tuple of ${items.size} elements, got $size")
      val values = new Array[Any](size)
      items.read(t, 0, values)
      Tuple.fromArray(values).asInstanceOf[T]
    }

  /** Converts the elements of a Python tuple. */
  trait TupleItems[T <: Tuple] {
    def size: Int
    def read(source: PyPtr, index: Int, target: Array[Any]): Unit
  }

  object TupleItems {
    given TupleItems[EmptyTuple] with {
      def size = 0
      def read(source: PyPtr, index: Int, target: Array[Any]): Unit = ()
    }

    given [H, T <: Tuple](using head: AsScala[H], tail: TupleItems[T]): TupleItems[H *: T] with {
      def size = 1 + tail.size
      def read(source: PyPtr, index: Int, target: Array[Any]): Unit = {
        target(index) = head.fromPython(PyTuple_GetItem(source, index))
        tail.read(source, index + 1, target)
      }
    }
  }

  private def narrow[T](min: Long, max: Long, name: String)(f: Long => T): AsScala[T] =
    AsScala[Long].map { l =>
      if (l < min || l > max) throw new ArithmeticException(s"$l does not fit in $name")
      f(l)
    }

  private def iterate[T](p: PyPtr)(using conv: AsScala[T]): ArrayBuffer[T] = {
    val buffer = ArrayBuffer.empty[T]
    foreach(p)(item => buffer += conv.fromPython(item))
    buffer
  }

  /** Calls `f` with a borrowed reference to each item of the iterable `p`. */
  private inline def foreach(p: PyPtr)(inline f: PyPtr => Unit): Unit =
    owning(check(PyObject_GetIter(p))) { it =>
      var item = PyIter_Next(it)
      while (item != null) {
        owning(item)(f)
        item = PyIter_Next(it)
      }
      checkError()
    }
}
