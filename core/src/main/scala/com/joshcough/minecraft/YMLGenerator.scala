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

  //  def create[T](className: String) = Class.forName(className).newInstance.asInstanceOf[T]
  //  def generateYml(className: String, author: String, version: String, outputDir: String) =
  //    create[ScalaPlugin](className).writeYML(author, version, outputDir)
  //
  //  def main(args: Array[String]): Unit = {
  //    argsParser(args.toList).extract {
  //      case className ~ author ~ version ~ outputDir ~ _ => generateYml(className, author, version, outputDir)
  //    }
  //  }


  def writeYML( pluginName: String,
                pluginClassName: String,
                author: String,
                version: String,
                outputDir: String = ".",
                commands: List[CommandsPlugin.Command]): Unit = {
    val resources = new java.io.File(outputDir)
    resources.mkdirs
    println(resources.getAbsolutePath)
    def write(contents: String, filename:String): Unit = {
      val f = new java.io.FileWriter(new java.io.File(resources, filename))
      f.write(contents)
      f.close()
    }
    val yml = CommandsPlugin.fullPluginYml(
        pluginName,
        pluginClassName,
        author,
        version,
        Nil, List("ScalaLibPlugin", "ScalaPluginAPI"), Nil,
        commands)
    println(yml)
    write(yml, "plugin.yml")
  }

  def writeYML(args: Array[String], commands: List[CommandsPlugin.Command]): Unit = args match {
    case Array(pluginName, pluginClassName, author, version, outputDir) =>
      writeYML(pluginName, pluginClassName, author, version, outputDir, commands)
  }

  def main(args: Array[String]): Unit = args match {
    case Array(pluginName, pluginClassName, author, version, outputDir) =>
      writeYML(pluginName, pluginClassName, author, version, outputDir, List())
  }
}
