package snipy

import scala.scalanative.native.{CDouble, CInt, CLong, CSize, CString, CVararg, Ptr, extern}

object CApi {
  @extern
  object Unmanaged {
    def Py_Initialize(): Unit = extern
    def Py_BuildValue(format: CString): PyObject = extern
    def PyUnicode_FromString(string: CString): PyString = extern
    def PyUnicode_FromStringAndSize(string: CString, size: CSize): PyString =
      extern
    def PyUnicode_AsUTF8(string: PyString): CString = extern
    def PyUnicode_AsUTF8AndSize(string: PyString, size: Ptr[CSize]): CString =
      extern
    def PyUnicode_GetLength(obj: PyObject): CSize = extern
    def PyImport_Import(name: PyString): PyModule = extern
    def PyObject_GetAttrString(obj: PyObject, string: CString): PyObject = extern
    def PyObject_Call(obj: PyObject, args: PyTupleAny, kwargs: PyDictAny): PyObject = extern
    def PyObject_CallObject(obj: PyObject, args: PyTupleAny): PyObject = extern
    def PyObject_CallFunctionObjArgs[T](function: PyObject,
                                        arguments: CVararg*): PyObject = extern
    def PyObject_CallMethod(obj: PyObject,
                            name: CString,
                            format: CString): PyObject = extern
    def PyObject_CallMethodObjArgs(obj: PyObject,
                                   method: CString,
                                   arguments: CVararg*): PyObject = extern
    def PyFloat_FromDouble(v: CDouble): PyFloat = extern
    def PyFloat_AsDouble(pyFloat: PyFloat): CDouble = extern
    def PyLong_FromLong(v: CLong): PyLong = extern
    def PyLong_AsLong(pyLong: PyLong): Long = extern
    def PyBool_FromLong(l: CLong): PyBool = extern

    def PyTuple_New(len: CSize): PyTupleAny = extern
    def PyTuple_Pack(size: CSize, elements: CVararg*): PyTupleAny = extern
    def PyTuple_GetItem(tuple: PyTupleAny, elem: CSize): PyObject = extern
    def PyTuple_SetItem(tuple: PyTupleAny, elem: CSize, value: PyObject): CInt = extern
    def PyTuple_Size(tuple: PyTupleAny): CSize = extern
    def PyList_New(len: CSize): PyListAny = extern
    def PyList_Size(list: PyListAny): CSize = extern
    def PyList_GetItem(list: PyListAny, index: CSize): PyObject = extern
    def PyList_SetItem(list: PyListAny, index: CSize, element: PyObject): CInt =
      extern
    def PyDict_New(): PyDictAny = extern
    def PyDict_SetItem(dict: PyDictAny, key: PyObject, value: PyObject): CInt =
      extern
    def PyDict_GetItem(dict: PyDictAny, key: PyObject): PyObject = extern
    def PyDict_Next(dict: PyDictAny,
                    posPtr: Ptr[CSize],
                    keysPtr: Ptr[PyObject],
                    valuePtr: Ptr[PyObject]): CInt = extern
    def Py_DecRef(obj: PyObject): Unit = extern
  }

  @inline def Py_Initialize(): Unit = Unmanaged.Py_Initialize()
  @inline def Py_BuildValue(format: CString)(implicit z: PyZone): PyObject = z.manage(Unmanaged.Py_BuildValue(format))
  @inline def PyUnicode_FromString(string: CString)(implicit z: PyZone): PyString = z.manage(Unmanaged.PyUnicode_FromString(string))
  @inline def PyUnicode_FromStringAndSize(string: CString, size: CSize)(implicit z: PyZone): PyString = z.manage(Unmanaged.PyUnicode_FromStringAndSize(string, size))
  @inline def PyUnicode_AsUTF8(string: PyString): CString = Unmanaged.PyUnicode_AsUTF8(string)
  @inline def PyUnicode_AsUTF8AndSize(string: PyString, size: Ptr[CSize]): CString = Unmanaged.PyUnicode_AsUTF8AndSize(string, size)
  @inline def PyUnicode_GetLength(obj: PyObject): CSize = Unmanaged.PyUnicode_GetLength(obj)
  @inline def PyImport_Import(name: PyString)(implicit z: PyZone): PyModule = z.manage(Unmanaged.PyImport_Import(name))
  @inline def PyObject_GetAttrString(obj: PyObject, string: CString)(implicit z: PyZone): PyObject = z.manage(Unmanaged.PyObject_GetAttrString(obj, string))
  @inline def PyObject_Call(obj: PyObject, args: PyTupleAny, kwargs: PyDictAny)(implicit z: PyZone): PyObject = z.manage(Unmanaged.PyObject_Call(obj, args, kwargs))
  @inline def PyObject_CallObject(obj: PyObject, args: PyTupleAny)(implicit z: PyZone): PyObject = z.manage(Unmanaged.PyObject_CallObject(obj, args))
//  @inline def PyObject_CallFunctionObjArgs[T](function: PyObject,arguments: CVararg*)(implicit z: PyZone): PyObject = z.manage(Unmanaged.PyObject_CallFunctionObjArgs(function, arguments: _*))
  @inline def PyObject_CallMethod(obj: PyObject,name: CString,format: CString)(implicit z: PyZone): PyObject = z.manage(Unmanaged.PyObject_CallMethod(obj, name, format))
//  @inline def PyObject_CallMethodObjArgs(obj: PyObject,method: CString,arguments: CVararg*)(implicit z: PyZone): PyObject = z.manage(Unmanaged.PyObject_CallMethodObjArgs(obj, method, arguments: _*))
  @inline def PyFloat_FromDouble(v: CDouble)(implicit z: PyZone): PyFloat = z.manage(Unmanaged.PyFloat_FromDouble(v))
  @inline def PyFloat_AsDouble(pyFloat: PyFloat): CDouble = Unmanaged.PyFloat_AsDouble(pyFloat)
  @inline def PyLong_FromLong(v: CLong)(implicit z: PyZone): PyLong = z.manage(Unmanaged.PyLong_FromLong(v))
  @inline def PyLong_AsLong(pyLong: PyLong): Long = Unmanaged.PyLong_AsLong(pyLong)
  @inline def PyBool_FromLong(l: CLong)(implicit z: PyZone): PyBool = z.manage(Unmanaged.PyBool_FromLong(l))
  @inline def PyTuple_New(len: CSize)(implicit z: PyZone): PyTupleAny = z.manage(Unmanaged.PyTuple_New(len))
//  @inline def PyTuple_Pack(size: CSize, elements: CVararg*)(implicit z: PyZone): PyTupleAny = z.manage(Unmanaged.PyTuple_Pack(size, elements: _*))
  @inline def PyTuple_GetItem(tuple: PyTupleAny, elem: CSize): PyObject = Unmanaged.PyTuple_GetItem(tuple, elem)
  @inline def PyTuple_SetItem(tuple: PyTupleAny, elem: CSize, value: PyObject): CInt = Unmanaged.PyTuple_SetItem(tuple, elem, value)
  @inline def PyTuple_Size(tuple: PyTupleAny): CSize = Unmanaged.PyTuple_Size(tuple)
  @inline def PyList_New(len: CSize)(implicit z: PyZone): PyListAny = z.manage(Unmanaged.PyList_New(len))
  @inline def PyList_Size(list: PyListAny): CSize = Unmanaged.PyList_Size(list)
  @inline def PyList_GetItem(list: PyListAny, index: CSize): PyObject = Unmanaged.PyList_GetItem(list, index)
  @inline def PyList_SetItem(list: PyListAny, index: CSize, element: PyObject): CInt = Unmanaged.PyList_SetItem(list, index, element)
  @inline def PyDict_New()(implicit z: PyZone): PyDictAny = z.manage(Unmanaged.PyDict_New())
  @inline def PyDict_SetItem(dict: PyDictAny, key: PyObject, value: PyObject): CInt = Unmanaged.PyDict_SetItem(dict, key, value)
  @inline def PyDict_GetItem(dict: PyDictAny, key: PyObject)(implicit z: PyZone): PyObject = z.manage(Unmanaged.PyDict_GetItem(dict, key))
  @inline def PyDict_Next(dict: PyDictAny, posPtr: Ptr[CSize], keysPtr: Ptr[PyObject], valuePtr: Ptr[PyObject]): CInt = Unmanaged.PyDict_Next(dict, posPtr, keysPtr, valuePtr)
  @inline def Py_DecRef(obj: PyObject): Unit = Unmanaged.Py_DecRef(obj)
}
