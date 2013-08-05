package com.joshcough.minecraft.examples

import com.joshcough.minecraft.CommandsPlugin

class PluginCommander extends CommandsPlugin {
  val commands = List(
    Command("enable",  "Enable some plugins",  plugin.+){
      case (_, plugins) => plugins foreach pluginManager.enablePlugin
    },
    Command("disable", "Disable some plugins", plugin.+){
      case (_, plugins) => plugins foreach pluginManager.disablePlugin
    }
  )
}