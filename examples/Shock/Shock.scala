package com.joshcough.minecraft.examples

import com.joshcough.minecraft._
import CommandsPlugin._
import org.bukkit.Server

class ShockPlugin extends CommandPlugin {
  val command = ShockCommands.command(this.server)
}

object ShockCommands {
  def main(args: Array[String]): Unit =
    YMLGenerator.writeYML(args, classOf[ShockPlugin].toString, "Josh Cough", List(command(null)))

  def command(implicit server: Server) =
    Command("shock", "shock a player", player){ case (_, them) => them.shock }
}
