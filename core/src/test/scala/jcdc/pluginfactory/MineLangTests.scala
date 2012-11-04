package jcdc.pluginfactory

import org.scalacheck.Properties
import org.scalacheck.Prop.secure
import ClojureInScala._
  import Reader._
  import AST._
  import Parser._
  import MineLang._
import java.io.File

object MineLangTests extends Properties("MinecraftParserTests") with EnrichmentClasses with TestHelpers{

  val mineLangDir = new File("../minelang")
  val expandMc = mineLangDir.child("expand.mc")
  parseDefsTest("expand defs parse", expandMc, 0)
  evalTest("expand", expandMc.slurp, Cube(TestServer.world(12,3,12), TestServer.world(-2,3,-2)))

  val factorialDefs = mineLangDir.child("factorial.mc")
  parseDefsTest("factorial defs parse", factorialDefs, 2)
  evalWithDefsTest("factorial defs eval", "(test)", 120, factorialDefs)

  val houseDefs = mineLangDir.child("house.mc")
  parseDefsTest("house defs parse", houseDefs, 10)
  evalWithDefsTest("house defs eval", "(city)", (), houseDefs)

  def evalTest(name:String, code:String, expected:Any) = test(name){ run(code, expected) }

  def evalWithDefsTest(name:String, code:String, expected:Any, defs:File) =
    test(name) { runWithDefs(code, expected, parseDefs(read(defs))) }

  def parseDefsTest(name:String, code:File, expected:Int) =
    test(name) {
      val res = parseDefs(read(code))
      //println(s"Parse Result:")
      //res.foreach(println)
      res.size == expected
    }

  def run(code:String, expected:Any): Boolean = {
    val actual = MineLang.run(code, TestServer.player)
    println(s"Result: $actual")
    actual == expected
  }

  def runWithDefs(code:String, expected:Any, defs:List[Def]): Boolean = {
    val actual = runProgram(Program(defs, parseExpr(read(code))), TestServer.player)
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
