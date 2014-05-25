package com.joshcough.minecraft

import ParserCombinators._

// A helper object I use to auto generate all my plugin.yml files.
object YMLGenerator {

  val argsParser =
    anyString.named("className") ~
    anyString.named("author")    ~
    anyString.named("version")   ~
    anyString.named("outputDir") ~
    eof

  def create[T](className: String) = Class.forName(className).newInstance.asInstanceOf[T]

  def generateYml(className: String, author: String, version: String, outputDir: String) =
    create[ScalaPlugin](className).writeYML(author, version, outputDir)

  def main(args: Array[String]): Unit = {
    argsParser(args.toList).extract {
      case className ~ author ~ version ~ outputDir ~ _ => generateYml(className, author, version, outputDir)
    }
  }

}
