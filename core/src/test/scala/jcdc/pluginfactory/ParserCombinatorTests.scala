package jcdc.pluginfactory

import org.scalacheck.Properties
import org.scalacheck.Prop._

object ParserCombinatorTests extends Properties("ParserCombinatorTests") with ParserCombinators with TestHelpers {

  test("strings")(forAll { (s:String) => stringToParser(s)(List(s)).get ?= s })

  property("ints") = forAll { (i:Int) => int(List(i.toString)).get ?= i }

  property("any") = forAll { (i: Int,  s: String, l:List[String]) =>
    success((i, s))(l) ?= Success((i, s), l)
  }

  property("odd ok") = forAll { (i:Int) => (i % 2 == 1) ==>
    (oddNum(List(i.toString)).get ?= i)
  }

  property("even ok") = forAll { (i:Int) => (i % 2 == 0) ==>
    (evenNum(List(i.toString)).get ?= i)
  }

  property("odd bad") = forAll { (i:Int) => (i % 2 == 0) ==>
    (oddNum(List(i.toString)) ?= Failure(s"invalid odd-int: $i"))
  }

  property("even bad") = forAll { (i:Int) => (i % 2 == 1) ==>
    (evenNum(List(i.toString)) ?= Failure(s"invalid even-int: $i"))
  }

  property("+ ok") = forAll { (is:List[Int]) => (is.size > 0) ==>
    (int+(is.map(_.toString)) ?= Success(is, Nil))
  }

  property("+ on nil") = { int+(Nil) ?= Failure("expected input, but got nothing") }

  property("*") = forAll { (is:List[Int]) => int*(is.map(_.toString)) ?= Success(is, Nil) }

  property("int") = { int(List("wfwefw")) ?= Failure("invalid int: wfwefw")}

  property("int ~ int") = forAll { (i:Int, j: Int) =>
    (int ~ int)(List(i.toString, j.toString)).get ?= new ~(i, j)
  }
}