package jcdc.pluginfactory

import org.scalacheck.Properties
import org.scalacheck.Prop
import org.scalacheck.Prop._

trait TestHelpers { self: Properties =>

  implicit val world = TestServer.world

  def trying(f: => Prop) = secure {
    try f catch { case e: Throwable  => e.printStackTrace; throw e }
  }

  def test(name:String)(f: => Prop) = property(name) = trying(f)
}