package jcdc.pluginfactory

import org.scalacheck.Properties
import org.scalacheck.Prop._

object CubeTests extends Properties("Cube Tests") with TestHelpers {

  val c = Cube.coors((0,0,0),(10,10,10))

  test("simple")   { c             ?= Cube.coors((0,0,0),(10,10,10)) }
  test("shift up") { c.shiftUp (1) ?= Cube.coors((0,1,0),(10,11,10)) }
  test("shift x")  { c.shiftX  (5) ?= Cube.coors((5,0,0),(15,10,10)) }
  test("shift z")  { c.shiftZ  (9) ?= Cube.coors((0,0,9),(10,10,19)) }
  test("expand")   { c.expand  (1) ?= Cube.coors((-1,-1,-1),(11,11,11)) }
  test("expandXY") { c.expandXZ(1) ?= Cube.coors((-1,0,-1),(11,10,11)) }

  test("shrink all the way") {
    c.shrink(5,5,5)       ?= Cube.coors((5,5,5),(5,5,5))
    c.shrink(100,100,100) ?= Cube.coors((5,5,5),(5,5,5))
    Cube.coors((0,0,0),(11,11,11)).shrink(6,6,6) ?= Cube.coors((5,5,5),(5,5,5))
  }

  test("shrink more") { c.shrink(2,3,4) ?= Cube.coors((2,3,4),(8,7,6)) }

  test("grow") { c.grow(2,3,4) ?= Cube.coors((-2,-3,-4),(12,13,14)) }

  // the max - min check here makes sure we don't wrap around to a negative int.
  test("shrink")(forAll{ (max:Int,min:Int) => (max >= min && max - min > 1) ==> {
    val c = Cube.coors((max,max,max), (min,min,min))
    c.size > c.shrink(1,1,1).size
  }})
}