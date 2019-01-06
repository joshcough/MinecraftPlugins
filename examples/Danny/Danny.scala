package com.joshcough.minecraft.examples

import com.joshcough.minecraft._
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.ChatColor._
import CommandsPlugin._
import org.bukkit.Particle

class DannyPlugin extends ListenersPlugin with CommandsPlugin {
  def msg(name: String) = GOLD.apply(getConfig().getString(name))
  val commands = DannyCommands.commands(getConfig)
  val listeners = List(
    OnPlayerJoin ((p, e) => p ! msg("message")),
    OnPlayerBedEnterEvent ((p, e) => p ! msg("sleepmessage")),
    OnVehicleEnterEvent ((p, e) => p ! msg("ridemessage")),
  )
}

object DannyCommands {
  def main(args: Array[String]): Unit = YMLGenerator.writeYML(args, commands(null))
  def commands(implicit config: FileConfiguration): List[Command] = {
    List(
      Command("hello", "hello")(p => p ! GOLD(config.getString("message"))),
      Command("particleattack", "particleattack"){ case p =>
          p ! GOLD("PARTICLE ATTACK")
          p.spawnParticle(Particle.VILLAGER_HAPPY, p.getLocation().add(1,2,0), 50)
      },
    )
  }
}
