package examples.facades

import snipy._
import snipy.dynamic._

object math {
  private val math = module("math")(PyZone.leakingZone)

  def log(d: Double): Double = PyZone { implicit z =>
    math.log(d).as[PyFloat].asScala
  }
}