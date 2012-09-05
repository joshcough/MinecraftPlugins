package jcdc.pluginfactory

import org.scalacheck.Properties
import org.scalacheck.Prop._

object ParserCombinatorTests extends Properties("ParserCombinatorTests") {

  object p extends ParserCombinators[Unit]{}
  import p.{Success, Failure}
  def suc[T](t:T, l:List[String]=Nil) = Success(t, (), l)

  property("strings") = forAll { (s:String) => p.stringToParser(s)((), List(s)).get ?= s }

  property("ints") = forAll { (i:Int) => p.num((), List(i.toString)).get ?= i }

  property("any") = forAll { (i: Int,  s: String, l:List[String]) =>
    p.success((i, s))((), l) ?= suc((i, s), l)
  }

  property("odd ok") = forAll { (i:Int) => (i % 2 == 1) ==>
    (p.oddNum((), List(i.toString)).get ?= i)
  }

  property("even ok") = forAll { (i:Int) => (i % 2 == 0) ==>
    (p.evenNum((), List(i.toString)).get ?= i)
  }

  property("odd bad") = forAll { (i:Int) => (i % 2 == 0) ==>
    (p.oddNum((), List(i.toString)) ?= Failure(s"invalid odd-number: $i"))
  }

  property("even bad") = forAll { (i:Int) => (i % 2 == 1) ==>
    (p.evenNum((), List(i.toString)) ?= Failure(s"invalid even-number: $i"))
  }

  property("+ ok") = forAll { (is:List[Int]) => (is.size > 0) ==>
    (p.num+((), is.map(_.toString)) ?= suc(is))
  }

  property("+ on nil") = { p.num+((), Nil) ?= Failure("expected number, got nothing") }

  property("*") = forAll { (is:List[Int]) => p.num*((), is.map(_.toString)) ?= suc(is) }
}