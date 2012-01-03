package jcdc.pluginfactory

import org.bukkit.command.Command
import scala.collection.JavaConversions._
import org.bukkit.entity._
import org.bukkit.{GameMode, ChatColor}
import ChatColor._
import ScalaPluginPredef._

class MultiPlayerCommands extends ManyCommandsPlugin {

  val commands = Map(
    "goto" -> new Goto with OneArg,
    "gm" -> new GameModeChanger with OneArg,
    "kill" -> new KillHandler with OpOnly with OneArg,
    "set-time" -> new ChangeTime with OneArg,
    "day" -> new DayMaker,
    "night" -> new NightMaker,
    "spawn" -> new Spawner with OneArg,
    "entities" -> new ShowEntities,
    "feed" -> new FeedHandler with OpOnly with OneArg,
    "starve" -> new StarveHandler with OpOnly with OneArg)

  class GameModeChanger extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) =
      if(List("c", "s").contains(args(0))) player.sendUsage(cmd)
      else player.setGameMode(if(args(0) == "c") GameMode.CREATIVE else GameMode.SURVIVAL)
  }

  class KillHandler extends CommandHandler {
    def handle(killer: Player, cmd: Command, args: Array[String]){
      val world = killer.getWorld
      val entities = world.getEntities
      def usage(){ killer.sendUsage(cmd) }
      def killPlayer(name:String) = killer.findPlayer(args(0)){ p =>
        p.messageAfter(RED + "you have been killed by: " + killer.getName){ p.setHealth(0) }
      }
      def removeAll(es:Seq[Entity]) { es.foreach(_.remove()) }
      args(0).toLowerCase match {
        case "player" => if(args.length == 2) killPlayer(args(1)) else usage()
        case "items" => removeAll(entities.collect{ case i: Item => i })
        case "chickens" => removeAll(entities.collect{ case i: Chicken => i })
        case _ => usage()
      }
    }
  }

  class ShowEntities extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]){
      player.getWorld.getEntities.foreach(e => player.sendMessage(e.toString))
    }
  }

  class Goto extends PlayerToPlayerCommand {
    def handle(sender: Player, receiver:Player, cmd: Command, args: Array[String]) { sender.teleport(receiver) }
  }

  class FeedHandler extends PlayerToPlayerCommand {
    def handle(feeder: Player, receiver:Player, cmd: Command, args: Array[String]) {
      receiver.messageAfter(GREEN + "you have been fed by " + feeder.getName){ receiver.setFoodLevel(20) }
      feeder.sendMessage(GREEN + "you have fed" + feeder.getName)
    }
  }

  class StarveHandler extends PlayerToPlayerCommand {
    def handle(feeder: Player, receiver:Player, cmd: Command, args: Array[String]) {
      receiver.messageAfter(GREEN + "you have been starved by " + feeder.getName){ receiver.setFoodLevel(0) }
      feeder.sendMessage(GREEN + "you have starved " + feeder.getName)
    }
  }

  class ChangeTime extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]){ player.getWorld.setTime(args(0).toInt) }
  }

  class DayMaker extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) = player.getWorld.setTime(1)
  }

  class NightMaker extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) = player.getWorld.setTime(15000)
  }

  class Spawner extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) {
      CreatureType.values.find(_.toString == args(0).toUpperCase) match {
        case Some(c) =>
          // TODO: its probably a really good idea to put some limit on N here.
          for(i <- 1 to (if(args.length == 2) args(1).toInt else 1)){
            player.getWorld.spawnCreature(player.getLocation, c)
          }
        case _ => player.sendError("no such creature: " + args(0))
      }
    }
  }
}