package snipy

/** Imports a Python module: `module("math")` is Python's `import math`. */
def module(name: String): Py = Python.module(name)

extension [T](value: T) {

  /** Converts a Scala value to Python. */
  def asPython(using conv: AsPython[T]): Py = conv.asPython(value)
}
