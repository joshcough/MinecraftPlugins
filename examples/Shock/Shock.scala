package com.joshcough.minecraft.examples

import com.joshcough.minecraft._
import CommandsPlugin._
import org.bukkit.Server

class ShockPlugin extends CommandPlugin {
  val command = ShockCommands.command(this.server)
}

class ShockConfig extends PluginConfig[ShockPlugin] {
  val pluginClass = classOf[ShockPlugin]
  override val commands = List(ShockCommands.command(null))
}

object ShockCommands {
  def command(implicit server: Server) =
    Command("shock", "shock a player", player){ case (_, them) => them.shock }
}
