# Changelog

## 0.1.0

A rewrite for current Scala, Scala Native and Python. See the [migration guide](docs/migration.md).

- Scala 3 (built with 3.3 LTS, tested up to 3.10), Scala Native 0.5, Python 3.12+.
- A single dynamic `Py` type replaces `PyObject`, `Dyn` and the phantom types.
- Python references are released by the Scala Native GC through `java.lang.ref.Cleaner`; `dispose()` and `Py.local` release them deterministically. `PyZone` is gone.
- Call arguments are converted at compile time by a macro: unsupported argument types are compile errors, and no implicit conversions are involved.
- Calls use CPython's vectorcall protocol, method calls avoid creating bound methods, and attribute names are interned once.
- Python exceptions become `PythonException`, with type, message and traceback.
- Scala functions can be passed to Python as callables.
- Python can be used from any Scala Native thread; the GIL is handled for you.
- Conversions for `BigInt`, `Char`, `Option`, `Set`, `Vector`, `Array`, `Array[Byte]` as `bytes`, and tuples of any size.
- Operators use Python's number protocol, including reflected operators, `floorDiv`, `matmul` and comparisons.
- Fixed: double release of container items, `/` calling `__div__`, keyword-only calls, string encoding, crashes on Python errors.

## 0.0.2

Last release for Scala 2.11 and Scala Native 0.3.
