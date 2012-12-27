package jcdc.pluginfactory

import java.io.File
import util.Try

object ParserCombinators extends ParserCombinators

/**
 * Simple (relatively naive) parser combinator library.
 * I wouldn't actually recommend using them, I'd probably say go use Scala's first,
 * or another parser combinator library. However, they do work well, and are simple
 * to learn. The reason that I implemented my own is because I was unable to get
 * good self descriptions out of Scala's parses. These parsers are able to
 * explain to you the types of all the arguments that they take. This comes in
 * especially handy when giving error messages to users, and when generating
 * the commands section of the plugin.yml file for a plugin.
 */
trait ParserCombinators extends EnchrichedScalaClasses {

  case class ~[+A, +B](a: A, b: B) {
    override def toString = s"($a ~ $b)"
  }

  trait ParseResult[+T]{
    def get: T
    def fold[A](failF: String => A)(sucF: (T,List[String]) => A): A
  }
  case class Failure(message: String) extends ParseResult[Nothing]{
    def get: Nothing = throw new IllegalStateException("can't get from a failure")
    def fold[A](failF: String => A)(sucF: (Nothing,List[String]) => A): A = failF(message)
  }
  case class Success[+T](value: T, rest: List[String]) extends ParseResult[T]{
    def get: T = value
    def fold[A](failF: String => A)(sucF: (T,List[String]) => A): A = sucF(value, rest)
  }

  trait Parser[+T] extends (List[String] => ParseResult[T]) { self =>
    def apply(args: List[String]): ParseResult[T]
    def apply(args: String): ParseResult[T] = this.apply(args.split(" ").toList)
    def describe: String

    def named(name: String) = new Parser[T] {
      def apply(args: List[String]) = self(args)
      def describe: String = name
    }

    def flatMap[U](f: T => Parser[U]) = new Parser[U] {
      def apply(args: List[String]): ParseResult[U] =
        self(args).fold[ParseResult[U]](Failure(_))(f(_)(_))
      def describe = self.describe
    }

    def filter(f: T => Boolean) = filterWith(f)("invalid " + this.describe)
    def filterWith(f: T => Boolean)(message: String) = this.flatMap(t =>
      if(f(t)) success(t) else failure(message + ": " + t)
    )
    def map[U](f: T => U) = this.flatMap(t => success(f(t)))
    def ^^[U](f: T => U) = map(f)
    def ^^^[U](u: => U) = this ^^ (_ => u)
    def ~[U](p2: => Parser[U]): Parser[T ~ U] = for(t <- self; u <- p2) yield new ~(t, u)
    def |[U >: T] (p2: => Parser[U]) = this.or(p2) ^^ (_.fold(id, id))

    def or[U](p2: => Parser[U]) = new Parser[Either[T, U]] {
      def apply(args: List[String]): ParseResult[Either[T, U]] = self(args) match {
        case Success(t, rest) => Success(Left(t), rest)
        case Failure(m1)      => p2(args) match {
          case Success(u, rest) => Success(Right(u), rest)
          case Failure(m2)      => Failure(s"$m1 or $m2")
        }
      }
      def describe = s"(${self.describe} or ${p2.describe})"
    }

    def * : Parser[List[T]] = ((this+) | success(List[T]())).named(self.describe + "*")
    def + : Parser[List[T]] =
      ((this ~ (this *)) ^^ { case t ~ ts => t :: ts}).named(self.describe + "+")

    def ? = new Parser[Option[T]] {
      def apply(args: List[String]): ParseResult[Option[T]] =
        self(args).fold(m => Success(None: Option[T], args))((t, rest) => Success(Some(t), rest))
      def describe = s"optional(${self.describe})"
    }

    def ~>[U](p2:Parser[U]): Parser[U] =
      (for (t <- this; u <- p2) yield u) named (self.describe + " ~> " + p2.describe)
    def <~[U](p2:Parser[U]): Parser[T] =
      (for (t <- this; u <- p2) yield t) named (self.describe + " <~ " + p2.describe)

    def repSep[U](p2: Parser[U]): Parser[List[T]] =
      (self ~ (p2 ~> self).*) ^^ { case (t ~ ts) => t :: ts }

    def debug: Parser[T] = new Parser[T] {
      def apply(args: List[String]) = {
        println(s"applying ${self.describe} to $args")
        val res = self(args)
        println(s"got: $res")
        res
      }
      def describe = self.describe
    }
  }

  def success[T](t: T) = new Parser[T] {
    def apply(args: List[String]) = Success(t, args)
    def describe = t.toString
  }

  def failure[T](message: String) = new Parser[T] {
    def apply(args: List[String]) = Failure(message)
    def describe = message
  }

  implicit def stringToParser(s: String): Parser[String] =
    anyString.filterWith(_ == s)(s"expected: $s") named s

  def maybe[T](name: String)(f: String => Option[T]): Parser[T] = (for {
    s <- anyString; res <- f(s).fold[Parser[T]](failure(s"invalid $name: $s"))(success(_))
  } yield res) named name

  def attempt[T](name: String)(f: String => T): Parser[T] =
    (anyString named name).flatMap{s => Try(success(f(s))).getOrElse(failure(s"invalid $name: $s"))}

  def noArguments = new Parser[Unit] {
    def apply(args: List[String]) = args match {
      case Nil => Success((), Nil)
      case _   => Failure(s"expected no arguments, got ${args.mkString(" ")}")
    }
    def describe = "nothing"
  }

  def anyString: Parser[String] = new Parser[String] {
    def apply(args: List[String]) = args match {
      case Nil => Failure(s"expected input, but got nothing")
      case x :: xs => Success(x, xs)
    }
    def describe = "string"
  }

  val slurp: Parser[String] = (anyString.* ^^ (_.mkString(" "))) named "slurp"

  // number parsers
  val int:     Parser[Int]  = attempt("int")(_.toInt)
  val oddNum:  Parser[Int]  = (int named "odd-int" ).filter(_.isOdd)
  val evenNum: Parser[Int]  = (int named "even-int").filter(_.isEven)
  val long:    Parser[Long] = attempt("long")(_.toLong)

  val bool:        Parser[Boolean] = attempt("boolean")(_.toBoolean)
  val boolOrTrue:  Parser[Boolean] = bool.? ^^ { _ getOrElse true }
  val boolOrFalse: Parser[Boolean] = bool.? ^^ { _ getOrElse false }

  // file parsers
  val file   : Parser[File] = anyString ^^ (new File(_))
  val newFile: Parser[File] = attempt("new-file"){ s => val f = new File(s); f.createNewFile; f }
  val existingFile     : Parser[File] = file.filter(_.exists) named "existing-file"
  val existingOrNewFile: Parser[File] = existingFile | newFile
}


// TODO: review these and maybe fix up later
//  def slurpUntil(delim:Char): Parser[String] = new Parser[String] {
//    def apply(args: List[String]) = {
//      val all = args.mkString(" ")
//      val (l,r) = all.partition(_ != delim)
//      if(l.isEmpty) Failure(s"didn't find: $delim in: $all")
//      else Success(l, r.drop(1).split(" ").toList)
//    }
//    def describe = s"slurp until: $delim"
//  }
//
//  implicit def charToParser(c:Char): Parser[Char] = matchChar(c)
//
//  def matchChar(c:Char): Parser[Char] = new Parser[Char] {
//    def apply(args: List[String]) = args match {
//      case Nil => Failure(s"didn't find: $c")
//      case x :: xs =>
//        if(x.startsWith(c.toString)) Success(c, x.drop(1) :: xs)
//        else Failure(s"expected: $c, but got: ${x.take(1)}")
//    }
//    def describe = s"slurp until: $c"
//  }

