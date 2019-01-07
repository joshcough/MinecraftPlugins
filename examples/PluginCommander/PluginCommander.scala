package com.joshcough.minecraft.examples

import com.joshcough.minecraft._
import CommandsPlugin._
import org.bukkit.plugin.{PluginManager}

class PluginCommanderPlugin extends CommandsPlugin {
  val commands = PluginCommanderCommands.commands(this.pluginManager)
}

object PluginCommanderCommands {
  def main(args: Array[String]): Unit =
    YMLGenerator.writeYML(args, classOf[PluginCommanderPlugin].toString, "Josh Cough", commands(null))

  def commands(implicit pluginManager: PluginManager) = List(
    Command("enable",  "Enable some plugins",  plugin.+){
      case (_, plugins) => plugins foreach pluginManager.enablePlugin
    },
    Command("disable", "Disable some plugins", plugin.+){
      case (_, plugins) => plugins foreach pluginManager.disablePlugin
    }
  )
}
