# snipy

[![CI](https://github.com/lolgab/snipy/actions/workflows/ci.yml/badge.svg)](https://github.com/lolgab/snipy/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.lolgab/snipy_native0.5_3)](https://central.sonatype.com/artifact/com.github.lolgab/snipy_native0.5_3)

Call Python from [Scala Native](https://scala-native.org). snipy embeds CPython in your binary and talks to it through the C API: no FFI layer, no subprocess, no JVM.

```scala
import snipy.*

@main def demo(): Unit = {
  val json = module("json")
  val text = json.dumps(Map("langs" -> List("scala", "python")), indent = 2)
  println(text.as[String])

  val byLength = (s: String) => s.length
  val sorted = Python.builtins.sorted(List("ccc", "a", "bb"), key = byLength)
  println(sorted.as[List[String]]) // List(a, bb, ccc)
}
```

- **Python, written as Python.** Attributes, calls, keyword arguments and operators read as they do in Python.
- **Checked at compile time.** Arguments are converted by a macro; a value with no conversion doesn't compile.
- **No manual reference counting.** Python references are released by the Scala Native GC through `java.lang.ref.Cleaner`, or right away with `Py.local` and `dispose()`.
- **Python errors are Scala exceptions** with Python's type, message and traceback.
- **Both directions.** Pass Scala functions to Python as callbacks.
- **Any thread.** The GIL is taken for you.

Scala 3.3+, Scala Native 0.5, Python 3.12+.

## Install

```scala
// project/plugins.sbt
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.12")
```

```scala
// build.sbt
import scala.sys.process.*

enablePlugins(ScalaNativePlugin)
libraryDependencies += "com.github.lolgab" %%% "snipy" % "0.1.0" // `%%` on sbt 2

nativeConfig ~= { config =>
  val python = sys.env.getOrElse("SNIPY_PYTHON", "python3")
  def sysconfig(name: String) =
    Seq(python, "-c", s"import sysconfig; print(sysconfig.get_config_var('$name'))").!!.trim
  val libDir = sysconfig("LIBDIR")
  config.withLinkingOptions(
    _ ++ Seq(s"-L$libDir", s"-Wl,-rpath,$libDir", s"-lpython${sysconfig("LDVERSION")}")
  )
}
```

## Documentation

- [Getting started](docs/getting-started.md): setup, Scala CLI, choosing the interpreter
- [Guide](docs/guide.md): calls, conversions, operators, callbacks, exceptions, facades, threads
- [Memory management](docs/memory.md): how Python references are released
- [Migrating from 0.0.x](docs/migration.md)
- [API reference](https://lolgab.github.io/snipy/api/)

Or browse it all on the website: **[lolgab.github.io/snipy](https://lolgab.github.io/snipy)**.

## Development

```sh
sbt snipy/test            # tests, against `python3` on the PATH
SNIPY_PYTHON=python3.13 sbt snipy/test
sbt examples/run          # a tour of the API
```

Releases are published to Maven Central by CI when a `v*` tag is pushed.

## License

[Apache 2.0](LICENSE)
