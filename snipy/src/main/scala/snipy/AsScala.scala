package snipy
import snipy.CApi._

import scala.scalanative.native.{CDouble, CLong, CSize, stackalloc}

trait AsScala[-T <: PyObject, +U] {
  def asScala(x: T): U
}

object AsScala {
  implicit val pyLongAsScala: AsScala[PyLong, CLong] =
    new AsScala[PyLong, CLong] {
      override def asScala(x: PyLong): CLong = PyLong_AsLong(x)
    }

  implicit val pyFloatAsScala: AsScala[PyFloat, CDouble] =
    new AsScala[PyFloat, CDouble] {
      override def asScala(x: PyFloat): CDouble = PyFloat_AsDouble(x)
    }

  implicit val pyStringAsScala: AsScala[PyString, String] =
    new AsScala[PyString, String] {
      def asScala(s: PyString): String = {
        val size = stackalloc[CSize]
        val cstr = PyUnicode_AsUTF8AndSize(s, size)
        val l    = (!size).toInt
        val arr  = new Array[Byte](l)
        var i    = 0
        while (i < l) {
          arr(i) = cstr(i)
          i += 1
        }
        new String(arr)
      }
    }

  implicit val pyBoolAsBoolean: AsScala[PyBool, Boolean] =
    new AsScala[PyBool, Boolean] {
      override def asScala(x: PyBool): Boolean = PyZone { implicit z =>
        if (x == true.asPython) true else false
      }
    }

  implicit def pyTuple2AsScala[T1 <: PyObject, T2 <: PyObject, U1, U2](implicit asScala1: AsScala[T1, U1],
                                                                       asScala2: AsScala[T2, U2]): AsScala[PyTuple2[T1, T2], (U1, U2)] =
    new AsScala[PyTuple2[T1, T2], (U1, U2)] {
      override def asScala(x: PyTuple2[T1, T2]): (U1, U2) =
        (PyTuple_GetItem(x, 0).asInstanceOf[T1].asScala, PyTuple_GetItem(x, 1).asInstanceOf[T2].asScala)
    }

  implicit def pyTupleAsScala[T <: PyObject, U](implicit asSc: AsScala[T, U]): AsScala[PyTuple[T], Seq[U]] =
    new AsScala[PyTuple[T], Seq[U]] {
      override def asScala(x: PyTuple[T]): Seq[U] = {
        val tuple   = x.asInstanceOf[PyTupleAny]
        val length  = PyTuple_Size(tuple)
        val builder = Seq.newBuilder[U]
        var i       = 0
        while (i < length) {
          builder += PyTuple_GetItem(tuple, i).asInstanceOf[T].asScala
          i += 1
        }
        builder.result()
      }
    }

  implicit def pyListAsScala[T <: PyObject, U](implicit asS: AsScala[T, U]): AsScala[PyList[T], Seq[U]] =
    new AsScala[PyList[T], Seq[U]] {
      override def asScala(x: PyList[T]): Seq[U] = {
        val list    = x.asInstanceOf[PyListAny]
        val length  = PyList_Size(list)
        val builder = Seq.newBuilder[U]
        var i       = 0
        while (i < length) {
          builder += PyList_GetItem(list, i).asInstanceOf[T].asScala
          i += 1
        }
        builder.result()
      }
    }

  implicit def pyDictAsScala[A <: PyObject, B <: PyObject, C, D](implicit asScA: AsScala[A, C],
                                                                 asScB: AsScala[B, D]): AsScala[PyDict[A, B], Map[C, D]] =
    new AsScala[PyDict[A, B], Map[C, D]] {
      override def asScala(x: PyDict[A, B]): Map[C, D] = {
        val m      = Map.newBuilder[C, D]
        val posPtr = stackalloc[CSize]
        !posPtr = 0
        val keyPtr   = stackalloc[PyObject]
        val valuePtr = stackalloc[PyObject]
        while (PyDict_Next(x.asInstanceOf[PyDictAny], posPtr, keyPtr, valuePtr) != 0) {
          m += (!keyPtr).asInstanceOf[A].asScala -> (!valuePtr)
            .asInstanceOf[B]
            .asScala
        }
        m.result()
      }
    }
}
