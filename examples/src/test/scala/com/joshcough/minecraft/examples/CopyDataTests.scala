//package com.joshcough.minecraft.examples
//
//import org.scalacheck._
//import org.scalacheck.Prop._
//import Gen._
//import Arbitrary.arbitrary
//import com.joshcough.minecraft.{Cube, Point}
//import java.io.{DataOutputStream, DataInputStream, ByteArrayInputStream, ByteArrayOutputStream}
//
//object CopyDataTests extends Properties("CopyDataTests") {
//  type C = Cube[Point]
//  val smallInteger = Gen.choose(-2,2)
//  val genSmallCube: Gen[C] = for {
//    x1 <- smallInteger; y1 <- smallInteger; z1 <- smallInteger
//    x2 <- smallInteger; y2 <- smallInteger; z2 <- smallInteger
//  } yield Cube(Point(x1, y1, z1), Point(x2, y2, z2))(identity)
//  implicit val smallCubes = Arbitrary(genSmallCube)
//  def toBlockData(p:Point): BlockData =
//    BlockData(p.x, p.y, p.z, (math.random * 1024).toInt, (math.random * 256).toByte)
//  def toCopyData(c: Cube[Point]): CopyData  =
//    CopyData(c.corner1, c.corner2, c.size.toLong, c.toStream.map(toBlockData))
//  implicit val copyDatas = Arbitrary(genSmallCube.map(toCopyData))
//  def test(name:String)(f: => Prop) = property(name) = trying(f)
//  def trying(f: => Prop) = secure {
//    try f catch { case e: Throwable  => e.printStackTrace; throw e }
//  }
//  def roundTrip(cd: CopyData): CopyData = {
//    val bos = new ByteArrayOutputStream()
//    val os = new DataOutputStream(bos)
//    CopyData.write(cd, os)
//    os.flush()
//    CopyData.read(new DataInputStream(new ByteArrayInputStream(bos.toByteArray)))
//  }
//
//  test("paste then mirror y same as mirror y then paste")(forAll{ (cd:CopyData) =>
//    cd ?= roundTrip(cd)
//  })
//}
