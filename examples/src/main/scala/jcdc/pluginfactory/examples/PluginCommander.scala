package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{Command, CommandsPlugin}

class PluginCommander extends CommandsPlugin {
  val commands = List(
    Command("enable",  "Enable some plugins",  args(plugin.+){
      case (_, plugins) => plugins.foreach(pluginManager.enablePlugin)
    }),
    Command("disable", "Disable some plugins", args(plugin.+){
      case (_, plugins) => plugins.foreach(pluginManager.disablePlugin)
    })
  )
}