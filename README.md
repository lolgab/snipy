# SNiPY
Snipy is a bridge to call Python code from [Scala Native](https://scala-native.org).
It interfaces directly with [Python C Api](https://docs.python.org/3.7/c-api/index.html) without ffi at (almost) zero cost.

## Getting Started

```scala
import snipy._         // import to enable snipy implicits
import snipy.dynamic._ // enables dynamic interface

object Main {
  def main(args: Array[String]): Unit = PyZone { implicit z => // You need to wrap Python call in a PyZone
                                                               // when zone block ends every PyObject is
                                                               // no more usable
    val math = module("math")  // equals to python's "import math"
    val res = math.log10(2.0)  // calls dynamically the log10 function passing a Double converted to PyObject
    println(res)               // calls the "str" python function as toString.
  }
}
```

## How it works
The conversions are managed by two typeclasses:
```scala
trait AsPython[T, U <: PyObject] {
  def asPython(t: T)(implicit z: PyZone): U
}
```
and
```scala
trait AsScala[T <: PyObject, U] {
  def asScala(t: T): U
}
```
You can implement new instances to convert directly from a Python type to a Scala Type without using default types' provided instances.

## Writing facades
You can define a hierarchy for python types this way:
```scala
object myfacade {
  private[myfacade] trait _Foo
  type Foo = PyObject with _Foo

  private[myfacade] trait _Bar
  type Bar = Foo with _Bar
```
This way you can say that `Bar extends Foo`.

You need to define a module. Not having a PyZone at facade site you can use the default leaking zone where Python objects are never freed:
```scala
val pythonLib = module("pythonLib")(PyZone.leakingZone)
```
now you can define a Scala function for every python function you need:
```scala
def pyFunction(foo: String): List[Int] = PyZone { implicit z =>  // You can define a local PyZone because you don't
                                                                 // need the intermediate Python Objects (result is a scala type)
  Dyn(pythonLib).pyFunction(foo.asPython).as[PyList[Int]].asScala // the as method casts to a specific python type
}                                                                 // so .asScala can call the correct typeclass instance
```

Then for every class you have to define an implicit class:
```scala
implicit class MyPyObjectOps(val o: MyPyObject) extends AnyVal {
  def function(): Unit = PyZone { implicit z =>
    Dyn(o).function()
  }
}
```
