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
//
//object CubeMirroringTests extends Properties("Cube Mirroring Tests") with TestHelpers {
//
//  val cx = Cube.coors((0,0,0),(3,0,0))
//  val cy = Cube.coors((0,0,0),(0,3,0))
//  val cz = Cube.coors((0,0,0),(0,0,3))
//
//  def toList(c: Cube[Coor]) = c.toStream.toList.map(_.xyz)
//
//  test("normal")  { toList(cx)         ?= List((0,0,0),(1,0,0),(2,0,0),(3,0,0)) }
//  test("mirrorX") { toList(cx.mirrorX) ?= List((3,0,0),(2,0,0),(1,0,0),(0,0,0)) }
//  test("mirrorY") { toList(cy.mirrorY) ?= List((0,3,0),(0,2,0),(0,1,0),(0,0,0)) }
//  test("mirrorZ") { toList(cz.mirrorZ) ?= List((0,0,3),(0,0,2),(0,0,1),(0,0,0)) }
//
//  test("paste y") {
//    cy.paste(Coor(5, 0, 0)).toStream.toList.map(_._2).map(_.xyz) ?= List(
//      (5,0,0),(5,1,0),(5,2,0),(5,3,0)
//    )
//  }
//
//  test("paste y twice") {
//    cy.paste(Coor(0, 10, 0)).paste(Coor(0, 20, 0)).toStream.toList.map(_._2._2).map(_.xyz) ?= List(
//      (0,20,0),(0,21,0),(0,22,0),(0,23,0)
//    )
//  }
//
//  test("paste then mirror y") {
//    cy.paste(Coor(0, 10, 0)).mirrorY.toStream.toList.map(_._2).map(_.xyz) ?= List(
//      (0,13,0),(0,12,0),(0,11,0),(0,10,0)
//    )
//  }
//
//  /**
//   * TODO: this fails!
//   * I would think that mirror then paste would be the same as paste then mirror...
//   */
//  test("mirror then paste y") {
//    val mp = cy.mirrorY.paste(Coor(0, 10, 0))
//    println(s"mp: $mp")
//
//    mp.toStream.toList.map(_._2).map(_.xyz) ?= List(
//      (0,13,0),(0,12,0),(0,11,0),(0,10,0)
//    )
//  }
//}