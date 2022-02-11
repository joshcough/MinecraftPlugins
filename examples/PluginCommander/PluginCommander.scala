package com.joshcough.minecraft.examples

import com.joshcough.minecraft._
import CommandsPlugin._
import org.bukkit.plugin.{PluginManager}

class PluginCommanderPlugin extends CommandsPlugin {
  val commands = PluginCommanderCommands.commands(this.pluginManager)
}

class PluginCommanderConfig extends PluginConfig[PluginCommanderPlugin] {
  val pluginClass = classOf[PluginCommanderPlugin]
  override val commands = PluginCommanderCommands.commands(null)
}

object PluginCommanderCommands {
  def commands(implicit pluginManager: PluginManager) = List(
    Command("enable",  "Enable some plugins",  plugin.+){
      case (_, plugins) => plugins foreach pluginManager.enablePlugin
    },
    Command("disable", "Disable some plugins", plugin.+){
      case (_, plugins) => plugins foreach pluginManager.disablePlugin
    }
  )
}
