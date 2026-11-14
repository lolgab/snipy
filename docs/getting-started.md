# Getting started

snipy embeds the CPython interpreter in a [Scala Native](https://scala-native.org) program and talks to it through the [Python C API](https://docs.python.org/3/c-api/). There is no FFI layer and no separate process: Python objects are plain pointers on the Scala side.

## Requirements

| | Version |
|---|---|
| Scala | 3.3 LTS or newer |
| Scala Native | 0.5.x |
| Python | 3.12 or newer, with its shared library (`libpython3.x`) |

Most Python distributions ship the shared library: Homebrew, the python.org installers, `actions/setup-python`, Debian/Ubuntu (`libpython3.x-dev`), pyenv when built with `--enable-shared`.

## sbt

`project/plugins.sbt`:

```scala
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.12")
```

`build.sbt`:

```scala
import scala.sys.process.*

enablePlugins(ScalaNativePlugin)

scalaVersion := "3.3.9"
libraryDependencies += "com.github.lolgab" %%% "snipy" % "0.1.0" // `%%` on sbt 2

// Link against libpython. SNIPY_PYTHON selects another interpreter.
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

The `-rpath` flag lets the binary find `libpython` at run time without setting `LD_LIBRARY_PATH`.

## Scala CLI

```scala
//> using scala 3.3.9
//> using platform native
//> using nativeVersion 0.5.12
//> using dep com.github.lolgab::snipy::0.1.0
```

Pass each linker flag with its own `--native-linking`:

```sh
LIBDIR="$(python3 -c "import sysconfig; print(sysconfig.get_config_var('LIBDIR'))")"
scala-cli run . $(for f in $(python3-config --ldflags --embed) "-Wl,-rpath,$LIBDIR"; do
  printf -- '--native-linking=%s ' "$f"
done)
```

## Your first call

```scala
import snipy.*

@main def hello(): Unit = {
  val math = module("math")              // import math
  val root = math.sqrt(2).as[Double]     // math.sqrt(2), converted to Double
  println(root)

  val json = module("json")
  val text = json.dumps(Map("a" -> List(1, 2)), indent = 2)
  println(text.as[String])
}
```

There is nothing to set up at run time: the interpreter starts the first time you touch a Python object.

## Choosing the interpreter at run time

Embedded Python finds its standard library from the location it was installed to. That works for system, Homebrew and `setup-python` interpreters. For relocated distributions, point Python to its installation:

```sh
PYTHONHOME="$(python3 -c 'import sys; print(sys.base_prefix)')" ./my-app
```

To use packages from a virtual environment, add its `site-packages` to `PYTHONPATH`:

```sh
PYTHONPATH="$(.venv/bin/python -c 'import site; print(site.getsitepackages()[0])')" ./my-app
```

The virtual environment must be based on the same Python version you linked against.

Next: the [guide](guide.md).
