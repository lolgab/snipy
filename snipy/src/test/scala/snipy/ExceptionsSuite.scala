package snipy

class ExceptionsSuite extends munit.FunSuite {

  test("Python exceptions carry type, message and traceback") {
    Python.exec("""
def snipy_fail(n):
    raise ValueError(f"bad value {n}")
""")
    val e = intercept[PythonException](Python.eval("snipy_fail").apply(3))
    assertEquals(e.pythonType, "ValueError")
    assertEquals(e.pythonMessage, "bad value 3")
    assertEquals(e.getMessage, "ValueError: bad value 3")
    assert(e.pythonTraceback.contains("snipy_fail"), e.pythonTraceback)
    assert(e.pythonTraceback.contains("ValueError: bad value 3"), e.pythonTraceback)
    assertEquals(e.value.args.as[Tuple1[String]], Tuple1("bad value 3"))
  }

  test("exception types from other modules are qualified") {
    val json = module("json")
    val e = intercept[PythonException](json.loads("{"))
    assertEquals(e.pythonType, "json.decoder.JSONDecodeError")
    assert(e.is("ValueError"))
    assert(!e.is("KeyError"))
  }

  test("exceptions without a message") {
    val e = intercept[PythonException](Python.exec("raise KeyboardInterrupt"))
    assertEquals(e.getMessage, "KeyboardInterrupt")
    assert(e.is("BaseException"))
  }

  test("the interpreter keeps working after an exception") {
    intercept[PythonException](Python.eval("1/0"))
    assertEquals(Python.eval("1 + 1").as[Int], 2)
  }

  test("exceptions can be caught and inspected as Python objects") {
    val e = intercept[PythonException](Python.eval("int('x')"))
    assertEquals(Python.builtins.`type`(e.value).__name__.as[String], "ValueError")
  }
}
