package jcdc.pluginfactory

import org.scalacheck.Properties
import org.scalacheck.Prop._

object ParserCombinatorTests extends Properties("ParserCombinatorTests") with Compare {

  object u extends ParserCombinators[Unit]{}
  import u.{Success, Failure}
  def suc[T](t:T, l:List[String] = Nil) = Success(t, (), l)

  property("strings") = forAll { (s:String) =>
    u.stringToParser(s)((), List(s)) == suc(s)
  }

  property("ints") = forAll { (i:Int) =>
    u.num((), List(i.toString)) == suc(i)
  }

  property("any") = forAll { (i: Int,  s: String, l:List[String]) =>
    u.success((i, s))((), l) == suc((i, s), l)
  }

  property("odd good") = forAll { (i:Int) => (i % 2 == 1) ==>
    (u.oddNum((), List(i.toString)) == suc(i))
  }

  property("even good") = forAll { (i:Int) => (i % 2 == 0) ==>
    (u.evenNum((), List(i.toString)) == suc(i))
  }

  property("odd bad") = forAll { (i:Int) => (i % 2 == 0) ==>
    (u.oddNum((), List(i.toString)) == Failure(s"invalid odd-number: $i"))
  }

  property("even bad") = forAll { (i:Int) => (i % 2 == 1) ==>
    (u.evenNum((), List(i.toString)) == Failure(s"invalid even-number: $i"))
  }

  compare("6", 6, 6)
}