package jcdc.pluginfactory

import org.scalacheck.Prop._
import org.scalacheck.Properties

trait Compare { self: Properties =>
  def compare[T](testName: String, actual: => T, expected: => T) = property(testName) = secure {
    val a = actual.toString
    val e = expected.toString
    if (a != e) println("expected: " + e + ", got: " + a)
    a == e
  }
}