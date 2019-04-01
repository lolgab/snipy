package snipy.facades

import snipy._
import snipy.dynamic._

object builtins {
  private val builtins = module("builtins")(PyZone.leakingZone)

  def str(o: PyObject): String = PyZone { implicit z =>
    builtins.str(o).as[PyString].asScala
  }

  def print(o: PyObject): Unit = PyZone { implicit z =>
    builtins.print(o)
  }
}
