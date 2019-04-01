package snipy

import snipy.CApi._
import scala.scalanative.native._

trait AsPython[-T, +U <: PyObject] {
  def asPython(x: T)(implicit z: PyZone): U
}
object AsPython {
  implicit val cDoubleAsPython: AsPython[CDouble, PyFloat] =
    new AsPython[CDouble, PyFloat] {
      override def asPython(d: CDouble)(implicit z: PyZone): PyFloat =
        PyFloat_FromDouble(d)
    }

  implicit val cLongAsPython: AsPython[CLong, PyLong] =
    new AsPython[CLong, PyLong] {
      override def asPython(v: CLong)(implicit z: PyZone): PyLong =
        PyLong_FromLong(v)
    }

  implicit val intAsPython: AsPython[Int, PyLong] =
    new AsPython[Int, PyLong] {
      override def asPython(v: Int)(implicit z: PyZone): PyLong =
        PyLong_FromLong(v)
    }

  implicit val stringAsPython: AsPython[String, PyString] =
    new AsPython[String, PyString] {
      override def asPython(s: String)(implicit z: PyZone): PyString = {
        val bytes = s.getBytes().asInstanceOf[scalanative.runtime.ByteArray]
        val length = bytes.length
          PyUnicode_FromStringAndSize(if (length == 0) null else bytes.at(0),
            bytes.length
        )
      }
    }

  implicit val booleanAsPython: AsPython[Boolean, PyBool] =
    new AsPython[Boolean, PyBool] {
      override def asPython(x: Boolean)(implicit z: PyZone): PyBool = {
        PyBool_FromLong(if (x) 1 else 0)
      }
    }

  implicit def tuple2AsPython[T1, T2, U1 <: PyObject, U2 <: PyObject](implicit asPy1: AsPython[T1, U1],
                                                                               asPy2: AsPython[T2, U2]): AsPython[(T1, T2), PyTuple2[U1, U2]] =
    new AsPython[(T1, T2), PyTuple2[U1, U2]] {
      override def asPython(x: (T1, T2))(
        implicit z: PyZone): PyTuple2[U1, U2] = {
          z.manage(Unmanaged.PyTuple_Pack(2,
            x._1.asPython.asInstanceOf[PyObject],
            x._2.asPython.asInstanceOf[PyObject])
          .asInstanceOf[PyTuple2[U1, U2]])
      }
    }

  implicit def listAsPython[T, U <: PyObject](
                                               implicit asPy: AsPython[T, U]): AsPython[Seq[T], PyList[U]] =
    new AsPython[Seq[T], PyList[U]] {
      override def asPython(x: Seq[T])(implicit z: PyZone): PyList[U] = {
        val l = PyList_New(x.length)
        var i = 0
        while (i < x.length) {
          PyList_SetItem(l, i, x(i).asPython)
          i += 1
        }
        l.asInstanceOf[PyList[U]]
      }
    }

  implicit def mapAsPython[A, B, C <: PyObject, D <: PyObject](
                                                                implicit asPyA: AsPython[A, C],
                                                                asPyB: AsPython[B, D]): AsPython[Map[A, B], PyDict[C, D]] =
    new AsPython[Map[A, B], PyDict[C, D]] {
      override def asPython(x: Map[A, B])(implicit z: PyZone): PyDict[C, D] = {
        val d = PyDict_New()
        for ((k, v) <- x) {
          PyDict_SetItem(d, k.asPython, v.asPython)
        }
        d.asInstanceOf[PyDict[C, D]]
      }
    }
}
