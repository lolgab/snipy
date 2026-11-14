package snipy

class DynamicSuite extends munit.FunSuite {
  private val builtins = Python.builtins

  test("module attributes and functions") {
    val math = module("math")
    assertEqualsDouble(math.pi.as[Double], scala.math.Pi, 0.0)
    assertEquals(math.factorial(10).as[Long], 3628800L)
    assertEquals(math.hypot(3, 4).as[Double], 5.0)
  }

  test("method calls on objects") {
    val s = Py("a,b,c")
    assertEquals(s.split(",").as[List[String]], List("a", "b", "c"))
    assertEquals(s.upper().as[String], "A,B,C")
  }

  test("keyword arguments") {
    assertEquals(builtins.sorted(List(3, 1, 2), reverse = true).as[List[Int]], List(3, 2, 1))
    assertEquals(builtins.int("ff", base = 16).as[Int], 255)
    val json = module("json")
    assertEquals(json.dumps(Map("a" -> 1), indent = 2).as[String], "{\n  \"a\": 1\n}")
  }

  test("calling objects directly, with and without keywords") {
    val int = builtins.int
    assertEquals(int("42").as[Int], 42)
    assertEquals(int.applyNamed("" -> "z", "base" -> 36).as[Int], 35)
    val dict = builtins.dict
    assertEquals(dict.applyNamed("a" -> 1).as[Map[String, Int]], Map("a" -> 1))
  }

  test("positional arguments after keywords are rejected") {
    intercept[IllegalArgumentException](builtins.int.applyNamed("base" -> 10, "" -> "1"))
  }

  test("spread arguments of type Seq[Py]") {
    val args = Seq(Py(1), Py(5), Py(2))
    assertEquals(builtins.max(args*).as[Int], 5)
    assertEquals(builtins.range.call(args).as[List[Int]], List(1, 3))
    assertEquals(builtins.callMethod("max", Seq(Py(List(1, 9)))).as[Int], 9)
  }

  test("many arguments") {
    val args = (1 to 50).map(Py(_))
    assertEquals(builtins.max(args*).as[Int], 50)
  }

  test("setting attributes") {
    Python.exec("class Box: pass")
    val box = Python.eval("Box()")
    box.value = 41
    box.label = "answer"
    assertEquals(box.value.as[Int], 41)
    box.setAttr("value", Py(42))
    assertEquals(Python.builtins.getattr(box, "value").as[Int], 42)
    assertEquals(box.label.as[String], "answer")
  }

  test("missing attributes raise AttributeError") {
    val e = intercept[PythonException](Py(1).nope)
    assertEquals(e.pythonType, "AttributeError")
  }

  test("arithmetic operators follow Python semantics") {
    val seven = Py(7)
    assertEquals((seven + 1).as[Int], 8)
    assertEquals((seven - 10).as[Int], -3)
    assertEquals((seven * 3).as[Int], 21)
    assertEquals((seven / 2).as[Double], 3.5)
    assertEquals(seven.floorDiv(2).as[Int], 3)
    assertEquals((Py(-7) % 3).as[Int], 2)
    assertEquals((seven ** 2).as[Int], 49)
    assertEquals((-seven).as[Int], -7)
    assertEquals((+seven).as[Int], 7)
    assertEquals((~seven).as[Int], -8)
    assertEquals((seven << 2).as[Int], 28)
    assertEquals((seven >> 1).as[Int], 3)
    assertEquals((seven & 3).as[Int], 3)
    assertEquals((seven | 8).as[Int], 15)
    assertEquals((seven ^ 1).as[Int], 6)
  }

  test("operators use reflected methods") {
    assertEquals((Py("ab") * 3).as[String], "ababab")
    assertEquals((Py(List(1)) + List(2)).as[List[Int]], List(1, 2))
    assertEquals((Py(1) + Py(2.5)).as[Double], 3.5)
  }

  test("division by zero raises ZeroDivisionError") {
    val e = intercept[PythonException](Py(1) / 0)
    assertEquals(e.pythonType, "ZeroDivisionError")
  }

  test("matrix multiplication dispatches to __matmul__") {
    Python.exec("""
class M:
    def __matmul__(self, other): return ('matmul', other)
""")
    assertEquals(Python.eval("M()").matmul(3).as[(String, Int)], ("matmul", 3))
  }

  test("comparisons") {
    assertEquals((Py(1) < 2).as[Boolean], true)
    assertEquals((Py(1) <= 1).as[Boolean], true)
    assertEquals((Py(1) > 2).as[Boolean], false)
    assertEquals((Py("b") >= "a").as[Boolean], true)
  }

  test("equality and hashing") {
    assertEquals(Py(1), Py(1))
    assertEquals(Py(1), Py(1.0))
    assertNotEquals(Py(1), Py(2))
    assertNotEquals[Any, Any](Py(1), 1)
    assertEquals(Py("k").hashCode, Py("k").hashCode)
    assertEquals(Set(Py("a"), Py("a"), Py("b")).size, 2)
    // unhashable objects fall back to identity hashing
    val l = Py(List(1))
    assertEquals(l.hashCode, l.hashCode)
  }

  test("str and repr") {
    assertEquals(Py("x").toString, "x")
    assertEquals(Py("x").repr, "'x'")
    assertEquals(Py(Map(1 -> List(2))).toString, "{1: [2]}")
  }

  test("subscripting") {
    val d = Py(Map("a" -> 1))
    assertEquals(d.bracketAccess("a").as[Int], 1)
    d.bracketUpdate("b", 2)
    assertEquals(d.as[Map[String, Int]], Map("a" -> 1, "b" -> 2))
    d.bracketDelete("a")
    assertEquals(d.as[Map[String, Int]], Map("b" -> 2))
    val e = intercept[PythonException](d.bracketAccess("zzz"))
    assertEquals(e.pythonType, "KeyError")

    val l = Py(List(10, 20, 30))
    assertEquals(l.bracketAccess(-1).as[Int], 30)
    assertEquals(l.bracketAccess(builtins.slice(1, 3)).as[List[Int]], List(20, 30))
  }

  test("iteration") {
    assertEquals(Python.eval("range(3)").iterator.map(_.as[Int]).toList, List(0, 1, 2))
    assertEquals(Py(List.empty[Int]).iterator.hasNext, false)
    Python.exec("""
def gen():
    yield 1
    raise ValueError('boom')
""")
    val it = Python.eval("gen()").iterator
    assertEquals(it.next().as[Int], 1)
    intercept[PythonException](it.hasNext)
    intercept[PythonException](Py(1).iterator)
  }

  test("None") {
    assert(Python.None.isNone)
    assert(Py(()).isNone)
    assert(!Py(0).isNone)
    assert(Py(List(1)).append(2).isNone)
  }

  test("eval and exec") {
    assertEquals(Python.eval("1 + 2").as[Int], 3)
    Python.exec("snipy_test_value = 21 * 2")
    assertEquals(Python.eval("snipy_test_value").as[Int], 42)
    val globals = Py.dict[String, Int]()
    Python.exec("x = 5\ny = x * 2", globals)
    assertEquals(Python.eval("x + y", globals).as[Int], 15)
    val e = intercept[PythonException](Python.eval("1 +"))
    assertEquals(e.pythonType, "SyntaxError")
  }

  test("building tuples, lists and dicts") {
    assertEquals(Py.tuple(1, "a", 2.0).repr, "(1, 'a', 2.0)")
    assertEquals(Py.list(1, "a", List(2)).repr, "[1, 'a', [2]]")
    assertEquals(Py.tuple().repr, "()")
    assertEquals(Py.dict("a" -> 1, "b" -> 2).repr, "{'a': 1, 'b': 2}")
  }

  test("importing a missing module raises ModuleNotFoundError") {
    val e = intercept[PythonException](module("snipy_does_not_exist"))
    assertEquals(e.pythonType, "ModuleNotFoundError")
    assert(e.is("ImportError"))
  }

  test("interpreter version") {
    assert(Python.version.startsWith("3."), Python.version)
  }
}
