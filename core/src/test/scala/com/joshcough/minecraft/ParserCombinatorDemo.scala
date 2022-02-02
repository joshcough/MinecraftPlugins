package com.joshcough.minecraft

object ParserCombinatorDemo {
  import com.joshcough.minecraft.ParserCombinators._
  def run[T](p: Parser[T], args:String): ParseResult[T] = p(args)
  // demo
  run(int, "5")
  run(int, "ewrer")
  run(int ~ int, "5 6")
  run(int ~ int, "5 qweqwe")
  run(int ~ anyString, "5 qweqwe")
  run(int ~ anyString, "5 qweqwe wfwfwef")
  run(bool or int , "true")
  run(bool or int , "7")
  run(bool or int , "qweqw")
  run("test", "test")
  run(int.*, "5 7 8 9")
  run(bool.+, "true")
  run(bool.+, "true false true")
  run(bool.+, "true false true qwewe")
  run(int ^^ (x => x * x), "7")
  run(int ~ "*" ~ int ^^^ 42, "6 * 9")
  run(int.? ~ "hi", "hi")
  run(int.? ~ "hi", "6 hi")
}
