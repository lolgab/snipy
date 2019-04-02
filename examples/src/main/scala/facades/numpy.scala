package snipy.facades

import snipy._
import snipy.dynamic._

import scala.scalanative.native.Ptr

object numpy {
  private[numpy] trait _ndarray
  type ndarray = PyObject with _ndarray

  private val np = module("numpy")(PyZone.leakingZone)

  def array[T](lists: T)(implicit z: PyZone, asPython: AsPython[T, PyListAny]): ndarray =
    np.array(lists.asPython).as[ndarray]

  def log10(array: ndarray)(implicit z: PyZone): ndarray = {
    np.log10(array).as[ndarray]
  }

  implicit class ndarrayOps(val o: ndarray) extends AnyVal {
    def ndim: Long = PyZone { implicit z =>
      Dyn(o).ndim.as[PyLong].asScala
    }

    def shape: Seq[Long] = PyZone { implicit z =>
      Dyn(o).shape.as[PyTuple[PyLong]].asScala
    }

    def size: Long = PyZone { implicit z =>
      Dyn(o).size.as[PyLong].asScala
    }

    def data: Ptr[Byte] = PyZone { implicit z =>
      Dyn(o).data.as[Ptr[Byte]]
    }
  }
}
