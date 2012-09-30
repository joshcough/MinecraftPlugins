//package jcdc.pluginfactory.examples
//
//import scala.tools.reflect.ToolBox
//import scala.reflect.runtime.{currentMirror=>m}
//
////http://stackoverflow.com/questions/11055210/whats-the-easiest-way-to-use-reify-get-an-ast-of-an-expression-in-scala
//object Eval {
//  def parseAndEval(code:String)= {
//    val tb   = m.mkToolBox()
//    val tree = tb.parseExpr(code)
//    tb.runExpr(tree)
//  }
//
//  def eval(code:String)= {
//    val tb = m.mkToolBox()
//    tb.runExpr(tb.parseExpr(code))
//  }
//}
