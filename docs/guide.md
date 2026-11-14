# Guide

Everything lives in one import:

```scala
import snipy.*
```

## Python objects: `Py`

A `Py` is a reference to a Python object. Get one by importing a module, converting a Scala value, or evaluating code:

```scala
val math = module("math")        // import math
val xs = Py(List(1, 2, 3))       // [1, 2, 3]
val n = Python.eval("2 ** 64")   // any Python expression
Python.exec("""
def greet(name):
    return f"Hello, {name}!"
""")
val greet = Python.eval("greet")
```

`Python.builtins` is the `builtins` module, `Python.None` is `None`, and `Python.version` is the interpreter's `sys.version`.

## Attributes and calls

Attributes, methods and calls are written as in Python:

```scala
math.pi                                   // attribute
math.factorial(10)                        // method call
greet("Scala")                            // calling an object
Python.builtins.sorted(xs, reverse = true) // keyword arguments
box.label = "new"                         // setattr
```

Arguments are converted at compile time with their type's `AsPython` instance. An argument with no conversion is a compile error, not a run-time surprise:

```scala
math.sqrt(new Object)
// error: snipy: no AsPython[Object] instance to convert this argument to Python
```

When the values of a collection have different types, convert each one to `Py`:

```scala
json.dumps(Map("name" -> Py("snipy"), "tags" -> Py(List("scala", "python"))))
```

`applyNamed` calls an object with keyword arguments (`f.applyNamed("x" -> 1)`); an empty name passes the value positionally.

### Spreading arguments

A `Seq[Py]` can be spread into a call:

```scala
val args = Seq(Py(1), Py(5), Py(2))
Python.builtins.max(args*)
```

`call`, `callNamed`, `callMethod` and `callMethodNamed` take already converted arguments and are handy when building calls programmatically.

### Name clashes

The few members defined on `Py` (`as`, `apply`, `applyNamed`, `call*`, `dispose`, `isDisposed`, `isNone`, `iterator`, `repr`, `setAttr`, `bracket*`, `floorDiv`, `matmul`) shadow Python attributes of the same name. Reach those explicitly:

```scala
df.selectDynamic("apply")             // the attribute
df.callMethod("apply", Seq(Py(f)))    // the method
```

Scala keywords need backticks: ``Python.builtins.`type`(x)``.

## Converting back: `as[T]`

```scala
math.sqrt(2).as[Double]
json.loads("[1, 2]").as[List[Int]]
Python.eval("{'a': (1, 'x')}").as[Map[String, (Int, String)]]
```

A Python value that doesn't fit throws: a `PythonException` with `TypeError` or `OverflowError` when Python refuses the conversion, `ArithmeticException` when an int doesn't fit a narrower Scala type, `IllegalArgumentException` for tuples of the wrong length.

### Built-in conversions

| Scala | to Python | from Python |
|---|---|---|
| `Boolean` | `bool` | any object, by truthiness |
| `Byte`, `Short`, `Int`, `Long` | `int` | `int` (or `__index__`), range-checked |
| `BigInt` | `int` | `int` of any size |
| `Float`, `Double` | `float` | `float`, `int` |
| `Char`, `String` | `str` | `str` |
| `Array[Byte]` | `bytes` | `bytes` |
| `Unit` | `None` | anything (ignored) |
| `Option[T]` | `None` or the value | `None` or the value |
| `Seq[T]`, `List[T]`, `Vector[T]`, any `Iterable[T]`, `Array[T]` | `list` | any iterable (as `Seq`, `List`, `Vector`) |
| `Set[T]` | `set` | any iterable |
| `Map[K, V]` | `dict` | any mapping |
| tuples of any size | `tuple` | any sequence of the right length |
| functions of 0 to 3 arguments | callable | — |
| `Py` | itself | itself |

### Your own conversions

Derive from an existing instance:

```scala
final case class Meters(value: Double)

given AsPython[Meters] = AsPython[Double].contramap(_.value)
given AsScala[Meters] = AsScala[Double].map(Meters(_))
```

Or implement the traits directly. `AsPython.newReference` and `AsScala.fromPython` work on raw `PyObject*` pointers and run with the GIL held; most conversions are easier to write on top of existing instances.

## Operators

Operators follow Python semantics, including reflected methods (`__radd__` and friends):

```scala
Py(7) / 2          // 3.5 (true division)
Py(7).floorDiv(2)  // 3
Py(-7) % 3         // 2
Py(2) ** 100       // arbitrary precision
Py("ab") * 3       // 'ababab'
a.matmul(b)        // a @ b
Py(1) < 2          // Python's True, as a Py
```

`==` on two `Py` is Python's `==`, and `hashCode` is Python's `hash` (identity for unhashable objects), so `Py` works as a key in Scala collections.

## Subscripting and iteration

```scala
d.bracketAccess("key")         // d["key"]
d.bracketUpdate("key", 1)      // d["key"] = 1
d.bracketDelete("key")         // del d["key"]
l.bracketAccess(Python.builtins.slice(1, 3)) // l[1:3]

for (item <- Python.eval("range(3)").iterator) println(item)
```

## Building containers

```scala
Py.tuple(1, "a", 2.0)       // (1, 'a', 2.0) with mixed types
Py.list(1, "a", List(2))    // [1, 'a', [2]]
Py.dict("a" -> 1, "b" -> 2) // {'a': 1, 'b': 2}
```

## Scala functions in Python

Functions with up to three parameters convert to Python callables, so they can be passed wherever Python expects one:

```scala
val byLength = (s: String) => s.length
Python.builtins.sorted(List("ccc", "a", "bb"), key = byLength)

module("functools").reduce((a: Int, b: Int) => a + b, List(1, 2, 3))
```

`Py.function` takes any number of `Py` arguments:

```scala
val count = Py.function(args => Py(args.length))
```

A Scala exception thrown inside the function reaches Python as a `RuntimeError`; a `PythonException` is re-raised as the original Python exception. The Scala closure is released when Python drops the callable.

Callbacks must be invoked on threads started by Scala Native, which is the case for all Python code you call from Scala, but not for threads created by Python's `threading` module.

## Exceptions

A Python exception surfaces as a `snipy.PythonException`:

```scala
try json.loads("{")
catch {
  case e: PythonException =>
    e.pythonType      // "json.decoder.JSONDecodeError"
    e.pythonMessage   // "Expecting property name enclosed in double quotes: ..."
    e.pythonTraceback // formatted like Python prints it
    e.is("ValueError") // isinstance check against a builtin class
    e.value           // the exception object, as a Py
}
```

## Typed facades

Dynamic calls are concise, but a typed facade documents what a library returns and keeps conversions in one place. An opaque type over `Py` gives a Python class its own Scala type without allocating a wrapper:

```scala
object fractions {
  private lazy val module = Py.global(snipy.module("fractions"))

  opaque type Fraction = Py

  object Fraction {
    def apply(numerator: Long, denominator: Long): Fraction =
      module.Fraction(numerator, denominator)
  }

  extension (f: Fraction) {
    // Inside this object Fraction is Py, so `f.numerator` would call this very
    // extension: reach the Python attribute with selectDynamic.
    def numerator: Long = f.selectDynamic("numerator").as[Long]
    def +(other: Fraction): Fraction = (f: Py) + (other: Py)
  }
}
```

Store modules in `lazy val`s wrapped in `Py.global`, so they survive being first used inside a `Py.local` block (see [memory management](memory.md)).

## Threads

Python can be used from any Scala Native thread. Every snipy operation takes the GIL for its own duration; Python code that releases the GIL, like I/O or `time.sleep`, lets other threads run.

To run many small operations in a row, take the GIL once:

```scala
val total = Python.withGil {
  (1 to 1000).map(Py(_)).reduce(_ + _).as[Int]
}
```

Next: [memory management](memory.md).
