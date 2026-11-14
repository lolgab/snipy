package examples

import snipy.*

/** A quick tour of snipy. Run with `sbt examples/run`. */
object Tour {
  def main(args: Array[String]): Unit = {
    println(s"Python ${Python.version.takeWhile(_ != ' ')}")

    // Modules, attributes and calls
    val math = module("math")
    println(s"sqrt(2) = ${math.sqrt(2).as[Double]}")

    // Keyword arguments and collections
    val json = module("json")
    // values of different types: convert each to Py
    val doc = Map("name" -> Py("snipy"), "tags" -> Py(List("scala", "python")))
    val text = json.dumps(doc, indent = 2)
    println(text.as[String])
    val parsed = json.loads("""{"xs": [1, 2, 3]}""").as[Map[String, List[Int]]]
    println(s"parsed: $parsed")

    // Operators follow Python semantics
    val big = Py(2) ** 100
    println(s"2 ** 100 = $big")

    // Scala functions are Python callables
    val words = List("banana", "Kiwi", "apple")
    val sorted = Python.builtins.sorted(words, key = (s: String) => s.toLowerCase)
    println(s"sorted: ${sorted.as[List[String]]}")

    // Python exceptions become PythonException
    try json.loads("{oops")
    catch { case e: PythonException => println(s"caught ${e.pythonType}: ${e.pythonMessage}") }

    // Typed facades
    import facades.fractions.Fraction
    val third = Fraction(1, 3)
    println(s"1/3 + 1/6 = ${third + Fraction(1, 6)}")

    // Release temporaries deterministically in hot loops
    var total = 0.0
    for (i <- 1 to 10000) Py.local(total += math.log1p(i.toDouble).as[Double])
    println(f"sum of log1p = $total%.3f")
  }
}
