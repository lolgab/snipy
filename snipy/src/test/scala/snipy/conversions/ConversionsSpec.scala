package snipy.conversions

import org.scalatest.Assertion
import org.scalatest.prop.Generator
import snipy._
import facades.builtins

class ConversionsSpec extends SnipySpec {
  def testConversion[T, U <: PyObject](implicit asPython: AsPython[T, U],
                                       asScala: AsScala[U, T],
                                       gen: Generator[T]): Assertion =
    forAll { b: T =>
      PyZone { implicit z =>
        assert(b.asPython.asScala === b)
      }
    }

  "Default values should go back and forth from Scala to Python to Scala unchanged" - {
    "Long" in testConversion[Long, PyLong]
    "Double" in testConversion[Double, PyFloat]
    "String" in testConversion[String, PyString]
    "Boolean" in testConversion[Boolean, PyBool]
    "(Boolean, Double)" in testConversion[(Boolean, Double),
                                          PyTuple2[PyBool, PyFloat]]
//    "Seq[Long]" in {
//      implicit val seqGen: Generator[Seq[Long]] = lists[Long].map(_.toSeq)
//      testConversion[Seq[Long], PyList[PyLong]]
//    }
    "Map[Long, Long]" in testConversion[Map[Long, Long], PyDict[PyLong, PyLong]]
  }

}
