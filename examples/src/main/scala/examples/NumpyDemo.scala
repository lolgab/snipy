package examples

import snipy.*

/** Requires numpy: `pip install numpy`, then `sbt "examples/runMain examples.NumpyDemo"`. */
object NumpyDemo {
  def main(args: Array[String]): Unit = {
    val np = module("numpy")
    val a = np.arange(6).reshape(2, 3)
    val b = np.ones(Py.tuple(3, 2))
    println(a.matmul(b))
    println(s"mean = ${a.mean().as[Double]}, shape = ${a.shape.as[(Int, Int)]}")
    val doubled = (a * 2).tolist().as[List[List[Int]]]
    println(s"doubled = $doubled")
  }
}
