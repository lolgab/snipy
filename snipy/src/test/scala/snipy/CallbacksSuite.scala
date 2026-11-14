package snipy

class CallbacksSuite extends munit.FunSuite {
  private val builtins = Python.builtins

  test("Scala functions as Python callables") {
    val double = (x: Int) => x * 2
    assertEquals(builtins.list(builtins.map(double, List(1, 2, 3))).as[List[Int]], List(2, 4, 6))
  }

  test("functions of different arities") {
    val zero = () => "called"
    assertEquals(Py(zero).apply().as[String], "called")

    val add = (a: Int, b: Int) => a + b
    assertEquals(module("functools").reduce(add, List(1, 2, 3, 4)).as[Int], 10)

    val three = (a: String, b: Int, c: Boolean) => s"$a$b$c"
    assertEquals(Py(three)("x", 1, true).as[String], "x1true")
  }

  test("functions as keyword arguments") {
    val byLength = (s: String) => s.length
    val sorted = builtins.sorted(List("ccc", "a", "bb"), key = byLength)
    assertEquals(sorted.as[List[String]], List("a", "bb", "ccc"))
  }

  test("functions receiving and returning Py") {
    val f = Py.function(args => args.head.upper())
    assertEquals(f("abc").as[String], "ABC")
    val variadic = Py.function(args => Py(args.length))
    assertEquals(variadic(1, 2, 3, 4).as[Int], 4)
  }

  test("returning Unit gives None") {
    var seen = List.empty[Int]
    val record = (x: Int) => { seen = x :: seen }
    assert(Py(record)(5).isNone)
    assertEquals(seen, List(5))
  }

  test("wrong number of arguments raises TypeError in Python") {
    val f = (x: Int) => x
    val e = intercept[PythonException](Py(f)(1, 2))
    assertEquals(e.pythonType, "TypeError")
  }

  test("Scala exceptions become Python RuntimeErrors") {
    val boom: Int => Int = _ => throw new IllegalStateException("boom")
    Python.exec("""
def snipy_call_and_catch(f):
    try:
        f(1)
    except RuntimeError as e:
        return str(e)
""")
    val message = Python.eval("snipy_call_and_catch")(boom).as[String]
    assertEquals(message, "java.lang.IllegalStateException: boom")
    val e = intercept[PythonException](Py(boom)(1))
    assertEquals(e.pythonType, "RuntimeError")
  }

  test("Python exceptions raised in nested calls keep their type") {
    val parse = (s: String) => Python.builtins.int(s).as[Int]
    val e = intercept[PythonException](builtins.list(builtins.map(parse, List("1", "x"))))
    assertEquals(e.pythonType, "ValueError")
  }

  test("callbacks are released when Python drops them") {
    val before = Callbacks.liveCount
    Py.local {
      (1 to 100).foreach(i => Py((x: Int) => x + i)(1))
    }
    assertEquals(Callbacks.liveCount, before)
  }
}
