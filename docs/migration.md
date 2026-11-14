# Migrating from 0.0.x

snipy 0.1 is a rewrite for Scala 3 and Scala Native 0.5. The ideas are the same, the API is smaller.

| 0.0.x | 0.1 |
|---|---|
| `PyZone { implicit z => ... }` around every call | nothing: references are released by the GC; use `Py.local { ... }` for deterministic release |
| `PyZone.leakingZone` for long-lived objects | `Py.global { ... }`, or nothing outside `Py.local` |
| `import snipy.dynamic._` and `Dyn(obj)` | every `Py` is dynamic: `obj.method(args)` |
| `dynamic.module("math")` | `module("math")` or `Python.module("math")` |
| phantom types (`PyFloat`, `PyList[PyLong]`, ...) and `.as[PyList[PyLong]].asScala` | one `Py` type and `.as[List[Long]]` |
| `AsPython[T, U <: PyObject]`, `AsScala[T <: PyObject, U]` | `AsPython[T]`, `AsScala[T]`, with `contramap` and `map` |
| `x.asPython` | `Py(x)` or `x.asPython` |
| `` a.`//`(b) `` | `a.floorDiv(b)` |
| `facades.builtins.str(o)` / `print(o)` | `o.toString` / `Python.builtins.print(o)` |
| Python errors ignored or crashing | `PythonException` with type, message and traceback |
| type facades as `PyObject with _Foo` | `opaque type Foo = Py` |

Bugs fixed by the rewrite:

- `/` called `__div__`, which doesn't exist in Python 3; it now uses true division.
- Values stored in tuples, lists and keyword dictionaries were released twice, because `PyTuple_SetItem` and `PyList_SetItem` steal a reference that the zone also released.
- Calls with keyword arguments only passed `None` instead of an empty tuple of positional arguments.
- Strings were encoded with the platform charset instead of UTF-8, and NUL characters truncated them.
- A failing Python call returned `NULL`, which crashed later; failures now raise `PythonException` right away.
- Python was only usable from the thread that initialized it.
