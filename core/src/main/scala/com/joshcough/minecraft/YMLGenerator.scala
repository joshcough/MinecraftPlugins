package com.joshcough.minecraft

import ParserCombinators._

trait PluginConfig[T] {
  val pluginClass: Class[T]
  val commands: List[CommandsPlugin.Command] = Nil
}

// A helper object I use to auto generate all my plugin.yml files.
object YMLGenerator {

  val argsParser =
    anyString.named("className") ~
    anyString.named("author")    ~
    anyString.named("version")   ~
    anyString.named("outputDir") ~
    anyString.named("configClassName") ~
    eof

  def main(args: Array[String]): Unit = argsParser(args.toList).extract {
    case pluginName ~ author ~ version ~ outputDir ~ configClassName ~ _ =>
      writeYMLFromConfig(pluginName, author, version, outputDir, configClassName)
  }

  def writeYMLFromConfig(pluginName: String,
                         author: String,
                         version: String,
                         outputDir: String = ".",
                         configClassName: String): Unit = {
    val config = Class.forName(configClassName).newInstance().asInstanceOf[PluginConfig[_]]
    writeYML(pluginName, config.pluginClass.getName, author, version, outputDir, config.commands)
  }

  def writeYML(pluginName: String,
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
      Nil,
      // TODO: these should not be hardcoded...should come from the config
      List("ScalaLibPlugin", "ScalaPluginAPI"), Nil,
      commands)
    println(yml)
    write(yml, "plugin.yml")
  }
}
