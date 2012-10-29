package jcdc.pluginfactory

import org.scalacheck.Properties
import org.scalacheck.Prop.secure
import MineLang._
import java.io.File

case class Point(x:Int, y:Int){
  def this(x:java.lang.Integer, y:java.lang.Integer, z:Unit) = this(x, y)
  override def toString = s"($x,$y)"
  def invoke1(i:java.lang.Integer) = "6"
  def invoke2(i:Int) = "6"
  def invoke3(i:Int, i2:java.lang.Integer)  = "6"
  def invoke4(i:Int, i2:java.lang.Integer*) = "6"
  def invokeFun1a(f: Any => Int) = f(7)
  def invokeFun1b(f: Int => Int) = f(8)
  def invokeFun2a(f: (Int, Int) => Int) = f(8, 9)
}

object MineLangTests extends Properties("MinecraftParserTests") with EnrichmentClasses {

  val mineLangDir = new File("../minelang/")

  val constructorCall1 = """((new jcdc.pluginfactory.Point 5 6))"""
  val constructorCall2 = """((new jcdc.pluginfactory.Point 5 6 unit))"""
  val instanceCall0    = """((.toString (new jcdc.pluginfactory.Point 5 6)))"""
  val instanceCall1    = """((.invoke1 (new jcdc.pluginfactory.Point 5 6) 0))"""
  val instanceCall2    = """((.invoke2 (new jcdc.pluginfactory.Point 5 6) 0))"""
  val instanceCall3    = """((.invoke3 (new jcdc.pluginfactory.Point 5 6) 0 0))"""
  val staticCall1      = """((java.lang.String/valueOf 5))"""
  val staticField1     = """(java.lang.Math/PI)"""
  val lamTest          = """(((lam (x) x) 7))"""
  val invokeWithFun1a  = """((.invokeFun1a (new jcdc.pluginfactory.Point 5 6) (lam (x) x)))"""
  val invokeWithFun1b  = """((.invokeFun1b (new jcdc.pluginfactory.Point 5 6) (lam (x) (+ x x))))"""
  val invokeWithFun2a  = """((.invokeFun2a (new jcdc.pluginfactory.Point 5 6) (lam (x y) (* 9 8))))"""
  val listMap          = """((.map (cons 1 (cons 2 (cons 3 nil))) (lam (x) (* x x))))"""
  val let1             = """((let (a 5) a))"""
  val let2             = """((let (a 5) (+ a a)))"""
  val letNested        = """((let (a 5) (let (a 10) 10)))"""
  val letStar          = """((let* ((a 5) (b 6)) (+ a b)))"""

  // simple java interop tests
  evalTest("constructorCall1", constructorCall1, ObjectValue(Point(5,6)))
  evalTest("constructorCall2", constructorCall2, ObjectValue(Point(5,6)))
  evalTest("instanceCall0",    instanceCall0,    ObjectValue("(5,6)"))
  evalTest("instanceCall1",    instanceCall1,    ObjectValue("6"))
  evalTest("instanceCall2",    instanceCall2,    ObjectValue("6"))
  evalTest("instanceCall3",    instanceCall3,    ObjectValue("6"))
  evalTest("staticCall1",      staticCall1,      ObjectValue("5"))
  evalTest("staticField1",     staticField1,     ObjectValue(Math.PI))
  evalTest("lamTest",          lamTest,          ObjectValue(7))
  evalTest("invokeWithFun1a",  invokeWithFun1a,  ObjectValue(7))
  evalTest("invokeWithFun1b",  invokeWithFun1b,  ObjectValue(16))
  evalTest("invokeWithFun2a",  invokeWithFun2a,  ObjectValue(72))
  evalTest("access nil",  s"(nil)",  ObjectValue(Nil))
  evalTest("apply cons",  s"((cons 1 nil))",     ObjectValue(List(1)))
  evalTest("let1 test",        let1,             ObjectValue(5))
  evalTest("let2 test",        let2,             ObjectValue(10))
  evalTest("letNested test",   letNested,        ObjectValue(10))
  evalTest("let* test",        letStar,          ObjectValue(11))

  // TODO: failing
  //evalTest("list map"  ,  listMap,  ObjectValue(List(1, 4, 9)))


  val expandMc = mineLangDir.child("expand.mc")
  parseDefsTest("expand defs parse", expandMc, 0)
  evalTest("expand", expandMc.slurp,
    ObjectValue(Cube(TestServer.world(12,3,12), TestServer.world(-2,3,-2)))
  )

  val factorialDefs = mineLangDir.child("factorial.mc")
  parseDefsTest("factorial defs parse", factorialDefs, 2)
  evalWithDefsTest("factorial defs eval", "(test)", ObjectValue(120), factorialDefs)

  val houseDefs = mineLangDir.child("house.mc")
  parseDefsTest("house defs parse", houseDefs, 10)
  evalWithDefsTest("house defs eval", "(city)", UnitValue, houseDefs)

  def evalTest(name:String, code:String, expected:Value) =
    property(name) = secure { run(code, expected) }

  def evalWithDefsTest(name:String, code:String, expected:Value, defs:File) =
    property(name) = secure { runWithDefs(code, expected, parseDefs(read(defs))) }

  def parseDefsTest(name:String, code:File, expected:Int) =
    property(name) = secure {
      attemptThrowable {
        val res = MineLang.parseDefs(read(code))
        //println(s"Parse Result:")
        //res.foreach(println)
        res.size == expected
      }
    }

  def run(code:String, expected:AnyRef): Boolean = attemptThrowable {
    val actual = MineLang.run(code, TestServer.player)
    println(s"Result: $actual")
    actual == expected
  }

  def runWithDefs(code:String, expected:AnyRef, defs:List[Def]): Boolean = attemptThrowable {
    val actual = runProgram(Program(defs, parseExpr(read(code))), TestServer.player)
    println(s"Result: $actual")
    actual == expected
  }

  def attemptThrowable[T](f: => T) = try f catch {
    case e: Throwable  => e.printStackTrace; throw e
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
