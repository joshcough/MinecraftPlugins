package jcdc.pluginfactory.examples

import org.scalacheck.Properties
import org.scalacheck.Prop._
import jcdc.pluginfactory.{Cube, TestServer}
import org.bukkit.entity.Player
import org.bukkit.ChatColor._
import MineLang._
import MineLangExamples._

case class Point(x:Int, y:Int){
  override def toString = s"($x,$y)"
}

object MineLangTests extends Properties("MinecraftParserTests") {

  val fact = """
    ((defrec fact (n) (if (eq n 0) 1 (* n (fact (- n 1)))))
     (fact 5)
    )
    """
  val newTest = "((new jcdc.pluginfactory.examples.Point 5 6))"
  val methodTest = "((.toString (new jcdc.pluginfactory.examples.Point 5 6)))"
  val methodTest2 = """((.indexOf (new jcdc.pluginfactory.examples.Point 5 6) "5" 0))"""

  evalTest("houseTest", house, UnitValue)
  evalTest("fact", fact, IntValue(120))
  evalTest("expansionTest", expansionTest, CubeValue(Cube(TestServer.world(12,3,12), TestServer.world(-2,3,-2))))
  evalTest("newTest", newTest, ObjectValue(Point(5,6)))
  evalTest("methodTest", methodTest, StringValue("(5,6)"))
  evalTest("methodTest2", methodTest2, IntValue(1))

  def evalTest(name:String, code:String, expected:Value) =
    property(name) = secure { run(code, expected) }

  def run(code:String, expected:AnyRef): Boolean = {
    val actual = MineLang.run(code, TestServer.player)
    println(s"Result: $actual")
    actual == expected
  }
}

//  val testScriptFull =
//    """
//     ((begin
//      (goto origin)
//      (corners (loc (+ X 20) (+ Y 50) (+ Z 20)) (loc (- X 20) Y (- Z 20)))
//      (floor stone)
//      (walls brick)
//      7
//     ))
//    """.stripMargin.trim
//
//  val valTest = "((val x 7) x)"
//
//  val defTest = """
//    (
//      (def d (a1 a2) (begin (corners (loc a1 a1 a1) (loc a2 a2 a2)) (set stone) (+ a1 a2)))
//      (val x 7)
//      (let (g 9) (d g 7))
//    )
//  """
//  property("origin")  = secure { parseExpr("origin") }
//  property("XYZ")     = secure { parseExpr("XYZ") }
//  property("(5 6 7)") = secure { parseExpr("(loc 5 6 7)") }
//  property("(5 Y 7)") = secure { parseExpr("(loc 5 Y 7)") }
//  property("((+ 5 10) Y 7)")        = secure { parseExpr("(loc (+ 5 10) Y 7)") }
//  property("((+ X 20) Y (+ Z 20))") = secure { parseExpr("(loc (+ X 20) Y (+ Z 20))") }
//  property("((- X 20) Y (- Z 20))") = secure { parseExpr("(loc (- X 20) Y (- Z 20))") }
//  property("((- 5 10) (+ Y 20) Z)") = secure { parseExpr("(loc (- 5 10) (+ Y 20) Z)") }
//  property("origin")  = secure { parseExpr("(set stone)") }
//  property("((goto origin))") = secure { run("((goto origin))") }
//  property("(corners XYZ origin)") = secure { run("((corners XYZ origin))") }
//  property("(corners XYZ (5 6 7))") = secure { run("((corners XYZ (loc 5 6 7)))") }
//  property("(corners (loc (+ X 20) Y (+ Z 20)) (loc (+ X 20) Y (+ Z 20)))") =
//    secure { run("((corners (loc (+ X 20) Y (+ Z 20)) (loc (+ X 20) Y (+ Z 20))))") }
//  property("(set stone)")   = secure { run("((set stone))") }
//  property("(walls brick)") = secure { run("((walls brick))") }
//  property("testScriptFull") = secure { parseExpr(testScriptFull) }
//  property("testScriptFull") = secure { run(testScriptFull) }
//  property("valTest") = secure { parse(valTest) }
//  property("valTest") = secure { run  (valTest) }
//  property("defTest") = secure { parse(defTest) }
//  property("defTest") = secure { run  (defTest) }
//  def parseExpr(code:String): Boolean =
//    attemptT(p, truthfully(println(s"Parse Tree: ${WorldEditLang.parseExpr(Reader.read(code))}")))
//
//  def parse(code:String): Boolean =
//    attemptT(p, truthfully(println(s"Parse Tree: ${WorldEditLang.parse(code)}")))
