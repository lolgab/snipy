package snipy

import snipy.facades.builtins
import snipy.CApi._

import scala.language.dynamics
import scala.language.implicitConversions
import scala.scalanative.native._

object dynamic {
  def module(name: String)(implicit z: PyZone): Dyn =
    Dyn(PyImport_Import(name.asPython))

  implicit def dynPyObjectToPyObject(dyn: Dyn): PyObject = dyn.o

  implicit def tToPyObject[T](t: T)(implicit z: PyZone, asPy: AsPython[T, PyObject]): Dyn = asPy.asPython(t)

  implicit class Dyn(val o: PyObject) extends AnyVal with Dynamic {
    def selectDynamic(attr: String)(implicit z: PyZone): Dyn = {
      val s = stackalloc[Byte](attr.getBytes().length + 1)
      val zone = new Zone {
        override def alloc(size: CSize): Ptr[Byte] = s
      }
      Dyn(PyObject_GetAttrString(o, toCString(attr)(zone)))
    }

    def applyDynamic(name: String)(args: Dyn*)(implicit z: PyZone): Dyn = {
      val func = selectDynamic(name)
      val res = args.length match {
        case 0 =>
          z.manage(Unmanaged.PyObject_CallFunctionObjArgs(func, 0))
        case 1 =>
          z.manage(Unmanaged.PyObject_CallFunctionObjArgs(func, args(0).o, 0))
        case 2 =>
          z.manage(Unmanaged.PyObject_CallFunctionObjArgs(func, args(0).o, args(1).o, 0))
        case 3 =>
          z.manage(Unmanaged.PyObject_CallFunctionObjArgs(func, args(0).o, args(1).o, args(2).o, 0))
        case 4 =>
          z.manage(Unmanaged.PyObject_CallFunctionObjArgs(func, args(0).o, args(1).o, args(2).o, args(3).o, 0))
        case 5 =>
          z.manage(Unmanaged.PyObject_CallFunctionObjArgs(func, args(0).o, args(1).o, args(2).o, args(3).o, args(4).o, 0))
        case 6 =>
          z.manage(Unmanaged.PyObject_CallFunctionObjArgs(func, args(0).o, args(1).o, args(2).o, args(3).o, args(4).o, args(5).o, 0))
        case 7 =>
          z.manage(Unmanaged.PyObject_CallFunctionObjArgs(func, args(0).o, args(1).o, args(2).o, args(3).o, args(4).o, args(5).o, args(6).o, 0))
        case 8 =>
          z.manage(Unmanaged.PyObject_CallFunctionObjArgs(func, args(0).o, args(1).o, args(2).o, args(3).o, args(4).o, args(5).o, args(6).o, args(7).o, 0))
        case 9 =>
          z.manage(Unmanaged.PyObject_CallFunctionObjArgs(func, args(0).o, args(1).o, args(2).o, args(3).o, args(4).o, args(5).o, args(6).o, args(7).o, args(8).o, 0))
        case 10 =>
          z.manage(Unmanaged.PyObject_CallFunctionObjArgs(func, args(0).o, args(1).o, args(2).o, args(3).o, args(4).o, args(5).o, args(6).o, args(7).o, args(8).o, args(9).o, 0))
        case _ =>
          var i: Int = 0
          val tuple = PyTuple_New(args.length)
          args.foreach { arg =>
            PyTuple_SetItem(tuple, i, arg.o)
            i = i + 1
          }
          PyObject_CallObject(func, tuple)
      }
      Dyn(res)
    }

    def applyDynamicNamed(name: String)(args: (String, Dyn)*)(implicit z: PyZone): Dyn = {
      val func = selectDynamic(name)
      val argsNum = args.count(_._1.isEmpty)
      val kwargs = PyDict_New()
      val argsTuple = if(argsNum == 0) PyNone else PyTuple_New(argsNum)
      var argsTupleIndex = 0
      for((name, value) <- args)
        if(name.isEmpty) {
          PyTuple_SetItem(argsTuple, argsTupleIndex, value)
          argsTupleIndex = argsTupleIndex + 1
        }
        else
          PyDict_SetItem(kwargs, name.asPython, value)
      Dyn(PyObject_Call(func, argsTuple, kwargs))
    }

    def +(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__add__(other)
    def -(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__sub__(other)
    def *(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__mul__(other)
    def `//`(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__floordiv__(other)
    def /(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__div__(other)
    def %(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__mod__(other)
    def divmod(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__divmod__(other)
    def **(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__pow__(other)
    def <<(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__lshift__(other)
    def >>(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__rshift__(other)
    def &(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__and__(other)
    def |(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__or__(other)
    def ^(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__xor__(other)
    // def +=(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__iadd__(other)
    // def -=(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__isub__(other)
    // def *=(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__imul__(other)
    // def `//=`(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__ifloordiv__(other)
    // def /=(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__idiv__(other)
    // def %=(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__imod__(other)
    // def divmod_=(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__idivmod__(other)
    // def **=(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__ipow__(other)
    // def <<=(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__ilshift__(other)
    // def >>=(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__irshift__(other)
    // def &=(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__iand__(other)
    // def |=(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__ior__(other)
    // def ^=(other: PyObject)(implicit z: PyZone): Dyn = Dyn(o).__ixor__(other)

    def as[U <: Ptr[Byte]]: U = o.asInstanceOf[U]
    def as[U <: PyObject, T](implicit asSc: AsScala[U, T]): T = o.asInstanceOf[U].asScala[T]

    override def toString: String = builtins.str(o)
  }
}
