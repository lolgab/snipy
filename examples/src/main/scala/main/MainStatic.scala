package main

import snipy._
import snipy.facades.{builtins, numpy => np}

object MainStatic {
  def main(args: Array[String]): Unit = PyZone { implicit z =>
    val array: np.ndarray = np.array(List(1,2,3,4))
    val logArray = np.log10(array)
    builtins.print(logArray)
  }
}
