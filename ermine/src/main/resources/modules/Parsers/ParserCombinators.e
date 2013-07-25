module Parsers.ParserCombinators where

import Bool
import Control.Functor
import Control.Monad
import Either
import Function hiding (|)
import List hiding or
import Native.Object using toString
import Maybe
import String as String

data Parser a = Parser ([String] -> ParseResult a) String

parserFunctor = monadFunctor parserMonad

parserMonad = Monad success bind' where
  bind' (Parser p name) f = Parser (args -> fold (p args) Failure (runParser . f)) name

runParser : Parser a -> [String] -> ParseResult a
runParser (Parser f _) = f

nameOf : Parser a -> String
nameOf (Parser _ name) = name

rename : Parser a -> String -> Parser a
rename (Parser f _) name = Parser f name

success : a -> Parser a
success a = Parser (Success a) (toString a)

failure : String -> Parser a
failure msg = Parser (_ -> Failure msg) msg

data ParseResult a = Failure String | Success a [String]

getOrElse : ParseResult a -> a -> a
getOrElse (Failure m)   a = a
getOrElse (Success a _) _ = a

fold : ParseResult a -> (String -> b) -> (a -> [String] -> b) -> b
fold (Failure m)      failF _    = failF m
fold (Success a rest) _     sucF = sucF a rest

mapP = fmap parserFunctor
bindP = bind parserMonad
infixl 5 >>=
(>>=) = bindP

infixl 5 ^^
(^^) = mapP

infixl 3 ^^^
(^^^) b = mapP (_ -> b)

filterP f p = filterWithP f (_ -> "invalid " ++_String (nameOf p)) p

filterWithP f errorF p = bindP p (a -> (if (f p) (success a) (failure (errorF a))))

infixl 5 :&
data And a b = (:&) a b
--override def toString = s"($a ~ $b)"

infixl 5 &
(&) : Parser a -> Parser b -> Parser (And a b)
(&) p1 p2 = rename (p1 >>= (a -> mapP (b -> a :& b) p2)) ((nameOf p1) ++_String " " ++_String (nameOf p2))

infixl 5 |
(|) = or
or : Parser a -> Parser b -> Parser (Either a b)
or (Parser f1 name1) (Parser f2 name2) = Parser f' n' where
  f' args = case (f1 args) of
    (Success a rest) -> Success (Left a) rest
    (Failure m1)     -> case (f2 args) of
                          (Success b rest) -> Success (Right b) rest
                          (Failure m2)     -> Failure (m1 ++_String " or " ++_String m2)
  n' = name1 ++_String " or " ++_String name2

{--
zeroOrMore p = rename (oneOrMore p | success []) (nameOf p ++_String "*")

oneOrMore p = rename (p & (zeroOrMore p) ^^ f') (nameOf p ++_String "+") where f' (a :& as) = a :: as

maybeP : Parser a -> Parser (Maybe a)
maybeP (Parser f n) = Parser f' n' where
  f' args = fold (f args) (_ -> Success Nothing args) (t rest -> Success (Just t) rest)
  n' = "optional(" ++_String n ++_String ")"
--}

{--
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
      case _   => Failure(s"unprocessed input: ${args.mkString(" ")}")
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

//at jcdc.pluginfactory.ParserCombinators$Parser$class.$tilde(ParserCombinators.scala:96)
//at jcdc.pluginfactory.ParserCombinators$$anon$9.$tilde(ParserCombinators.scala:230)
//at jcdc.pluginfactory.ParserCombinators$Parser$class.$plus(ParserCombinators.scala:134)
//at jcdc.pluginfactory.ParserCombinators$$anon$9.$plus(ParserCombinators.scala:230)
//at jcdc.pluginfactory.ParserCombinators$Parser$class.$times(ParserCombinators.scala:125)
--}