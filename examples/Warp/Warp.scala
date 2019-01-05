package com.joshcough.minecraft.examples

import org.bukkit.{Location, World}
import org.bukkit.entity.Player
import com.joshcough.minecraft._
import java.io.File

import org.bukkit.configuration.file.YamlConfiguration

import scala.collection.JavaConverters._
import BukkitEnrichment._

/**
 * Classic Warp Plugin example, done in Scala.
 *
 * Allows players to create warp locations that they can
 * later warp to. This is good for getting around quickly,
 * and even better for not worrying about getting lost.
 *
 * This plugin also serves as a useful example of how to use the database.
 * The Warp class is an 'Entity' that gets saved in the database.
 * Any data saved in the database persists across user logouts, and server restarts.
 */
/*
Run these commands to regenerate the plugin.yml contents
import com.joshcough.minecraft.ScalaPlugin
import com.joshcough.minecraft.CommandsPlugin
println(CommandsPlugin.fullPluginYml("WarpPlugin", "com.joshcough.minecraft.examples.WarpPlugin", "Josh Cough", "0.1",
                              Nil, List("ScalaLibPlugin", "ScalaPluginAPI"), Nil,
                              com.joshcough.minecraft.examples.WarpCommands.commands(null)))
*/

class WarpPlugin extends CommandsPlugin  {
  val commands = WarpCommands.commands(getDataFolder())
  this.saveConfig()
}

object Warp {
  import com.joshcough.minecraft.CommandsPlugin._

  def warpToken: Parser[String] = anyStringAs("warp-name")

  def getConfig(player: Player)(implicit dataFolder: File): YamlConfiguration = {
    val configFile = new File(dataFolder, player.getUniqueId.toString + ".yml")
    YamlConfiguration.loadConfiguration(configFile)
  }

  def saveConfig(config: YamlConfiguration, player: Player)(implicit dataFolder: File) = {
    val configFile = new File(dataFolder, player.getUniqueId.toString + ".yml")
    config.save(configFile)
  }

  def withConfig[A](player: Player)(f : YamlConfiguration => A)(implicit dataFolder: File): A = {
    val config = getConfig(player)
    val a = f(config)
    saveConfig(config, player)
    a
  }

  def setWarp(w:Warp)(implicit dataFolder: File): Unit = Warp.withConfig(w.player){ config =>
    config.set(w.warpName, w.x + "," + w.y + "," + w.z)
    println("warps: " + getAllWarps(w.player))
  }

  def deleteWarp(player: Player, warpName: String)(implicit dataFolder: File): Unit = Warp.withConfig(player){ config =>
    config.set(warpName, null)
    println("warps: " + getAllWarps(player))
  }

  def getWarp(player: Player, warpName: String)(implicit dataFolder: File): Option[Warp] = Warp.withConfig(player){ config =>
    val locString = Option(config.getString(warpName))
    locString.map{xyz =>
      val Array(x,y,z) = xyz.split(",").map(_.toDouble)
      Warp(warpName, player, x, y, z)
    }
  }

  def getAllWarps(player: Player)(implicit dataFolder: File): List[String] = Warp.withConfig(player){ config =>
    config.getKeys(false).asScala.toList.sorted
  }
}

case class Warp(warpName: String, player: Player, x: Double, y: Double, z: Double) {
  def location(world: World) = new Location(world, x, y, z)
  def warp = player teleport location(player.world)
}

object WarpCommands {

  import CommandsPlugin._
  import Warp._

  def commands(implicit dataFolder: File): List[Command] = {
    List(
      Command("warps", "List all warps.")(p => getAllWarps(p).foreach(w => p ! w)),
      Command("warp",  "Warp to the given warp location.", warpToken){ case (p, wt) =>
        getWarp(p, wt).foreach{ _.warp }
      },
      Command("set-warp", "Create a new warp location.", warpToken){ case (p, warpName) =>
        deleteWarp(p, warpName)
        p ! s"deleted warp: $warpName"
      },
      Command("delete-warp", "Delete a warp.", warpToken){ case (p, warpName) =>
        setWarp(Warp(warpName, p, p.x, p.y, p.z))
        p ! s"created warp: $warpName"
      },
      Command("home", desc="Warp home.") { case p =>
        getWarp(p, "home").foreach{ _.warp }
      },
      Command("set-home", desc="Set your home location.") { case p =>
        setWarp(Warp("home", p, p.x, p.y, p.z))
      },
      Command("delete-home", "Delete your home location."){ case p =>
        deleteWarp(p, "home")
        p ! s"deleted warp: home"
      },
      Command("delete-all", "Delete all your warps.")(p =>
        getAllWarps(p).foreach(deleteWarp(p, _))
      ),
    )
  }
}


/*  def WarpCommand[T](name: String, desc: String, args: Parser[T])
                    (body: ((Player, T, YamlConfiguration)) => Unit)
                    (implicit dataFolder: File): Command = Command(name, desc, args){
    case (p, args) => withConfig(p)(body(p, args, _))
  }

  def WarpCommand(name: String, desc: String)
                    (body: ((Player, YamlConfiguration)) => Unit)
                    (implicit dataFolder: File): Command = Command(name, desc){
    case p => withConfig(p)(body(p, _))
  }*/