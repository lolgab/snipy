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


  implicit def tuple2AsPython[T1, T2, U1 <: PyObject, U2 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2]): AsPython[(T1, T2), PyTuple2[U1, U2]] =
    new AsPython[(T1, T2), PyTuple2[U1, U2]] {
      override def asPython(x: (T1, T2))(
          implicit z: PyZone): PyTuple2[U1, U2] = {
        z.manage(
            Unmanaged.PyTuple_Pack(2,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple2[U1, U2]]
      }
    }


  implicit def tuple3AsPython[T1, T2, T3, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3]): AsPython[(T1, T2, T3), PyTuple3[U1, U2, U3]] =
    new AsPython[(T1, T2, T3), PyTuple3[U1, U2, U3]] {
      override def asPython(x: (T1, T2, T3))(
          implicit z: PyZone): PyTuple3[U1, U2, U3] = {
        z.manage(
            Unmanaged.PyTuple_Pack(3,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple3[U1, U2, U3]]
      }
    }


  implicit def tuple4AsPython[T1, T2, T3, T4, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4]): AsPython[(T1, T2, T3, T4), PyTuple4[U1, U2, U3, U4]] =
    new AsPython[(T1, T2, T3, T4), PyTuple4[U1, U2, U3, U4]] {
      override def asPython(x: (T1, T2, T3, T4))(
          implicit z: PyZone): PyTuple4[U1, U2, U3, U4] = {
        z.manage(
            Unmanaged.PyTuple_Pack(4,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple4[U1, U2, U3, U4]]
      }
    }


  implicit def tuple5AsPython[T1, T2, T3, T4, T5, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5]): AsPython[(T1, T2, T3, T4, T5), PyTuple5[U1, U2, U3, U4, U5]] =
    new AsPython[(T1, T2, T3, T4, T5), PyTuple5[U1, U2, U3, U4, U5]] {
      override def asPython(x: (T1, T2, T3, T4, T5))(
          implicit z: PyZone): PyTuple5[U1, U2, U3, U4, U5] = {
        z.manage(
            Unmanaged.PyTuple_Pack(5,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple5[U1, U2, U3, U4, U5]]
      }
    }


  implicit def tuple6AsPython[T1, T2, T3, T4, T5, T6, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6]): AsPython[(T1, T2, T3, T4, T5, T6), PyTuple6[U1, U2, U3, U4, U5, U6]] =
    new AsPython[(T1, T2, T3, T4, T5, T6), PyTuple6[U1, U2, U3, U4, U5, U6]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6))(
          implicit z: PyZone): PyTuple6[U1, U2, U3, U4, U5, U6] = {
        z.manage(
            Unmanaged.PyTuple_Pack(6,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple6[U1, U2, U3, U4, U5, U6]]
      }
    }


  implicit def tuple7AsPython[T1, T2, T3, T4, T5, T6, T7, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7]): AsPython[(T1, T2, T3, T4, T5, T6, T7), PyTuple7[U1, U2, U3, U4, U5, U6, U7]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7), PyTuple7[U1, U2, U3, U4, U5, U6, U7]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7))(
          implicit z: PyZone): PyTuple7[U1, U2, U3, U4, U5, U6, U7] = {
        z.manage(
            Unmanaged.PyTuple_Pack(7,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple7[U1, U2, U3, U4, U5, U6, U7]]
      }
    }


  implicit def tuple8AsPython[T1, T2, T3, T4, T5, T6, T7, T8, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8), PyTuple8[U1, U2, U3, U4, U5, U6, U7, U8]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8), PyTuple8[U1, U2, U3, U4, U5, U6, U7, U8]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8))(
          implicit z: PyZone): PyTuple8[U1, U2, U3, U4, U5, U6, U7, U8] = {
        z.manage(
            Unmanaged.PyTuple_Pack(8,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple8[U1, U2, U3, U4, U5, U6, U7, U8]]
      }
    }


  implicit def tuple9AsPython[T1, T2, T3, T4, T5, T6, T7, T8, T9, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject, U9 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8], asPy9: AsPython[T9, U9]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9), PyTuple9[U1, U2, U3, U4, U5, U6, U7, U8, U9]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9), PyTuple9[U1, U2, U3, U4, U5, U6, U7, U8, U9]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8, T9))(
          implicit z: PyZone): PyTuple9[U1, U2, U3, U4, U5, U6, U7, U8, U9] = {
        z.manage(
            Unmanaged.PyTuple_Pack(9,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject], x._9.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple9[U1, U2, U3, U4, U5, U6, U7, U8, U9]]
      }
    }


  implicit def tuple10AsPython[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject, U9 <: PyObject, U10 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8], asPy9: AsPython[T9, U9], asPy10: AsPython[T10, U10]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10), PyTuple10[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10), PyTuple10[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10))(
          implicit z: PyZone): PyTuple10[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10] = {
        z.manage(
            Unmanaged.PyTuple_Pack(10,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject], x._9.asPython.asInstanceOf[PyObject], x._10.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple10[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10]]
      }
    }


  implicit def tuple11AsPython[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject, U9 <: PyObject, U10 <: PyObject, U11 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8], asPy9: AsPython[T9, U9], asPy10: AsPython[T10, U10], asPy11: AsPython[T11, U11]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11), PyTuple11[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11), PyTuple11[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11))(
          implicit z: PyZone): PyTuple11[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11] = {
        z.manage(
            Unmanaged.PyTuple_Pack(11,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject], x._9.asPython.asInstanceOf[PyObject], x._10.asPython.asInstanceOf[PyObject], x._11.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple11[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11]]
      }
    }


  implicit def tuple12AsPython[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject, U9 <: PyObject, U10 <: PyObject, U11 <: PyObject, U12 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8], asPy9: AsPython[T9, U9], asPy10: AsPython[T10, U10], asPy11: AsPython[T11, U11], asPy12: AsPython[T12, U12]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12), PyTuple12[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12), PyTuple12[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12))(
          implicit z: PyZone): PyTuple12[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12] = {
        z.manage(
            Unmanaged.PyTuple_Pack(12,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject], x._9.asPython.asInstanceOf[PyObject], x._10.asPython.asInstanceOf[PyObject], x._11.asPython.asInstanceOf[PyObject], x._12.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple12[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12]]
      }
    }


  implicit def tuple13AsPython[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject, U9 <: PyObject, U10 <: PyObject, U11 <: PyObject, U12 <: PyObject, U13 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8], asPy9: AsPython[T9, U9], asPy10: AsPython[T10, U10], asPy11: AsPython[T11, U11], asPy12: AsPython[T12, U12], asPy13: AsPython[T13, U13]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13), PyTuple13[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13), PyTuple13[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13))(
          implicit z: PyZone): PyTuple13[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13] = {
        z.manage(
            Unmanaged.PyTuple_Pack(13,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject], x._9.asPython.asInstanceOf[PyObject], x._10.asPython.asInstanceOf[PyObject], x._11.asPython.asInstanceOf[PyObject], x._12.asPython.asInstanceOf[PyObject], x._13.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple13[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13]]
      }
    }


  implicit def tuple14AsPython[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject, U9 <: PyObject, U10 <: PyObject, U11 <: PyObject, U12 <: PyObject, U13 <: PyObject, U14 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8], asPy9: AsPython[T9, U9], asPy10: AsPython[T10, U10], asPy11: AsPython[T11, U11], asPy12: AsPython[T12, U12], asPy13: AsPython[T13, U13], asPy14: AsPython[T14, U14]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14), PyTuple14[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14), PyTuple14[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14))(
          implicit z: PyZone): PyTuple14[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14] = {
        z.manage(
            Unmanaged.PyTuple_Pack(14,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject], x._9.asPython.asInstanceOf[PyObject], x._10.asPython.asInstanceOf[PyObject], x._11.asPython.asInstanceOf[PyObject], x._12.asPython.asInstanceOf[PyObject], x._13.asPython.asInstanceOf[PyObject], x._14.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple14[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14]]
      }
    }


  implicit def tuple15AsPython[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject, U9 <: PyObject, U10 <: PyObject, U11 <: PyObject, U12 <: PyObject, U13 <: PyObject, U14 <: PyObject, U15 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8], asPy9: AsPython[T9, U9], asPy10: AsPython[T10, U10], asPy11: AsPython[T11, U11], asPy12: AsPython[T12, U12], asPy13: AsPython[T13, U13], asPy14: AsPython[T14, U14], asPy15: AsPython[T15, U15]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15), PyTuple15[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15), PyTuple15[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15))(
          implicit z: PyZone): PyTuple15[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15] = {
        z.manage(
            Unmanaged.PyTuple_Pack(15,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject], x._9.asPython.asInstanceOf[PyObject], x._10.asPython.asInstanceOf[PyObject], x._11.asPython.asInstanceOf[PyObject], x._12.asPython.asInstanceOf[PyObject], x._13.asPython.asInstanceOf[PyObject], x._14.asPython.asInstanceOf[PyObject], x._15.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple15[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15]]
      }
    }


  implicit def tuple16AsPython[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject, U9 <: PyObject, U10 <: PyObject, U11 <: PyObject, U12 <: PyObject, U13 <: PyObject, U14 <: PyObject, U15 <: PyObject, U16 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8], asPy9: AsPython[T9, U9], asPy10: AsPython[T10, U10], asPy11: AsPython[T11, U11], asPy12: AsPython[T12, U12], asPy13: AsPython[T13, U13], asPy14: AsPython[T14, U14], asPy15: AsPython[T15, U15], asPy16: AsPython[T16, U16]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16), PyTuple16[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16), PyTuple16[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16))(
          implicit z: PyZone): PyTuple16[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16] = {
        z.manage(
            Unmanaged.PyTuple_Pack(16,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject], x._9.asPython.asInstanceOf[PyObject], x._10.asPython.asInstanceOf[PyObject], x._11.asPython.asInstanceOf[PyObject], x._12.asPython.asInstanceOf[PyObject], x._13.asPython.asInstanceOf[PyObject], x._14.asPython.asInstanceOf[PyObject], x._15.asPython.asInstanceOf[PyObject], x._16.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple16[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16]]
      }
    }


  implicit def tuple17AsPython[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject, U9 <: PyObject, U10 <: PyObject, U11 <: PyObject, U12 <: PyObject, U13 <: PyObject, U14 <: PyObject, U15 <: PyObject, U16 <: PyObject, U17 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8], asPy9: AsPython[T9, U9], asPy10: AsPython[T10, U10], asPy11: AsPython[T11, U11], asPy12: AsPython[T12, U12], asPy13: AsPython[T13, U13], asPy14: AsPython[T14, U14], asPy15: AsPython[T15, U15], asPy16: AsPython[T16, U16], asPy17: AsPython[T17, U17]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17), PyTuple17[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17), PyTuple17[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17))(
          implicit z: PyZone): PyTuple17[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17] = {
        z.manage(
            Unmanaged.PyTuple_Pack(17,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject], x._9.asPython.asInstanceOf[PyObject], x._10.asPython.asInstanceOf[PyObject], x._11.asPython.asInstanceOf[PyObject], x._12.asPython.asInstanceOf[PyObject], x._13.asPython.asInstanceOf[PyObject], x._14.asPython.asInstanceOf[PyObject], x._15.asPython.asInstanceOf[PyObject], x._16.asPython.asInstanceOf[PyObject], x._17.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple17[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17]]
      }
    }


  implicit def tuple18AsPython[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject, U9 <: PyObject, U10 <: PyObject, U11 <: PyObject, U12 <: PyObject, U13 <: PyObject, U14 <: PyObject, U15 <: PyObject, U16 <: PyObject, U17 <: PyObject, U18 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8], asPy9: AsPython[T9, U9], asPy10: AsPython[T10, U10], asPy11: AsPython[T11, U11], asPy12: AsPython[T12, U12], asPy13: AsPython[T13, U13], asPy14: AsPython[T14, U14], asPy15: AsPython[T15, U15], asPy16: AsPython[T16, U16], asPy17: AsPython[T17, U17], asPy18: AsPython[T18, U18]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18), PyTuple18[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18), PyTuple18[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18))(
          implicit z: PyZone): PyTuple18[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18] = {
        z.manage(
            Unmanaged.PyTuple_Pack(18,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject], x._9.asPython.asInstanceOf[PyObject], x._10.asPython.asInstanceOf[PyObject], x._11.asPython.asInstanceOf[PyObject], x._12.asPython.asInstanceOf[PyObject], x._13.asPython.asInstanceOf[PyObject], x._14.asPython.asInstanceOf[PyObject], x._15.asPython.asInstanceOf[PyObject], x._16.asPython.asInstanceOf[PyObject], x._17.asPython.asInstanceOf[PyObject], x._18.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple18[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18]]
      }
    }


  implicit def tuple19AsPython[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject, U9 <: PyObject, U10 <: PyObject, U11 <: PyObject, U12 <: PyObject, U13 <: PyObject, U14 <: PyObject, U15 <: PyObject, U16 <: PyObject, U17 <: PyObject, U18 <: PyObject, U19 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8], asPy9: AsPython[T9, U9], asPy10: AsPython[T10, U10], asPy11: AsPython[T11, U11], asPy12: AsPython[T12, U12], asPy13: AsPython[T13, U13], asPy14: AsPython[T14, U14], asPy15: AsPython[T15, U15], asPy16: AsPython[T16, U16], asPy17: AsPython[T17, U17], asPy18: AsPython[T18, U18], asPy19: AsPython[T19, U19]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19), PyTuple19[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19), PyTuple19[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19))(
          implicit z: PyZone): PyTuple19[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19] = {
        z.manage(
            Unmanaged.PyTuple_Pack(19,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject], x._9.asPython.asInstanceOf[PyObject], x._10.asPython.asInstanceOf[PyObject], x._11.asPython.asInstanceOf[PyObject], x._12.asPython.asInstanceOf[PyObject], x._13.asPython.asInstanceOf[PyObject], x._14.asPython.asInstanceOf[PyObject], x._15.asPython.asInstanceOf[PyObject], x._16.asPython.asInstanceOf[PyObject], x._17.asPython.asInstanceOf[PyObject], x._18.asPython.asInstanceOf[PyObject], x._19.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple19[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19]]
      }
    }


  implicit def tuple20AsPython[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject, U9 <: PyObject, U10 <: PyObject, U11 <: PyObject, U12 <: PyObject, U13 <: PyObject, U14 <: PyObject, U15 <: PyObject, U16 <: PyObject, U17 <: PyObject, U18 <: PyObject, U19 <: PyObject, U20 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8], asPy9: AsPython[T9, U9], asPy10: AsPython[T10, U10], asPy11: AsPython[T11, U11], asPy12: AsPython[T12, U12], asPy13: AsPython[T13, U13], asPy14: AsPython[T14, U14], asPy15: AsPython[T15, U15], asPy16: AsPython[T16, U16], asPy17: AsPython[T17, U17], asPy18: AsPython[T18, U18], asPy19: AsPython[T19, U19], asPy20: AsPython[T20, U20]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20), PyTuple20[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19, U20]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20), PyTuple20[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19, U20]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20))(
          implicit z: PyZone): PyTuple20[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19, U20] = {
        z.manage(
            Unmanaged.PyTuple_Pack(20,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject], x._9.asPython.asInstanceOf[PyObject], x._10.asPython.asInstanceOf[PyObject], x._11.asPython.asInstanceOf[PyObject], x._12.asPython.asInstanceOf[PyObject], x._13.asPython.asInstanceOf[PyObject], x._14.asPython.asInstanceOf[PyObject], x._15.asPython.asInstanceOf[PyObject], x._16.asPython.asInstanceOf[PyObject], x._17.asPython.asInstanceOf[PyObject], x._18.asPython.asInstanceOf[PyObject], x._19.asPython.asInstanceOf[PyObject], x._20.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple20[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19, U20]]
      }
    }


  implicit def tuple21AsPython[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject, U9 <: PyObject, U10 <: PyObject, U11 <: PyObject, U12 <: PyObject, U13 <: PyObject, U14 <: PyObject, U15 <: PyObject, U16 <: PyObject, U17 <: PyObject, U18 <: PyObject, U19 <: PyObject, U20 <: PyObject, U21 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8], asPy9: AsPython[T9, U9], asPy10: AsPython[T10, U10], asPy11: AsPython[T11, U11], asPy12: AsPython[T12, U12], asPy13: AsPython[T13, U13], asPy14: AsPython[T14, U14], asPy15: AsPython[T15, U15], asPy16: AsPython[T16, U16], asPy17: AsPython[T17, U17], asPy18: AsPython[T18, U18], asPy19: AsPython[T19, U19], asPy20: AsPython[T20, U20], asPy21: AsPython[T21, U21]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21), PyTuple21[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19, U20, U21]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21), PyTuple21[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19, U20, U21]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21))(
          implicit z: PyZone): PyTuple21[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19, U20, U21] = {
        z.manage(
            Unmanaged.PyTuple_Pack(21,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject], x._9.asPython.asInstanceOf[PyObject], x._10.asPython.asInstanceOf[PyObject], x._11.asPython.asInstanceOf[PyObject], x._12.asPython.asInstanceOf[PyObject], x._13.asPython.asInstanceOf[PyObject], x._14.asPython.asInstanceOf[PyObject], x._15.asPython.asInstanceOf[PyObject], x._16.asPython.asInstanceOf[PyObject], x._17.asPython.asInstanceOf[PyObject], x._18.asPython.asInstanceOf[PyObject], x._19.asPython.asInstanceOf[PyObject], x._20.asPython.asInstanceOf[PyObject], x._21.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple21[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19, U20, U21]]
      }
    }


  implicit def tuple22AsPython[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, U1 <: PyObject, U2 <: PyObject, U3 <: PyObject, U4 <: PyObject, U5 <: PyObject, U6 <: PyObject, U7 <: PyObject, U8 <: PyObject, U9 <: PyObject, U10 <: PyObject, U11 <: PyObject, U12 <: PyObject, U13 <: PyObject, U14 <: PyObject, U15 <: PyObject, U16 <: PyObject, U17 <: PyObject, U18 <: PyObject, U19 <: PyObject, U20 <: PyObject, U21 <: PyObject, U22 <: PyObject](
      implicit asPy1: AsPython[T1, U1], asPy2: AsPython[T2, U2], asPy3: AsPython[T3, U3], asPy4: AsPython[T4, U4], asPy5: AsPython[T5, U5], asPy6: AsPython[T6, U6], asPy7: AsPython[T7, U7], asPy8: AsPython[T8, U8], asPy9: AsPython[T9, U9], asPy10: AsPython[T10, U10], asPy11: AsPython[T11, U11], asPy12: AsPython[T12, U12], asPy13: AsPython[T13, U13], asPy14: AsPython[T14, U14], asPy15: AsPython[T15, U15], asPy16: AsPython[T16, U16], asPy17: AsPython[T17, U17], asPy18: AsPython[T18, U18], asPy19: AsPython[T19, U19], asPy20: AsPython[T20, U20], asPy21: AsPython[T21, U21], asPy22: AsPython[T22, U22]): AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22), PyTuple22[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19, U20, U21, U22]] =
    new AsPython[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22), PyTuple22[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19, U20, U21, U22]] {
      override def asPython(x: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22))(
          implicit z: PyZone): PyTuple22[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19, U20, U21, U22] = {
        z.manage(
            Unmanaged.PyTuple_Pack(22,
                         x._1.asPython.asInstanceOf[PyObject], x._2.asPython.asInstanceOf[PyObject], x._3.asPython.asInstanceOf[PyObject], x._4.asPython.asInstanceOf[PyObject], x._5.asPython.asInstanceOf[PyObject], x._6.asPython.asInstanceOf[PyObject], x._7.asPython.asInstanceOf[PyObject], x._8.asPython.asInstanceOf[PyObject], x._9.asPython.asInstanceOf[PyObject], x._10.asPython.asInstanceOf[PyObject], x._11.asPython.asInstanceOf[PyObject], x._12.asPython.asInstanceOf[PyObject], x._13.asPython.asInstanceOf[PyObject], x._14.asPython.asInstanceOf[PyObject], x._15.asPython.asInstanceOf[PyObject], x._16.asPython.asInstanceOf[PyObject], x._17.asPython.asInstanceOf[PyObject], x._18.asPython.asInstanceOf[PyObject], x._19.asPython.asInstanceOf[PyObject], x._20.asPython.asInstanceOf[PyObject], x._21.asPython.asInstanceOf[PyObject], x._22.asPython.asInstanceOf[PyObject]))
          .asInstanceOf[PyTuple22[U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, U14, U15, U16, U17, U18, U19, U20, U21, U22]]
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
