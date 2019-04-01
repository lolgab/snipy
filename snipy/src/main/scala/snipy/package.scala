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

  private[snipy] trait _PyListAny
  type PyListAny = PyObject with _PyListAny

  private[snipy] trait _PyList[T <: PyObject]
  type PyList[T <: PyObject] = PyListAny with _PyList[T]

  private[snipy] trait _PyDictAny
  type PyDictAny = PyObject with _PyDictAny

  private[snipy] trait _PyDict[A <: PyObject, B <: PyObject]
  type PyDict[A <: PyObject, B <: PyObject] = PyDictAny with _PyDict[A, B]

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
