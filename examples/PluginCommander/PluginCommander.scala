package jcdc.pluginfactory.examples

import jcdc.pluginfactory.CommandsPlugin

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