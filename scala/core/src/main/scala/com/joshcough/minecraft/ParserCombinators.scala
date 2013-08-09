package com.joshcough.minecraft

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
trait ParserCombinators extends ScalaEnrichment {

  case class ~[+A, +B](a: A, b: B) { override def toString = s"($a ~ $b)" }

  trait ParseResult[+T]{
    def get: T
    def extract[A](pf: PartialFunction[T, A])
    def fold[A](failF: String => A)(sucF: (T,List[String]) => A): A
  }
  case class Failure(message: String) extends ParseResult[Nothing]{
    def get: Nothing = throw new IllegalStateException(message)
    def extract[A](pf: PartialFunction[Nothing, A]) = get
    def fold[A](failF: String => A)(sucF: (Nothing,List[String]) => A): A = failF(message)
  }
  case class Success[+T](value: T, rest: List[String]) extends ParseResult[T]{
    def get: T = value
    def extract[A](pf: PartialFunction[T, A]) = pf(get)
    def fold[A](failF: String => A)(sucF: (T,List[String]) => A): A = sucF(value, rest)
  }

  object ParserMonad {
    def unit[T](t: T) = new Parser[T] {
      def apply(args: List[String]) = Success(t, args)
      def describe = t.toString
    }
    def bind[T, U](p: Parser[T])(f: T => Parser[U]) = new Parser[U] {
      def apply(args: List[String]): ParseResult[U] =
        p(args).fold[ParseResult[U]](Failure)(f(_)(_))
      def describe = p.describe
    }
    def map[T, U](p: Parser[T])(f: T => U) = bind(p)(t => unit(f(t)))
  }

  trait Parser[+T] extends (List[String] => ParseResult[T]) { self =>
    def apply(args: List[String]): ParseResult[T]
    def apply(args: String): ParseResult[T] = this.apply(args.split(" ").toList)
    def describe: String

    /**
     * Create a new parser that when ran, if it succeeds,
     * take its result and map it over f.
     */
    def map[U](f: T => U) = ParserMonad.map(this)(f)

    /**
     * Operator alias for map.
     */
    def ^^[U](f: T => U) = map(f)

    /**
     * Create a new parser that when ran, if it succeeds,
     * throws out its result and returns u instead.
     */
    def ^^^[U](u: => U) = map (_ => u)

    /**
     * Monadic bind for a Parser. Used for chaining parsers together in for comprehensions,
     * so that you can run N of them in a row, short circuiting if any fail.
     */
    def flatMap[U](f: T => Parser[U]) = ParserMonad.bind(this)(f)

    /**
     * Create a new parser that when ran, if it succeeds,
     * then checks to see if its result also passes the given predicate.
     * If it does not, then the parser fails.
     */
    def filter(f: T => Boolean): Parser[T] = filterWith(f)(_ => "invalid " + describe)

    /**
     * Same as filter, but allows for supplying a custom failure message
     * if the result fails the predicate.
     */
    def filterWith(f: T => Boolean)(errMsgF: T => String): Parser[T] =
      flatMap(t => if(f(t)) success(t) else failure(errMsgF(t) + ": " + t)) named describe

    /**
     * Creates a new parser that chains two parsers together (this, and p2).
     * The second parser works on the remaining input after the first parser is ran.
     * If they both succeed, a compound result is returned: ~[T,U]
     */
    def ~[U](p2: => Parser[U]): Parser[T ~ U] =
      (for(t <- self; u <- p2) yield new ~(t, u)) named s"${self.describe} ${p2.describe}"

    /**
     * Creates a new parser that will succeed if either this, or p2 succeed.
     * If this parser fails, then p2 will run on the same input.
     * If this succeeds, Left(some t) is returned, and
     * if p2 succeeds, Right(some u) is returned.
     */
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

    /**
     * Same as or, but where U is the same type as T, basically.
     */
    def |[U >: T] (p2: => Parser[U]) = or(p2) ^^ (_.fold(id, id))

    /**
     * Return a new parser that will parse zero or more of whatever this parser parses.
     * The results are returned in a list.
     * If zero parse successfully, Nil is returned.
     */
    def * : Parser[List[T]] = (this.+ | success(List[T]())).named(describe + "*")

    /**
     * Return a new parser that will parse one or more of whatever this parser parses.
     * The results are returned in a list.
     * If zero parse successfully, the parser fails.
     * @return
     */
    def + : Parser[List[T]] =
      ((this ~ this.*) ^^ { case t ~ ts => t :: ts }).named(describe + "+")

    /**
     * Return a new parser that will attempt to parse input (T), and returns an Option[T]
     * If parsing fails, None is returned. If parsing succeeds, Some(t) is returned.
     */
    def ? = new Parser[Option[T]] {
      def apply(args: List[String]): ParseResult[Option[T]] =
        self(args).fold(m => Success(None: Option[T], args))((t, rest) => Success(Some(t), rest))
      def describe = s"optional(${self.describe})"
    }

    /**
     * Creates a new parser that chains two parsers together (this, and p2).
     * The second parser works on the remaining input after the first parser is ran.
     * If they both succeed, the result of the second parser is returned (U),
     * and the result of the first parser is thrown out.
     */
    def ~>[U](p2:Parser[U]): Parser[U] =
      (for (t <- this; u <- p2) yield u) named (self.describe + " ~> " + p2.describe)

    /**
     * Creates a new parser that chains two parsers together (this, and p2).
     * The second parser works on the remaining input after the first parser is ran.
     * If they both succeed, the result of the first parser is returned (T),
     * and the result of the second parser is thrown out.
     */
    def <~[U](p2:Parser[U]): Parser[T] =
      (for (t <- this; u <- p2) yield t) named (self.describe + " <~ " + p2.describe)

    def repSep[U](p2: Parser[U]): Parser[List[T]] =
      (self ~ (p2 ~> self).*) ^^ { case t ~ ts => t :: ts }

    /**
     * Rename this parser the given string.
     */
    def named(name: => String) = new Parser[T] {
      def apply(args: List[String]) = self(args)
      def describe: String = name
    }

    /**
     * Returns a new parser that prints debugging information before and after parsing the input.
     */
    def debug: Parser[T] = new Parser[T] {
      def apply(args: List[String]) = {
        println(s"applying $describe to $args")
        val res = self(args)
        println(s"got: $res")
        res
      }
      def describe = self.describe
    }
  }

  /**
   * Monadic return for Parsers. Create a parser that always succeeds, returning t.
   */
  def success[T](t: T) = ParserMonad.unit(t)

  /**
   * Create a parser that always fails with the given error message.
   */
  def failure[T](message: String) = new Parser[T] {
    def apply(args: List[String]) = Failure(message)
    def describe = message
  }

  /**
   * Convert a String into a Parser that accepts only that String as valid input.
   */
  implicit def stringToParser(s: String): Parser[String] =
    anyStringAs(s).filterWith(_ == s)(_ => s"expected: $s") named s

  /**
   * Create a parser from an operation that parses a String and returns Option[T].
   * If that parsing operation returns None, then the parser fails.
   * If the parsing operation returns Some(t), then the t is the result.
   */
  def maybe[T](name: String)(f: String => Option[T]): Parser[T] = for {
    s <- anyStringAs(name); res <- f(s).fold[Parser[T]](failure(s"invalid $name: $s"))(success)
  } yield res

  /**
   * Create a parser from an operation that may fail (with an exception).
   * If it does not throw, then the parser succeeds, returning the result.
   * If an exception is thrown, it is caught, and the parser fails.
   */
  def attempt[T](name: String)(f: String => T): Parser[T] =
    anyStringAs(name).flatMap(s => Try(success(f(s))).getOrElse(failure(s"invalid $name: $s")))

  /**
   * A parser that succeeds as long as there is
   * at least one string remaining in the input.
   * @param name the description for this parser
   */
  def anyStringAs(name: String): Parser[String] = new Parser[String] {
    def apply(args: List[String]) = args match {
      case Nil => Failure(s"expected $name, but got nothing")
      case x :: xs => Success(x, xs)
    }
    def describe = name
  }

  /**
   * Create a parser that succeeds only if there is no input remaining to be parsed.
   */
  val eof = new Parser[Unit] {
    def apply(args: List[String]) = args match {
      case Nil => Success((), Nil)
      case _   => Failure(s"expected eof, but got: ${args.mkString(" ")}")
    }
    def describe = "EOF"
  }

  /**
   * Alias for eof
   */
  val noArguments = eof

  /**
   * Alias for eof
   */
  val nothing     = eof

  /**
   * Alias for eof
   */
  val empty       = eof

  /**
   * A parser that succeeds as long as there is
   * at least one string remaining in the input.
   */
  val anyString: Parser[String] = anyStringAs("string")

  /**
   * A parser that consumes the rest of the input, return it all back in one string.
   */
  val slurp: Parser[String] = (anyString.* ^^ (_.mkString(" "))) named "slurp"

  /**
   * A parser that consumes the rest of the input
   */
  val remainingArgs: Parser[List[String]] = new Parser[List[String]] {
    def apply(args: List[String]) = Success(args, Nil)
    def describe = "remainingArgs"
  }

  // number parsers
  val int:     Parser[Int]    = attempt("int")   (_.toInt)
  val long:    Parser[Long]   = attempt("long")  (_.toLong)
  val double:  Parser[Double] = attempt("double")(_.toDouble)
  val short:   Parser[Short]  = attempt("short") (_.toShort)
  val float:   Parser[Float]  = attempt("float") (_.toFloat)
  val oddNum:  Parser[Int]    = (int named "odd-int" ).filter(_.isOdd)
  val evenNum: Parser[Int]    = (int named "even-int").filter(_.isEven)
  val binary:  Parser[String] = ("1" | "0").+ ^^ (_.mkString)
  // bool parsers
  val bool:        Parser[Boolean] = attempt("boolean")(_.toBoolean)
  val boolOrTrue:  Parser[Boolean] = bool | success(true)
  val boolOrFalse: Parser[Boolean] = bool | success(false)

  // file parsers
  // TODO: fix so that filenameP only accepts valid file names.
  val filenameP: Parser[String] = anyStringAs("file")
  val file   : Parser[File] = filenameP ^^ (new File(_))
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

//at com.joshcough.minecraft.ParserCombinators$Parser$class.$tilde(ParserCombinators.scala:96)
//at com.joshcough.minecraft.ParserCombinators$$anon$9.$tilde(ParserCombinators.scala:230)
//at com.joshcough.minecraft.ParserCombinators$Parser$class.$plus(ParserCombinators.scala:134)
//at com.joshcough.minecraft.ParserCombinators$$anon$9.$plus(ParserCombinators.scala:230)
//at com.joshcough.minecraft.ParserCombinators$Parser$class.$times(ParserCombinators.scala:125)
