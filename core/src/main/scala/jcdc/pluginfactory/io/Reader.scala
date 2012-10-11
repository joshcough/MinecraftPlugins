package jcdc.pluginfactory.io

object Reader extends Reader

trait Reader {

  def read(s:String): Any = read(stripComments(s).toList)
  def stripComments(code:String) = code.split("\n").map(s => s.takeWhile(_!=';').trim).mkString(" ")
  def read(stream:List[Char]): Any = readWithRest(stream)._1
  def readWithRest(s:String): (Any, String) = {
    val rwr = readWithRest(stripComments(s).toList)
    (rwr._1, rwr._2.mkString.trim)
  }

  def readWithRest(stream:List[Char]): (Any, List[Char]) = {

    def readList(stream: List[Char], acc: List[Any], terminator:Char): (List[Any], List[Char]) = stream match {
      case ' ' :: tail => readList(tail, acc, terminator)
      case x   :: tail if x == terminator => (acc, tail)
      case x   :: tail =>
        val (next, rest) = readWithRest(stream)
        readList(rest, acc ::: List(next), terminator)
      case List()     => error("unclosed list")
    }

    def readSymbol(stream:List[Char]): (Symbol, List[Char]) = {
      val (chars, rest) = stream.span( ! List('(', ')', '[', ']', ' ', '\n').contains(_) )
      (Symbol(chars.mkString), rest)
    }

    def readNumOrMaybeSymbol(stream:List[Char], negate:Boolean): (Any, List[Char]) = {
      val (chars, rest) = stream.span( ! List('(', ')', '[', ']', ' ', '\n').contains(_) )
      // if there are any non number characters, this must be a symbol
      if(chars.exists(c => ! Character.isDigit(c))) (Symbol(chars.mkString), rest)
      else (((if(negate) "-" else "") + (chars.mkString)).toInt, rest)
    }

    def readStringLit(stream: List[Char], acc: String): (String, List[Char]) = stream match {
      case '"' :: tail => (acc, tail)
      case c   :: tail => readStringLit(tail, acc + c)
      case List()      => error("unclosed string literal")
    }

    def readCharLit(stream: List[Char]): (Char, List[Char]) = stream match {
      case c :: '\'' :: tail => (c, tail)
      case _  => error("unclosed character literal")
    }

    stream match {
      case '('  ::  tail => readList(stream=tail, acc=Nil, terminator=')')
      case '['  ::  tail => readList(stream=tail, acc=Nil, terminator=']')
      case ' '  ::  tail => readWithRest(tail)
      case '\n' ::  tail => readWithRest(tail)
      case '"'  ::  tail => readStringLit(tail, "")
      case '\'' ::  tail => readCharLit(tail)
      case ')'  ::  _    => error("unexpected list terminator")
      case ']'  ::  _    => error("unexpected list terminator")
      case c    ::  tail if(Character.isDigit(c)) => readNumOrMaybeSymbol(stream, negate=false)
      case '-'  :: c :: tail if(Character.isDigit(c)) => readNumOrMaybeSymbol(c :: tail, negate=true)
      case _ => readSymbol(stream)
    }
  }

  def printSExp(a:Any): String = a match {
    case s:Symbol => s.toString.drop(1)
    case s:String => s
    case i:Int => i.toString
    case l:List[Any] => l.map(printSExp).mkString("(", " ", ")")
  }

}
//  testRead("([xlt2 (< x 2)])", List(Symbol("["), 'xlt2, List('<, 'x, 2), Symbol("]")))
