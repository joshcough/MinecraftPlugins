package jcdc.pluginfactory

import org.scalacheck._
import org.scalacheck.Prop._
import Gen._
import Arbitrary.arbitrary

/**
 * Generate cubes, possibly up to the maximum sized cube.
 */
trait CubeGenerators {
  type C = Cube[Coor]
  implicit val genCoor: Gen[Coor] = for {
    x <- arbitrary[Int]; y <- arbitrary[Int]; z <- arbitrary[Int]
  } yield Coor(x, y, z)
  implicit val genCube: Gen[C] = for { c1 <- genCoor; c2 <- genCoor } yield Cube(c1, c2)(identity)
  implicit val cubes = Arbitrary(genCube)
}

/**
 * Generate cubes with:
 *   max x, y, and z no greater than 50
 *   min x, y, and z no less than -50
 */
trait SmallCubeGenerators { self: Properties =>
  type C = Cube[Coor]
  val smallInteger = Gen.choose(-50,50)
  val genSmallCube: Gen[C] = for {
    x1 <- smallInteger; y1 <- smallInteger; z1 <- smallInteger
    x2 <- smallInteger; y2 <- smallInteger; z2 <- smallInteger
  } yield Cube(Coor(x1, y1, z1), Coor(x2, y2, z2))(identity)
  implicit val smallCubes = Arbitrary(genSmallCube)
}

class CubeTestBase(name: String) extends Properties(name) with TestHelpers{
  def toList(c: Cube[Coor]) = c.toStream.toList.map(_.xyz)
  def run(c: Cube[Coor]) = c.toZippedStream.toList.map( t => (t._1.xyz, t._2.xyz) )
}

/**
 * This object is for testing general properties on cubes,
 * where those properties are simple (O(1), usually), and calculating them doesn't
 * require traversing the whole cube.
 */
object CubeTestsAwesome extends CubeTestBase("Cube Tests Awesome") with CubeGenerators {
  test("cubes size > 0")(forAll{ (c:C) => c.size > 0 })
}

/**
 * This object is for testing operations on random cubes,
 * where those operations must traverse the cube.
 * That is why we use SmallCubeGenerators, so we don't generate ridiculously huge cubes
 */
object SmallCubeTestsAwesome extends CubeTestBase("Cube Tests Awesome") with SmallCubeGenerators {

  test("paste then mirror y same as mirror y then paste")(forAll{ (c:C) =>
    run(c.translateTo(Coor(0, 10, 0)).mirrorY) ?= run(c.mirrorY.translateTo(Coor(0, 10, 0)))
  })

  test("mirror x y z, same as mirror z y x")(forAll{ (c:C) =>
    run(c.mirrorX.mirrorY.mirrorZ) ?= run(c.mirrorZ.mirrorY.mirrorX)
  })

  test("mirror x x, same as identity")(forAll{ (c:C) =>
    run(c.mirrorX.mirrorX) ?= run(c)
  })
}


/**
 * Literal tests that don't use the generators.
 */
object LiteralCubeTests extends CubeTestBase("Cube Tests") {

  val c0 = Cube.coors((0,0,0),(0,0,0))
  val c = Cube.coors((0,0,0),(10,10,10))

  test("ap") {
    Cube.ap(
      Cube((0,0,0),(0,0,0))(c => (p:P) => p(c)),
      Cube[P]((0,0,0),(0,0,0))(c => X)
    )(Coor(0,0,0)) ?= 0
  }

  test("size 0"){ c0.size ?= 1 }

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

/**
 * Literal tests on mirroring that don't use the generators.
 */
object LiteralCubeMirroringTests extends CubeTestBase("Cube Mirroring Tests") {

  val cx = Cube.coors((0,0,0),(3,0,0))
  val cy = Cube.coors((0,0,0),(0,3,0))
  val cz = Cube.coors((0,0,0),(0,0,3))

  test("normal x") { toList(cx)         ?= List((0,0,0),(1,0,0),(2,0,0),(3,0,0)) }
  test("normal y") { toList(cy)         ?= List((0,0,0),(0,1,0),(0,2,0),(0,3,0)) }
  test("mirrorX")  { toList(cx.mirrorX) ?= List((3,0,0),(2,0,0),(1,0,0),(0,0,0)) }
  test("mirrorY")  { toList(cy.mirrorY) ?= List((0,3,0),(0,2,0),(0,1,0),(0,0,0)) }
  test("mirrorZ")  { toList(cz.mirrorZ) ?= List((0,0,3),(0,0,2),(0,0,1),(0,0,0)) }

  test("paste y") {
    run(cy.translateTo(Coor(5, 0, 0))) ?= List(
      ((5,0,0),(0,0,0)), ((5,1,0),(0,1,0)), ((5,2,0),(0,2,0)), ((5,3,0),(0,3,0))
    )
  }

  test("paste y twice") {
    run(cy.translateTo(Coor(0, 10, 0)).translateTo(Coor(0, 20, 0))) ?= List(
      ((0,20,0),(0,0,0)), ((0,21,0),(0,1,0)), ((0,22,0),(0,2,0)), ((0,23,0),(0,3,0))
    )
  }

  test("paste then mirror y") {
    run(cy.translateTo(Coor(0, 10, 0)).mirrorY) ?= List(
      ((0,10,0),(0,3,0)), ((0,11,0),(0,2,0)), ((0,12,0),(0,1,0)), ((0,13,0),(0,0,0))
    )
  }

  test("mirror then paste y") {
    run(cy.mirrorY.translateTo(Coor(0, 10, 0))) ?= List(
      ((0,10,0),(0,3,0)), ((0,11,0),(0,2,0)), ((0,12,0),(0,1,0)), ((0,13,0),(0,0,0))
    )
  }
}
