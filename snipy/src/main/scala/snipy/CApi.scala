package snipy

import scala.scalanative.unsafe.*

/** Raw bindings to the CPython C API.
 *
 *  Every function returning `PyPtr` follows CPython's ownership rules: unless
 *  stated otherwise ("borrowed") the caller owns the returned reference and
 *  must release it. All of them, except the GIL functions, must be called while
 *  holding the GIL.
 *
 *  Functions that can run Python code are `@blocking`: Python may release the
 *  GIL and wait for it inside them, and the Scala Native GC must not wait for a
 *  thread stuck there.
 */
@extern
private[snipy] object CApi {
  type PyPtr = Ptr[Byte]

  /** `PyMethodDef`: name, C function, flags, docstring. */
  type PyMethodDef = CStruct4[CString, CFuncPtr2[PyPtr, PyPtr, PyPtr], CInt, CString]

  // Interpreter and GIL
  def Py_IsInitialized(): CInt = extern
  @blocking
  def Py_InitializeEx(initsigs: CInt): Unit = extern
  @blocking
  def PyEval_SaveThread(): Ptr[Byte] = extern
  @blocking
  def PyGILState_Ensure(): CInt = extern
  @blocking
  def PyGILState_Release(state: CInt): Unit = extern

  // Reference counting
  def Py_IncRef(o: PyPtr): Unit = extern
  @blocking
  def Py_DecRef(o: PyPtr): Unit = extern

  // Errors
  def PyErr_Occurred(): PyPtr = extern // borrowed
  def PyErr_GetRaisedException(): PyPtr = extern
  def PyErr_SetRaisedException(exc: PyPtr): Unit = extern // steals
  def PyErr_Clear(): Unit = extern

  // Modules and code
  @blocking
  def PyImport_ImportModule(name: CString): PyPtr = extern
  def PyModule_GetDict(module: PyPtr): PyPtr = extern // borrowed
  @blocking
  def PyRun_String(code: CString, start: CInt, globals: PyPtr, locals: PyPtr): PyPtr = extern

  // Generic objects
  @blocking
  def PyObject_GetAttr(o: PyPtr, name: PyPtr): PyPtr = extern
  @blocking
  def PyObject_SetAttr(o: PyPtr, name: PyPtr, value: PyPtr): CInt = extern
  @blocking
  def PyObject_GetItem(o: PyPtr, key: PyPtr): PyPtr = extern
  @blocking
  def PyObject_SetItem(o: PyPtr, key: PyPtr, value: PyPtr): CInt = extern
  @blocking
  def PyObject_DelItem(o: PyPtr, key: PyPtr): CInt = extern
  @blocking
  def PyObject_Str(o: PyPtr): PyPtr = extern
  @blocking
  def PyObject_Repr(o: PyPtr): PyPtr = extern
  @blocking
  def PyObject_RichCompare(a: PyPtr, b: PyPtr, op: CInt): PyPtr = extern
  @blocking
  def PyObject_RichCompareBool(a: PyPtr, b: PyPtr, op: CInt): CInt = extern
  @blocking
  def PyObject_Hash(o: PyPtr): CSSize = extern
  @blocking
  def PyObject_IsTrue(o: PyPtr): CInt = extern
  @blocking
  def PyObject_IsInstance(o: PyPtr, cls: PyPtr): CInt = extern
  @blocking
  def PyObject_Type(o: PyPtr): PyPtr = extern
  @blocking
  def PyObject_GetIter(o: PyPtr): PyPtr = extern
  @blocking
  def PyIter_Next(it: PyPtr): PyPtr = extern
  @blocking
  def PyObject_Vectorcall(callable: PyPtr, args: Ptr[PyPtr], nargsf: CSize, kwnames: PyPtr): PyPtr =
    extern
  @blocking
  def PyObject_VectorcallMethod(
      name: PyPtr,
      args: Ptr[PyPtr],
      nargsf: CSize,
      kwnames: PyPtr
  ): PyPtr =
    extern

  // Numbers
  @blocking
  def PyNumber_Add(a: PyPtr, b: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_Subtract(a: PyPtr, b: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_Multiply(a: PyPtr, b: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_MatrixMultiply(a: PyPtr, b: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_TrueDivide(a: PyPtr, b: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_FloorDivide(a: PyPtr, b: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_Remainder(a: PyPtr, b: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_Power(a: PyPtr, b: PyPtr, mod: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_Lshift(a: PyPtr, b: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_Rshift(a: PyPtr, b: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_And(a: PyPtr, b: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_Or(a: PyPtr, b: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_Xor(a: PyPtr, b: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_Negative(a: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_Positive(a: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_Invert(a: PyPtr): PyPtr = extern
  @blocking
  def PyNumber_Long(a: PyPtr): PyPtr = extern

  // Primitive types
  def PyLong_FromLongLong(v: CLongLong): PyPtr = extern
  @blocking
  def PyLong_AsLongLong(o: PyPtr): CLongLong = extern
  def PyLong_FromString(str: CString, pend: Ptr[CString], base: CInt): PyPtr = extern
  def PyFloat_FromDouble(v: CDouble): PyPtr = extern
  @blocking
  def PyFloat_AsDouble(o: PyPtr): CDouble = extern
  def PyBool_FromLong(v: CLong): PyPtr = extern
  def PyUnicode_FromStringAndSize(str: CString, size: CSSize): PyPtr = extern
  @blocking
  def PyUnicode_AsUTF8AndSize(o: PyPtr, size: Ptr[CSSize]): CString = extern
  def PyUnicode_InternFromString(str: CString): PyPtr = extern
  def PyBytes_FromStringAndSize(str: CString, size: CSSize): PyPtr = extern
  @blocking
  def PyBytes_AsStringAndSize(o: PyPtr, buffer: Ptr[CString], size: Ptr[CSSize]): CInt = extern

  // Containers
  def PyTuple_New(size: CSSize): PyPtr = extern
  def PyTuple_SetItem(t: PyPtr, i: CSSize, v: PyPtr): CInt = extern // steals v
  def PyTuple_GetItem(t: PyPtr, i: CSSize): PyPtr = extern // borrowed
  def PyTuple_Size(t: PyPtr): CSSize = extern
  @blocking
  def PySequence_Tuple(o: PyPtr): PyPtr = extern
  def PyList_New(size: CSSize): PyPtr = extern
  def PyList_SetItem(l: PyPtr, i: CSSize, v: PyPtr): CInt = extern // steals v
  def PyDict_New(): PyPtr = extern
  @blocking
  def PyDict_SetItem(d: PyPtr, k: PyPtr, v: PyPtr): CInt = extern
  @blocking
  def PyMapping_Items(o: PyPtr): PyPtr = extern
  @blocking
  def PySet_New(iterable: PyPtr): PyPtr = extern
  @blocking
  def PySet_Add(s: PyPtr, key: PyPtr): CInt = extern

  // Scala callbacks
  def PyCFunction_NewEx(ml: Ptr[PyMethodDef], self: PyPtr, module: PyPtr): PyPtr = extern
  def PyCapsule_New(pointer: Ptr[Byte], name: CString, destructor: CFuncPtr1[PyPtr, Unit]): PyPtr =
    extern
  def PyCapsule_GetPointer(capsule: PyPtr, name: CString): Ptr[Byte] = extern

  // Scala Native GC, to re-enter managed code from Python callbacks
  @name("scalanative_GC_set_mutator_thread_state")
  def scalanative_GC_set_mutator_thread_state(state: CInt): Unit = extern
  @name("scalanative_GC_yield")
  def scalanative_GC_yield(): Unit = extern
}
