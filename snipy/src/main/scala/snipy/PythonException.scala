package snipy

import scala.util.control.NonFatal

/** A Python exception, raised by Python code called from Scala.
 *
 *  @param value
 *    the Python exception object
 *  @param pythonType
 *    the exception's class name, qualified with its module unless it's a builtin
 *    (`ValueError`, `json.decoder.JSONDecodeError`)
 *  @param pythonMessage
 *    `str(exception)`
 *  @param pythonTraceback
 *    the traceback formatted like Python prints it, empty if there is none
 */
final class PythonException private (
    val value: Py,
    val pythonType: String,
    val pythonMessage: String,
    val pythonTraceback: String
) extends RuntimeException(
      if (pythonMessage.isEmpty) pythonType else s"$pythonType: $pythonMessage"
    ) {

  /** Whether the Python exception is an instance of the builtin class `name`,
   *  or of one of its subclasses.
   */
  def is(name: String): Boolean = Python.withGil {
    Python.builtins.isinstance(value, Python.builtins.selectDynamic(name)).as[Boolean]
  }
}

object PythonException {
  private lazy val tracebackModule = Py.global(Python.module("traceback"))

  private def describe(f: => String): String =
    try f
    catch { case NonFatal(_) => "" }

  private[snipy] def typeError(message: String): PythonException =
    apply(Python.builtins.TypeError(message))

  private[snipy] def apply(value: Py): PythonException = Python.withGil {
    val cls = Python.builtins.`type`(value)
    val module = describe(cls.__module__.as[String])
    val name = describe(cls.__qualname__.as[String])
    val qualified = if (module.isEmpty || module == "builtins") name else s"$module.$name"
    val message = describe(Python.builtins.str(value).as[String])
    val traceback = describe {
      if (value.__traceback__.isNone) ""
      else tracebackModule.format_exception(value).as[List[String]].mkString
    }
    new PythonException(value, qualified, message, traceback)
  }
}
