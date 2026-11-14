package examples.facades

import snipy.*

/** A typed facade over Python's `fractions` module.
 *
 *  An opaque type over [[Py]] gives the Python object a Scala type of its own,
 *  without any wrapper allocation.
 */
object fractions {
  private lazy val module = Py.global(snipy.module("fractions"))

  opaque type Fraction = Py

  object Fraction {
    def apply(numerator: Long, denominator: Long): Fraction =
      module.Fraction(numerator, denominator)

    def parse(s: String): Fraction = module.Fraction(s)
  }

  // Inside this object Fraction is Py, so `f.numerator` would call the
  // extension below: reach the Python attributes with selectDynamic.
  extension (f: Fraction) {
    def numerator: Long = f.selectDynamic("numerator").as[Long]
    def denominator: Long = f.selectDynamic("denominator").as[Long]
    def +(other: Fraction): Fraction = (f: Py) + (other: Py)
    def *(other: Fraction): Fraction = (f: Py) * (other: Py)
    def toDouble: Double = Python.builtins.float(f).as[Double]
  }
}
