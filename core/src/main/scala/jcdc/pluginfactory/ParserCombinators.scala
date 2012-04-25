package jcdc.pluginfactory

trait ParserCombinators[C] {

  type ParseContext = C

  case class ~[+A, +B](a: A, b: B) {
    override def toString = "(" + a + "~" + b + ")"
  }

  trait ParseResult[T]
  case class Failure[T](message: String) extends ParseResult[T]
  case class Success[T](t: T, rest: List[String]) extends ParseResult[T]

  trait Parser[T] { self =>
    def apply(p: ParseContext, args: List[String]): ParseResult[T]
    def describe: String

    def named(name: String) = new Parser[T] {
      def apply(p: ParseContext, args: List[String]) = self(p, args)
      def describe: String = name
    }

    def ^^[U](f: T => U) = new Parser[U] {
      def apply(p: ParseContext, args: List[String]): ParseResult[U] = self(p, args) match {
        case Failure(m) => Failure(m)
        case Success(t, rest) => Success(f(t), rest)
      }
      def describe = self.describe
    }

    def ~[U](p2: => Parser[U]) = new Parser[~[T, U]] {
      def apply(p: ParseContext, args: List[String]) = self(p, args) match {
        case Failure(m) => Failure(m)
        case Success(t: T, rest) => p2(p, rest) match {
          case Failure(m) => Failure(m)
          case Success(u, rest) => Success(new ~(t, u), rest)
        }
      }
      def describe = self.describe + "  " + p2.describe
    }

    def | (p2: => Parser[T]) = new Parser[T] {
      def apply(p: ParseContext, args: List[String]): ParseResult[T] = self(p, args) match {
        case Success(t, rest) => Success(t, rest)
        case Failure(m1) => p2(p, args) match {
          case Success(t, rest) => Success(t, rest)
          case Failure(m2) => Failure(m1 + " or " + m2)
        }
      }
      def describe = "(" + self.describe + " or " + p2.describe + ")"
    }

    def or[U](p2: => Parser[U]) = new Parser[Either[T, U]] {
      def apply(p: ParseContext, args: List[String]): ParseResult[Either[T, U]] = self(p, args) match {
        case Success(t, rest) => Success(Left(t), rest)
        case Failure(m1) => p2(p, args) match {
          case Success(u, rest) => Success(Right(u), rest)
          case Failure(m2) => Failure(m1 + " or " + m2)
        }
      }
      def describe = "(" + self.describe + " or " + p2.describe + ")"
    }

    def * : Parser[List[T]] = ((this+) | success(List[T]())).named(self.describe + "*")

    def + : Parser[List[T]] =
      ((this ~ (this *)) ^^ { case t ~ ts => t :: ts}).named(self.describe + "+")
  }

  def opt[T](parser: Parser[T]) = new Parser[Option[T]] {
    def apply(p: ParseContext, args: List[String]): ParseResult[Option[T]] = parser(p, args) match {
      case Failure(m) => Success(None: Option[T], args)
      case Success(t, rest) => Success(Some(t), rest)
    }
    def describe = "optional(" + parser.describe + ")"
  }

  def success[T](t: T) = new Parser[T] {
    def apply(p: ParseContext, args: List[String]) = Success(t, args)
    def describe = t.toString
  }

  implicit def stringToParser(s: String) = new Parser[String] {
    def apply(p: ParseContext, args: List[String]) = args match {
      case Nil => Failure("expected :" + s + ", but got nothing")
      case x :: xs => if (x == s) Success(x, xs) else Failure("expected: " + s + ", but got: " + x)
    }
    def describe = s
  }

  def token[T](name: String)(f: (ParseContext, String) => Option[T]) = new Parser[T] {
    def apply(p: ParseContext, args: List[String]) = args match {
      case Nil => Failure("expected " + name + ", got nothing")
      case x :: xs => f(p, x) match {
        case None => Failure("invalid " + name + ": " + x)
        case Some(t) => Success(t, xs)
      }
    }
    def describe = name
  }

  def anyString = token("string") { (_, s) => Some(s) }
  def even(n: Int) = n % 2 == 0
  def odd (n: Int) = !even(n)
  def tryNum(s: String)    = try Some(s.toInt) catch { case e => None }
  def num:     Parser[Int] = token("number") { (_, s) => tryNum(s) }
  def oddNum:  Parser[Int] = token("odd-number") { (_, s) => tryNum(s).filter(odd) }
  def evenNum: Parser[Int] = token("even-number") { (_, s) => tryNum(s).filter(even) }
}
