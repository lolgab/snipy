package snipy

import scala.quoted.*

/** Converts the arguments of dynamic calls with their static type's [[AsPython]]
 *  instance, so `obj.f(1, "a", List(2.0))` needs no implicit conversions and
 *  fails to compile when an argument can't be converted.
 */
private[snipy] object Macros {

  def args(args: Expr[Seq[Any]])(using Quotes): Expr[Seq[Py]] = args match {
    case Varargs(items)     => Expr.ofSeq(items.map(convert))
    case '{ $seq: Seq[Py] } => seq
    case _                  =>
      quotes.reflect.report.errorAndAbort(
        "snipy: spread arguments (xs*) must be a Seq[Py]",
        args
      )
  }

  def namedArgs(args: Expr[Seq[(String, Any)]])(using Quotes): Expr[Seq[(String, Py)]] =
    args match {
      case Varargs(items) =>
        Expr.ofSeq(items.map {
          case '{ ($name: String, $value: t) }     => '{ ($name, ${ convert(value) }) }
          case '{ ($name: String) -> ($value: t) } => '{ ($name, ${ convert(value) }) }
          case other                               =>
            quotes.reflect.report.errorAndAbort("snipy: expected a (name, value) pair", other)
        })
      case '{ $seq: Seq[(String, Py)] } => seq
      case _                            =>
        quotes.reflect.report.errorAndAbort(
          "snipy: spread arguments (xs*) must be a Seq[(String, Py)]",
          args
        )
    }

  private def convert(value: Expr[Any])(using Quotes): Expr[Py] = {
    import quotes.reflect.*
    value.asTerm.tpe.widen.asType match {
      case '[Py] => value.asExprOf[Py]
      case '[t]  =>
        Expr.summon[AsPython[t]] match {
          case Some(conv) => '{ $conv.asPython(${ value.asExprOf[t] }) }
          case None       =>
            report.errorAndAbort(
              s"snipy: no AsPython[${Type.show[t]}] instance to convert this argument to Python",
              value
            )
        }
    }
  }

  def applyDynamic(self: Expr[Py], name: Expr[String], args: Expr[Seq[Any]])(using
      Quotes
  ): Expr[Py] = '{ $self.callMethod($name, ${ Macros.args(args) }) }

  def applyDynamicNamed(self: Expr[Py], name: Expr[String], args: Expr[Seq[(String, Any)]])(using
      Quotes
  ): Expr[Py] = '{ $self.callMethodNamed($name, ${ namedArgs(args) }) }

  def apply(self: Expr[Py], args: Expr[Seq[Any]])(using Quotes): Expr[Py] =
    '{ $self.call(${ Macros.args(args) }) }

  def applyNamed(self: Expr[Py], args: Expr[Seq[(String, Any)]])(using Quotes): Expr[Py] =
    '{ $self.callNamed(${ namedArgs(args) }) }

  def tuple(args: Expr[Seq[Any]])(using Quotes): Expr[Py] =
    '{ Py.tupleOf(${ Macros.args(args) }) }

  def list(args: Expr[Seq[Any]])(using Quotes): Expr[Py] =
    '{ Py.listOf(${ Macros.args(args) }) }
}
