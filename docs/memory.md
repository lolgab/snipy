# Memory management

CPython frees objects by reference counting; Scala Native frees objects with a tracing garbage collector. snipy connects the two so that you rarely have to think about either.

## One `Py`, one reference

Each `Py` owns exactly one strong reference to its Python object. Copying the `Py` value around shares that one reference; Python objects stay alive as long as some `Py` pointing at them is reachable, or Python itself holds them.

## Automatic release with `java.lang.ref.Cleaner`

Scala Native 0.5 implements `java.lang.ref.Cleaner` on top of the GC's weak references. snipy registers every `Py` with a cleaner. When the GC finds that a `Py` is unreachable, the cleaner enqueues its pointer, and the reference is released the next time a thread enters or leaves a snipy operation while holding the GIL.

The cleaner never calls into Python itself: it may run inside the GC or on a thread that doesn't hold the GIL, where touching Python would be unsafe.

This requires the `immix` (default) or `commix` GC. With `none`, which has no weak references, and with `boehm`, whose weak references deadlock under load on Scala Native 0.5.12, objects are only released by `dispose` and `Py.local`.

## Deterministic release

The GC only sees the small `Py` handles, not the memory Python allocates behind them. A loop creating large Python objects, such as big arrays, can grow Python's memory long before the GC feels any pressure. Two tools release references right away.

### `dispose()`

```scala
val frame = pandas.read_csv(path)
// ...
frame.dispose() // release now; using `frame` afterwards throws IllegalStateException
```

### `Py.local`

`Py.local` disposes every `Py` created on the current thread while its body runs, except the value it returns when that value is a `Py`:

```scala
val result: Double = Py.local {
  val a = np.random.rand(1000, 1000)
  val b = a.matmul(a.T)
  b.sum().as[Double]
} // a, b, a.T and b.sum() are released here

val kept: Py = Py.local {
  val tmp = np.arange(10)
  tmp * 2 // returned, so kept
}
```

Blocks nest; a value returned from an inner block belongs to the outer one. If the body throws a `PythonException`, its `value` is kept so you can inspect it.

Only the returned `Py` itself is kept. A `Py` stored in a variable outside the block, or inside a returned Scala collection, is disposed anyway: convert results to Scala values inside the block, or create long-lived objects with `Py.global`.

### `Py.global`

`Py.global` runs its body outside any enclosing `Py.local` block. Use it for objects that must outlive the block they happen to be created in, notably modules cached in `lazy val`s:

```scala
object np {
  private lazy val numpy = Py.global(module("numpy"))
  def zeros(n: Int): Py = numpy.zeros(n)
}
```

Without `Py.global`, the first `np.zeros` call inside a `Py.local` block would initialize `numpy` inside that block and dispose it at its end.

## Borrowed versus owned pointers

If you implement `AsPython` or `AsScala` directly, you work with raw `PyObject*` pointers and must follow CPython's ownership rules:

- `AsPython.newReference` returns a new reference that the caller takes ownership of.
- `AsScala.fromPython` receives a borrowed reference: it must not release it, and must take a new reference for anything it keeps.

Building on existing instances with `contramap` and `map` avoids both.
