package snipy
import snipy.CApi.PyList_New

object PyList {
  def empty[T <: PyObject](implicit z: PyZone): PyList[T] =
    PyList_New(0).asInstanceOf[PyList[T]]
}