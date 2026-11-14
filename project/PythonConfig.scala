import scala.sys.process._

/**
 * Discovers how to link against the embeddable libpython.
 *
 *  The interpreter is taken from the `SNIPY_PYTHON` environment variable,
 *  falling back to `python3` on the `PATH`.
 */
object PythonConfig {
  lazy val interpreter: String = sys.env.getOrElse("SNIPY_PYTHON", "python3")

  private def query(expr: String): String =
    Seq(interpreter, "-c", s"import sysconfig; print($expr)").!!.trim

  lazy val libDir: String = query("sysconfig.get_config_var('LIBDIR')")

  lazy val ldVersion: String = query("sysconfig.get_config_var('LDVERSION')")

  lazy val linkingOptions: Seq[String] = {
    val extra = query(
      "' '.join(filter(None, [sysconfig.get_config_var('LIBS'), sysconfig.get_config_var('SYSLIBS')]))"
    ).split("\\s+")
      .filter(_.nonEmpty)
      .toSeq
      // already linked by Scala Native
      .filterNot(Set("-ldl", "-lpthread", "-lm"))
    Seq(s"-L$libDir", s"-Wl,-rpath,$libDir", s"-lpython$ldVersion") ++ extra
  }
}
