package com.joshcough.minecraft

import org.scalacheck._
import org.scalacheck.Prop._
import Gen._
import Arbitrary.arbitrary
import Cube._
import ScalaEnrichment.id

/**
 * Generate cubes, possibly up to the maximum sized cube.
 */
trait CubeGenerators {
  implicit val genCoor : Gen[Point] = for {
    x <- arbitrary[Int]; y <- arbitrary[Int]; z <- arbitrary[Int]
  } yield Point(x, y, z)
  implicit val genCube: Gen[Cube[Point]] =
    for { c1 <- genCoor; c2 <- genCoor } yield Cube(c1, c2)(identity)
  implicit val cubes : Arbitrary[Cube[Point]] = Arbitrary(genCube)
}

/**
 * Generate cubes with:
 *   max x, y, and z no greater than 50
 *   min x, y, and z no less than -50
 */
trait SmallCubeGenerators { self: Properties =>
  val smallInteger = Gen.choose(-50,50)
  val genSmallCube: Gen[Cube[Point]] = for {
    x1 <- smallInteger; y1 <- smallInteger; z1 <- smallInteger
    x2 <- smallInteger; y2 <- smallInteger; z2 <- smallInteger
  } yield Cube(Point(x1, y1, z1), Point(x2, y2, z2))(identity)
  implicit val smallCubes: Arbitrary[Cube[Point]] = Arbitrary(genSmallCube)
}

abstract class CubeTestBase(name: String) extends Properties(name) with TestHelpers {
  def idCube(p1: Point, p2: Point): Cube[Point] = Cube(p1, p2)(id)
  def toList(cube: Cube[Point]) =  cube.toLazyList.toList
  def run(c: Cube[Point]) = c.toZippedStream.toList
}

/**
 * This object is for testing general properties on cubes,
 * where those properties are simple (O(1), usually), and calculating them doesn't
 * require traversing the whole cube.
 */
object BigCubeTests extends CubeTestBase("Cube Tests Awesome") with CubeGenerators {
  test("cubes size > 0")(forAll{ (c:Cube[Point]) => c.size > 0 })
}

/**
 * This object is for testing operations on random cubes,
 * where those operations must traverse the cube.
 * That is why we use SmallCubeGenerators, so we don't generate ridiculously huge cubes
 */
object SmallCubeTests extends CubeTestBase("Cube Tests Awesome") with SmallCubeGenerators {

  test("paste then mirror y same as mirror y then paste")(forAll{ (c:Cube[Point]) =>
    run(c.translateTo(Point(0, 10, 0)).mirrorY) ?= run(c.mirrorY.translateTo(Point(0, 10, 0)))
  })

  test("mirror x y z, same as mirror z y x")(forAll{ (c:Cube[Point]) =>
    run(c.mirrorX.mirrorY.mirrorZ) ?= run(c.mirrorZ.mirrorY.mirrorX)
  })

  test("mirror x x, same as identity")(forAll{ (c:Cube[Point]) => run(c.mirrorX.mirrorX) ?= run(c) })

  test("grow then shrink, same as identity")(forAll{ (c:Cube[Point]) =>
    run(c.grow(5,6,7).shrink(5,6,7)) ?= run(c)
  })

  test("shift up, shift down, same as identity")(forAll{ (c:Cube[Point], i: Int) =>
    run(c.shiftUp(i).shiftDown(i)) ?= run(c)
  })

  test("shift by i, shift by -i, same as identity")(forAll{ (c:Cube[Point], i: Int) =>
    run(c.shiftX(i).shiftX(-i).shiftY(i).shiftY(-i).shiftZ(i).shiftZ(-i)) ?= run(c)
  })
}


/**
 * Literal tests that don't use the generators.
 */
object LiteralCubeTests extends CubeTestBase("Cube Tests") {

  val c0 = idCube(Point(0,0,0),Point(0,0,0))
  val c  = idCube(Point(0,0,0),Point(10,10,10))

  trait P { def apply(c: Point): Int }
  case object X extends P { def apply(c: Point): Int = c.x }
  case object Y extends P { def apply(c: Point): Int = c.y }
  case object Z extends P { def apply(c: Point): Int = c.z }

  test("ap") {
    Cube.ap(
      Cube(Point(0,0,0),Point(0,0,0))(c => (p:P) => p(c)),
      Cube[P](Point(0,0,0),Point(0,0,0))(c => X)
    )(Point(0,0,0)) ?= 0
  }

  test("size 0"){ c0.size ?= 1 }

  test("simple")   { c             ?= idCube(Point(0,0,0),Point(10,10,10)) }
  test("expand")   { c.expand  (1) ?= idCube(Point(-1,-1,-1),Point(11,11,11)) }
  test("expandXY") { c.expandXZ(1) ?= idCube(Point(-1,0,-1),Point(11,10,11)) }

  test("shrink all the way") {
    c.shrink(5,5,5)       ?= idCube(Point(5,5,5),Point(5,5,5))
    c.shrink(100,100,100) ?= idCube(Point(5,5,5),Point(5,5,5))
    idCube(Point(11,11,11),Point(0,0,0)).shrink(6,6,6) ?= idCube(Point(6,6,6),Point(5,5,5))
  }

  test("shrink more") { c.shrink(2,3,4) ?= idCube(Point(2,3,4),Point(8,7,6)) }

  test("grow") { c.grow(2,3,4) ?= idCube(Point(-2,-3,-4),Point(12,13,14)) }

  // the max - min check here makes sure we don't wrap around to a negative int.
  test("shrink")(forAll{ (max:Int,min:Int) => (max >= min && max - min > 1) ==> {
    val c = idCube(Point(max,max,max), Point(min,min,min))
    c.size > c.shrink(1,1,1).size
  }})
}

/**
 * Literal tests on mirroring that don't use the generators.
 */
object LiteralCubeMirroringTests extends CubeTestBase("Cube Mirroring Tests") {

  val cx = idCube(Point(0,0,0),Point(3,0,0))
  val cy = idCube(Point(0,0,0),Point(0,3,0))
  val cz = idCube(Point(0,0,0),Point(0,0,3))

  test("normal x") {
    toList(cx)         ?= List(Point(0,0,0),Point(1,0,0),Point(2,0,0),Point(3,0,0))
  }
  test("normal y") { toList(cy)         ?= List(Point(0,0,0),Point(0,1,0),Point(0,2,0),Point(0,3,0)) }
  test("mirrorX")  { toList(cx.mirrorX) ?= List(Point(3,0,0),Point(2,0,0),Point(1,0,0),Point(0,0,0)) }
  test("mirrorY")  { toList(cy.mirrorY) ?= List(Point(0,3,0),Point(0,2,0),Point(0,1,0),Point(0,0,0)) }
  test("mirrorZ")  { toList(cz.mirrorZ) ?= List(Point(0,0,3),Point(0,0,2),Point(0,0,1),Point(0,0,0)) }

  test("paste y") {
    run(cy.translateTo(Point(5, 0, 0))) ?= List(
      (Point(5,0,0),Point(0,0,0)), (Point(5,1,0),Point(0,1,0)),
      (Point(5,2,0),Point(0,2,0)), (Point(5,3,0),Point(0,3,0))
    )
  }

  test("paste y twice") {
    run(cy.translateTo(Point(0, 10, 0)).translateTo(Point(0, 20, 0))) ?= List(
      (Point(0,20,0),Point(0,0,0)),
      (Point(0,21,0),Point(0,1,0)),
      (Point(0,22,0),Point(0,2,0)),
      (Point(0,23,0),Point(0,3,0))
    )
  }

  test("paste then mirror y") {
    run(cy.translateTo(Point(0, 10, 0)).mirrorY) ?= List(
      (Point(0,10,0),Point(0,3,0)),
      (Point(0,11,0),Point(0,2,0)),
      (Point(0,12,0),Point(0,1,0)),
      (Point(0,13,0),Point(0,0,0))
    )
  }

  test("mirror then paste y") {
    run(cy.mirrorY.translateTo(Point(0, 10, 0))) ?= List(
      (Point(0,10,0),Point(0,3,0)),
      (Point(0,11,0),Point(0,2,0)),
      (Point(0,12,0),Point(0,1,0)),
      (Point(0,13,0),Point(0,0,0))
    )
  }
}

object ShrinkageTests extends CubeTestBase("Past failures for Cubes") {
  test("cube of size 1 wont shrink.") {
    idCube(Point(3,0,0), Point(3,0,0)).shrink(5,0,0) ?= idCube(Point(3,0,0), Point(3,0,0))
  }
  test("cube of size 2 wont shrink.") {
    idCube(Point(4,0,0), Point(3,0,0)).shrink(1,0,0) ?= idCube(Point(4,0,0), Point(3,0,0))
  }
  test("shrink test 3") {
    idCube(Point(6,0,0), Point(3,0,0)).shrink(5,0,0) ?= idCube(Point(5,0,0), Point(4,0,0))
  }
  test("shrink test 4") {
    idCube(Point(6,0,0), Point(3,0,0)).shrink(1,0,0) ?= idCube(Point(5,0,0), Point(4,0,0))
  }
  test("shrink test 5") {
    idCube(Point(6,0,0), Point(3,0,0)).shrink(2,0,0) ?= idCube(Point(5,0,0), Point(4,0,0))
  }
}