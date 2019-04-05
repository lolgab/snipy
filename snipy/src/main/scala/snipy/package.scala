import snipy.CApi._
import scalanative.native._

package object snipy {
  Py_Initialize() // side effect

  private[snipy] trait _PyObject
  type PyObject = Ptr[Byte] with _PyObject

  private[snipy] trait _PyFloat
  type PyFloat = PyObject with _PyFloat

  private[snipy] trait _PyLong
  type PyLong = PyObject with _PyLong

  private[snipy] trait _PyString
  type PyString = PyObject with _PyString

  private[snipy] trait _PyBool
  type PyBool = PyObject with _PyBool

  private[snipy] trait _PyTupleAny
  type PyTupleAny = PyObject with _PyTupleAny

  private[snipy] trait _PyTuple[T]
  type PyTuple[T] = PyTupleAny with _PyTuple[T]

  private[snipy] trait _PyTuple2[T1 <: PyObject, T2 <: PyObject]
  type PyTuple2[T1 <: PyObject, T2 <: PyObject] = PyTupleAny with _PyTuple2[T1, T2]
  private[snipy] trait _PyTuple3[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject]
  type PyTuple3[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject] = PyTupleAny with _PyTuple3[T1, T2, T3]
  private[snipy] trait _PyTuple4[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject]
  type PyTuple4[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject] = PyTupleAny with _PyTuple4[T1, T2, T3, T4]
  private[snipy] trait _PyTuple5[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject]
  type PyTuple5[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject] = PyTupleAny with _PyTuple5[T1, T2, T3, T4, T5]
  private[snipy] trait _PyTuple6[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject]
  type PyTuple6[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject] = PyTupleAny with _PyTuple6[T1, T2, T3, T4, T5, T6]
  private[snipy] trait _PyTuple7[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject]
  type PyTuple7[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject] = PyTupleAny with _PyTuple7[T1, T2, T3, T4, T5, T6, T7]
  private[snipy] trait _PyTuple8[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject]
  type PyTuple8[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject] = PyTupleAny with _PyTuple8[T1, T2, T3, T4, T5, T6, T7, T8]
  private[snipy] trait _PyTuple9[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject]
  type PyTuple9[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject] = PyTupleAny with _PyTuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9]
  private[snipy] trait _PyTuple10[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject]
  type PyTuple10[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject] = PyTupleAny with _PyTuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]
  private[snipy] trait _PyTuple11[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject]
  type PyTuple11[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject] = PyTupleAny with _PyTuple11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]
  private[snipy] trait _PyTuple12[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject]
  type PyTuple12[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject] = PyTupleAny with _PyTuple12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]
  private[snipy] trait _PyTuple13[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject]
  type PyTuple13[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject] = PyTupleAny with _PyTuple13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13]
  private[snipy] trait _PyTuple14[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject]
  type PyTuple14[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject] = PyTupleAny with _PyTuple14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14]
  private[snipy] trait _PyTuple15[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject]
  type PyTuple15[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject] = PyTupleAny with _PyTuple15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15]
  private[snipy] trait _PyTuple16[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject, T16 <: PyObject]
  type PyTuple16[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject, T16 <: PyObject] = PyTupleAny with _PyTuple16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16]
  private[snipy] trait _PyTuple17[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject, T16 <: PyObject, T17 <: PyObject]
  type PyTuple17[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject, T16 <: PyObject, T17 <: PyObject] = PyTupleAny with _PyTuple17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17]
  private[snipy] trait _PyTuple18[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject, T16 <: PyObject, T17 <: PyObject, T18 <: PyObject]
  type PyTuple18[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject, T16 <: PyObject, T17 <: PyObject, T18 <: PyObject] = PyTupleAny with _PyTuple18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18]
  private[snipy] trait _PyTuple19[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject, T16 <: PyObject, T17 <: PyObject, T18 <: PyObject, T19 <: PyObject]
  type PyTuple19[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject, T16 <: PyObject, T17 <: PyObject, T18 <: PyObject, T19 <: PyObject] = PyTupleAny with _PyTuple19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19]
  private[snipy] trait _PyTuple20[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject, T16 <: PyObject, T17 <: PyObject, T18 <: PyObject, T19 <: PyObject, T20 <: PyObject]
  type PyTuple20[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject, T16 <: PyObject, T17 <: PyObject, T18 <: PyObject, T19 <: PyObject, T20 <: PyObject] = PyTupleAny with _PyTuple20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20]
  private[snipy] trait _PyTuple21[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject, T16 <: PyObject, T17 <: PyObject, T18 <: PyObject, T19 <: PyObject, T20 <: PyObject, T21 <: PyObject]
  type PyTuple21[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject, T16 <: PyObject, T17 <: PyObject, T18 <: PyObject, T19 <: PyObject, T20 <: PyObject, T21 <: PyObject] = PyTupleAny with _PyTuple21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21]
  private[snipy] trait _PyTuple22[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject, T16 <: PyObject, T17 <: PyObject, T18 <: PyObject, T19 <: PyObject, T20 <: PyObject, T21 <: PyObject, T22 <: PyObject]
  type PyTuple22[T1 <: PyObject, T2 <: PyObject, T3 <: PyObject, T4 <: PyObject, T5 <: PyObject, T6 <: PyObject, T7 <: PyObject, T8 <: PyObject, T9 <: PyObject, T10 <: PyObject, T11 <: PyObject, T12 <: PyObject, T13 <: PyObject, T14 <: PyObject, T15 <: PyObject, T16 <: PyObject, T17 <: PyObject, T18 <: PyObject, T19 <: PyObject, T20 <: PyObject, T21 <: PyObject, T22 <: PyObject] = PyTupleAny with _PyTuple22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22]

  private[snipy] trait _PyListAny
  type PyListAny = PyObject with _PyListAny

  private[snipy] trait _PyList[T <: PyObject]
  type PyList[T <: PyObject] = PyListAny with _PyList[T]

  private[snipy] trait _PyDictAny
  type PyDictAny = PyObject with _PyDictAny

  private[snipy] trait _PyDict[K <: PyObject, V <: PyObject]
  type PyDict[K <: PyObject, V <: PyObject] = PyDictAny with _PyDict[K, V]

  private[snipy] trait _PyModule
  type PyModule = PyObject with _PyModule

  implicit class AsPythonOps[T](val t: T) extends AnyVal {
    def asPython[U <: PyObject](implicit asPython: AsPython[T, U], zone: PyZone): U = asPython.asPython(t)
  }

  implicit class AsScalaOps[T <: PyObject](val t: T) extends AnyVal {
    def asScala[U](implicit asScala: AsScala[T, U]): U =
      asScala.asScala(t)
  }

  def PyNone(implicit z: PyZone): PyObject with Nothing = Py_BuildValue(c"").asInstanceOf[PyObject with Nothing]
}
