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

data Parser a = Parser (List String -> ParseResult a) String

parserFunctor = monadFunctor parserMonad

parserMonad = Monad success bind' where
  bind' (Parser p name) f = Parser (args -> fold (p args) Failure (runParser . f)) name

runParser : Parser a -> List String -> ParseResult a
runParser (Parser f _) = f

nameOf : Parser a -> String
nameOf (Parser _ name) = name

rename : Parser a -> String -> Parser a
rename (Parser f _) name = Parser f name

success : a -> Parser a
success a = Parser (Success a) (toString a)

failure : String -> Parser a
failure msg = Parser (_ -> Failure msg) msg

data ParseResult a = Failure String | Success a (List String)

getOrElse : ParseResult a -> a -> a
getOrElse (Failure m)   a = a
getOrElse (Success a _) _ = a

fold : ParseResult a -> (String -> b) -> (a -> List String -> b) -> b
fold (Failure m)      failF _    = failF m
fold (Success a rest) _     sucF = sucF a rest

mapP = fmap parserFunctor
bindP = bind parserMonad
infixl 5 >>=
(>>=) = bindP

infixl 5 ^^
(^^) p f = mapP f p

infixl 3 ^^^
(^^^) p b = mapP (_ -> b) p

filterP f p = filterWithP f (_ -> "invalid " ++_String (nameOf p)) p

filterWithP f errorF p = bindP p (a -> (if (f p) (success a) (failure (errorF a))))

infixl 5 :&
data And a b = (:&) a b
--override def toString = s"($a ~ $b)"

andToList : And a (List a) -> List a
andToList (a :& as) = a :: as

infixl 5 &
(&) : Parser a -> Parser b -> Parser (And a b)
(&) p1 p2 = rename (p1 >>= (a -> mapP (b -> a :& b) p2)) ((nameOf p1) ++_String " " ++_String (nameOf p2))

infixl 5 |
(|) = or
-- TODO: Ermine seems to have a naming issue. i wanted to name x' f',
-- TODO: but it conflicted with another f' below :(
or : Parser a -> Parser b -> Parser (Either a b)
or (Parser f1 name1) (Parser f2 name2) = Parser x' n' where
  x' args = case (f1 args) of
    (Success a rest) -> Success (Left a) rest
    (Failure m1)     -> case (f2 args) of
                          (Success b rest) -> Success (Right b) rest
                          (Failure m2)     -> Failure (m1 ++_String " or " ++_String m2)
  n' = name1 ++_String " or " ++_String name2

zeroOrMore : Parser a -> Parser (List a)
zeroOrMore p =  rename ((oneOrMore p | success []) ^^ f') (nameOf p ++_String "*") where
  f' : Either (List a) (List a) -> List a
  f' (Left  as) = as
  f' (Right as) = as

oneOrMore  : Parser a -> Parser (List a)
oneOrMore p = rename ((p & (zeroOrMore p)) ^^ andToList) (nameOf p ++_String "+")

opt : Parser a -> Parser (Maybe a)
opt (Parser f n) = Parser f' n' where
  f' args = fold (f args) (_ -> Success Nothing args) (t rest -> Success (Just t) rest)
  n' = "optional(" ++_String n ++_String ")"

infixl 5 ~>
(~>) : Parser a -> Parser b -> Parser b
(~>) p1 p2 = bindP p1 (a -> p2)

infixl 5 <~
(<~) : Parser a -> Parser b -> Parser a
(<~) p1 p2 = bindP p1 (a -> p2 ^^^ a)

repSep : Parser a -> Parser b -> Parser (List a)
repSep p1 p2 = p1 & oneOrMore (p2 ~> p1) ^^ andToList

eof : Parser ()
eof = Parser empty' "EOF" where
  empty' : List String -> ParseResult ()
  empty' [] = Success () []
  empty' ss = Failure $ concat_String ("unprocessed input" :: ss)

noArguments = eof
nothing     = eof
empty       = eof

anyStringAs : String -> Parser String
anyStringAs name = Parser f' name where
  f' : List String -> ParseResult String
  f' [] = Failure $ concat_String ["expected ", name, " but got nothing"]
  f' (x::xs) = Success x xs

anyString: Parser String
anyString = anyStringAs "string"

slurp: Parser String
slurp = rename (zeroOrMore anyString ^^ unwords_String) "slurp"

remainingArgs: Parser (List String)
remainingArgs = Parser (flip Success []) "remainingArgs"

{--
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
--}