package jcdc.pluginfactory

import java.io.File
import util.Try

trait ParserCombinators {

  case class ~[+A, +B](a: A, b: B) {
    override def toString = s"($a ~ $b)"
  }

  trait ParseResult[+T]{
    def get: T
    def map[U](f: T => U): ParseResult[U]
    def flatMapWithNext[U](f: (T, List[String]) => ParseResult[U]): ParseResult[U]
    def mapFailure[U >: T](f: String => ParseResult[U]): ParseResult[U]
  }
  case class Failure(message: String) extends ParseResult[Nothing]{
    def get: Nothing = throw new IllegalStateException("can't get from a failure")
    def map[U](f: Nothing => U) = this
    def flatMapWithNext[U](f: (Nothing, List[String]) => ParseResult[U]) = this
    def mapFailure[U >: Nothing](f: String => ParseResult[U]) = f(message)
  }
  case class Success[+T](value: T, rest: List[String]) extends ParseResult[T]{
    def get: T = value
    def map[U](f: T => U): ParseResult[U] = Success(f(value), rest)
    def flatMapWithNext[U](f: (T, List[String]) => ParseResult[U]) = f(value, rest)
    def mapFailure[U >: T](a: String => ParseResult[U]): ParseResult[U] = this
  }

  trait Parser[+T] extends (List[String] => ParseResult[T]) { self =>
    def apply(args: List[String]): ParseResult[T]
    def describe: String

    def named(name: String) = new Parser[T] {
      def apply(args: List[String]) = self(args)
      def describe: String = name
    }

    def ^^[U](f: T => U) = new Parser[U] {
      def apply(args: List[String]): ParseResult[U] = self(args) map f
      def describe = self.describe
    }

    def ^^^[U](u: => U) = new Parser[U] {
      def apply(args: List[String]): ParseResult[U] = self(args) map (_ => u)
      def describe = self.describe
    }

    def ~[U](p2: => Parser[U]) = new Parser[~[T, U]] {
      def apply(args: List[String]) = self(args) flatMapWithNext {
        (t, rest) => p2(rest) map { u => new ~(t, u) }
      }
      def describe = self.describe + "  " + p2.describe
    }

    def |[U >: T] (p2: => Parser[U]) = new Parser[U] {
      def apply(args: List[String]): ParseResult[U] = self(args) mapFailure { m1 =>
        p2(args) mapFailure { m2 => Failure(s"$m1 or $m2") }
      }
      def describe = s"(${self.describe} or ${p2.describe})"
    }

    def or[U](p2: => Parser[U]) = new Parser[Either[T, U]] {
      def apply(args: List[String]): ParseResult[Either[T, U]] =
        self(args) map (Left(_)) mapFailure { m1 => p2(args) map (Right(_)) mapFailure { m2 =>
          Failure(s"$m1 or $m2")
        }
      }
      def describe = s"(${self.describe} or ${p2.describe})"
    }

    def * : Parser[List[T]] = ((this+) | success(List[T]())).named(self.describe + "*")

    def + : Parser[List[T]] =
      ((this ~ (this *)) ^^ { case t ~ ts => t :: ts}).named(self.describe + "+")
  }

  def opt[T](parser: Parser[T]) = new Parser[Option[T]] {
    def apply(args: List[String]): ParseResult[Option[T]] = parser(args) match {
      case Failure(m) => Success(None: Option[T], args)
      case Success(t, rest) => Success(Some(t), rest)
    }
    def describe = s"optional(${parser.describe})"
  }

  def success[T](t: T) = new Parser[T] {
    def apply(args: List[String]) = Success(t, args)
    def describe = t.toString
  }

  implicit def stringToParser(s: String) = new Parser[String] {
    def apply(args: List[String]) = args match {
      case Nil => Failure(s"expected :$s, but got nothing")
      case x :: xs => if (x == s) Success(x, xs) else Failure(s"expected: $s, but got: $x")
    }
    def describe = s
  }

  def token[T](name: String)(f: (String) => Option[T]) = new Parser[T] {
    def apply(args: List[String]) = args match {
      case Nil => Failure(s"expected $name, got nothing")
      case x :: xs => f(x) match {
        case None => Failure(s"invalid $name: $x")
        case Some(t) => Success(t, xs)
      }
    }
    def describe = name
  }

  def anyString = token("string") { s => Some(s) }
  def tryOption[T](f: => T): Option[T] = Try(Option(f)).getOrElse(None)

  // number parsers
  def even(n: Int) = n % 2 == 0
  def odd (n: Int) = !even(n)
  def tryNum(s: String)     = tryOption(s.toInt)
  def num:     Parser[Int]  = token("number") { s => tryNum(s) }
  def oddNum:  Parser[Int]  = token("odd-number") { s => tryNum(s).filter(odd) }
  def evenNum: Parser[Int]  = token("even-number") { s => tryNum(s).filter(even) }
  def long:    Parser[Long] = token("long") { s => tryOption(s.toLong) }

  def bool:    Parser[Boolean] = token("boolean") { s => tryOption(s.toBoolean) }
  def boolOrTrue: Parser[Boolean]  = opt(bool) ^^ { ob => ob.getOrElse(true) }
  def boolOrFalse: Parser[Boolean] = opt(bool) ^^ { ob => ob.getOrElse(false) }

  // file parsers
  def file:    Parser[File] = token("file") { s => Some(new File(s)) }
  def newFile: Parser[File] = token("new-file"){ s => tryOption {
    val f = new File(s)
    f.createNewFile()
    f
  }}
  // todo, maybe deal with exception handling here...
  def existingFile: Parser[File] = token("existing-file"){ s =>
    val f = new File(s)
    if (f.exists) Some(f) else None
  }
  def existingOrNewFile: Parser[File] = existingFile | newFile
}
