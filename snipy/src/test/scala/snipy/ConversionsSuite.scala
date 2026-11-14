package snipy

class ConversionsSuite extends munit.FunSuite {

  private def roundTrip[T](value: T)(using AsPython[T], AsScala[T]): Unit =
    assertEquals(Py(value).as[T], value)

  test("primitives round-trip") {
    roundTrip(true)
    roundTrip(false)
    roundTrip(0)
    roundTrip(Int.MinValue)
    roundTrip(Int.MaxValue)
    roundTrip(Long.MinValue)
    roundTrip(Long.MaxValue)
    roundTrip(42.toShort)
    roundTrip((-7).toByte)
    roundTrip(3.25)
    roundTrip(Double.MaxValue)
    roundTrip(Double.PositiveInfinity)
    roundTrip(1.5f)
    roundTrip('x')
    roundTrip(())
  }

  test("NaN round-trips") {
    assert(Py(Double.NaN).as[Double].isNaN)
  }

  test("strings round-trip, including non-ASCII and NUL characters") {
    roundTrip("")
    roundTrip("hello")
    roundTrip("àèìòù – 日本語 – 🐍🦀")
    roundTrip("nul\u0000inside")
  }

  test("strings have the right Python length") {
    assertEquals(Python.builtins.len("🐍é").as[Int], 2)
  }

  test("BigInt round-trips beyond 64 bits") {
    roundTrip(BigInt(0))
    roundTrip(BigInt("-123456789012345678901234567890123456789"))
    roundTrip(BigInt(2).pow(200))
    assertEquals((Py(BigInt(2)) ** 100).as[BigInt], BigInt(2).pow(100))
  }

  test("Python types of converted values") {
    def typeName(py: Py): String = Python.builtins.`type`(py).__name__.as[String]
    assertEquals(typeName(Py(1)), "int")
    assertEquals(typeName(Py(1L)), "int")
    assertEquals(typeName(Py(1.0)), "float")
    assertEquals(typeName(Py(true)), "bool")
    assertEquals(typeName(Py("s")), "str")
    assertEquals(typeName(Py(Array[Byte](1, 2))), "bytes")
    assertEquals(typeName(Py(List(1))), "list")
    assertEquals(typeName(Py(Vector(1))), "list")
    assertEquals(typeName(Py(Array(1, 2))), "list")
    assertEquals(typeName(Py(Set(1))), "set")
    assertEquals(typeName(Py(Map(1 -> 2))), "dict")
    assertEquals(typeName(Py((1, "a"))), "tuple")
    assertEquals(typeName(Py(())), "NoneType")
    assertEquals(typeName(Py(Option.empty[Int])), "NoneType")
    assertEquals(typeName(Py(Option(3))), "int")
  }

  test("bytes round-trip") {
    val bytes = Array[Byte](0, 1, -1, 127, -128)
    assertEquals(Py(bytes).as[Array[Byte]].toSeq, bytes.toSeq)
    assertEquals(Py(Array.emptyByteArray).as[Array[Byte]].toSeq, Seq.empty)
  }

  test("collections round-trip") {
    roundTrip(List(1, 2, 3))
    roundTrip(List.empty[Int])
    roundTrip(Vector("a", "b"))
    roundTrip(Seq(1.5, 2.5))
    roundTrip(Set(1, 2, 3))
    roundTrip(Map("a" -> 1, "b" -> 2))
    roundTrip(Map.empty[String, Int])
    roundTrip(List(List(1, 2), List(3)))
    roundTrip(Map("xs" -> List(Option(1), None)))
  }

  test("tuples round-trip") {
    roundTrip(Tuple1(1))
    roundTrip((1, "a"))
    roundTrip((1, "a", 2.5, true))
    roundTrip((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23))
    roundTrip(List((1, "one"), (2, "two")))
  }

  test("options") {
    roundTrip(Option(1))
    roundTrip(Option.empty[String])
    assertEquals(Python.None.as[Option[Int]], None)
  }

  test("sequences accept any Python iterable") {
    assertEquals(Python.eval("range(4)").as[List[Int]], List(0, 1, 2, 3))
    assertEquals(Python.eval("(1, 2)").as[Vector[Int]], Vector(1, 2))
    assertEquals(Python.eval("'abc'").as[List[String]], List("a", "b", "c"))
    assertEquals(Python.eval("{3, 3, 4}").as[Set[Int]], Set(3, 4))
  }

  test("tuples accept lists of the right length") {
    assertEquals(Python.eval("[1, 'x']").as[(Int, String)], (1, "x"))
    intercept[IllegalArgumentException](Python.eval("[1, 2, 3]").as[(Int, Int)])
  }

  test("maps accept any mapping") {
    Python.exec("import collections")
    val od = Python.eval("collections.OrderedDict([('a', 1), ('b', 2)])")
    assertEquals(od.as[Map[String, Int]], Map("a" -> 1, "b" -> 2))
  }

  test("booleans follow Python truthiness") {
    assertEquals(Py(0).as[Boolean], false)
    assertEquals(Py(List(1)).as[Boolean], true)
    assertEquals(Py("").as[Boolean], false)
    assertEquals(Python.None.as[Boolean], false)
  }

  test("ints convert to Double and integral floats do not convert to Long") {
    assertEquals(Py(3).as[Double], 3.0)
    val e = intercept[PythonException](Py(3.0).as[Long])
    assertEquals(e.pythonType, "TypeError")
  }

  test("out of range numbers fail") {
    intercept[ArithmeticException](Py(Long.MaxValue).as[Int])
    intercept[ArithmeticException](Py(300).as[Byte])
    val e = intercept[PythonException](Py(BigInt(2).pow(70)).as[Long])
    assertEquals(e.pythonType, "OverflowError")
  }

  test("wrong types fail with a Python TypeError") {
    val e = intercept[PythonException](Py(1).as[String])
    assertEquals(e.pythonType, "TypeError")
    intercept[PythonException](Py("x").as[Int])
    intercept[PythonException](Py(1).as[List[Int]])
    intercept[PythonException](Py("x").as[Array[Byte]])
    intercept[IllegalArgumentException](Py("xy").as[Char])
  }

  test("Py converts to itself") {
    val py = Py(List(1, 2))
    assertEquals(Py(py), py)
    assertEquals(py.as[Py], py)
  }

  test("custom conversions with contramap and map") {
    final case class Meters(value: Double)
    given AsPython[Meters] = AsPython[Double].contramap(_.value)
    given AsScala[Meters] = AsScala[Double].map(Meters(_))
    roundTrip(Meters(1.5))
    assertEquals(module("math").floor(Meters(2.7)).as[Meters], Meters(2.0))
  }

  test("asPython extension") {
    assertEquals(List(1, 2).asPython.repr, "[1, 2]")
  }
}
