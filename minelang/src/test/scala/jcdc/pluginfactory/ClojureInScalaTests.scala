package jcdc.pluginfactory

import org.scalacheck.Properties
import org.scalacheck.Prop.secure
import ClojureInScala._

case class Point2D(x:Int, y:Int){
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

object ClojureInScalaTests extends Properties("MinecraftParserTests") with BukkitEnrichment with TestHelpers {

  evalTest("constructorCall1", "(new jcdc.pluginfactory.Point2D 5 6)",                Point2D(5,6))
  evalTest("constructorCall2", "(new jcdc.pluginfactory.Point2D 5 6 nil)",            Point2D(5,6))
  evalTest("instanceCall0",    "(.toString (new jcdc.pluginfactory.Point2D 5 6))",    "(5,6)")
  evalTest("instanceCall1",    "(.invoke1 (new jcdc.pluginfactory.Point2D 5 6) 0)",   "6")
  evalTest("instanceCall2",    "(.invoke2 (new jcdc.pluginfactory.Point2D 5 6) 0)",   "6")
  evalTest("instanceCall3",    "(.invoke3 (new jcdc.pluginfactory.Point2D 5 6) 0 0)", "6")
  // TODO: this passes in java 6, but fails in 7!
  evalTest("staticCall1",      "(java.lang.String/valueOf 5)",                      "5")
  evalTest("staticField1",     "java.lang.Math/PI",                                 Math.PI)
  evalTest("lamTest",          "((lam (x) x) 7)",                                   7)
  evalTest("invokeWithFun1a",  "(.invokeFun1a (new jcdc.pluginfactory.Point2D 5 6) (lam (x) x))",         7)
  evalTest("invokeWithFun1b",  "(.invokeFun1b (new jcdc.pluginfactory.Point2D 5 6) (lam (x) (+ x x)))",   16)
  evalTest("invokeWithFun2a",  "(.invokeFun2a (new jcdc.pluginfactory.Point2D 5 6) (lam (x y) (* 9 8)))", 72)
  evalTest("let1 test",        "(let (a 5) a)",                       5)
  evalTest("let2 test",        "(let (a 5) (+ a a))",                 10)
  evalTest("letNested test",   "(let (a 5) (let (a 10) 10))",         10)
  evalTest("let* test 1",      "(let* ((a 5) (b 6)) (+ a b))",        11)
  evalTest("let* test 2",      "(let* ((a 5) (b (+ a 2))) (+ a b))",  12)
  evalTest("isa? test 1",      """(isa? "hi" java.lang.String)""",    true)
  evalTest("access empty",     s"empty",                              Nil)
  evalTest("apply cons",       s"(cons 1 empty)",                     List(1))
  evalTest("list map"  ,  "(map (lam (x) (* x x)) (cons 1 (cons 2 (cons 3 empty))))",  List(1, 4, 9))

  def evalTest(name:String, code:String, expected:Any) = test("name"){
    val actual = Session.withStdLib().runExpr(code)._1._2
    println(s"Result: $actual")
    actual == expected
  }
}
