package jcdc.pluginfactory.examples

import scala.tools.reflect.ToolBox
import scala.reflect.runtime.{currentMirror=>m}

object Eval {
  def parseAndEval(code:String)= {
    val tb   = m.mkToolBox()
    val tree = tb.parseExpr(code)
    tb.runExpr(tree)
  }

  def eval(code:String)= {
    val tb = m.mkToolBox()
    tb.runExpr(tb.parseExpr(code))
  }
}
