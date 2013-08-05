package com.joshcough.minecraft

import org.scalacheck.Properties
import org.scalacheck.Prop._
import ScalaEnrichment._

object ScalaEnrichmentTests extends Properties("ScalaEnrichmentTests") with TestHelpers {
  test("simple")   { Some("hi").flipFold(s => s + s)("fail?") ?= "hihi" }
}
