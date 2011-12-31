package jcdc.pluginfactory

import org.bukkit.command.Command
import org.bukkit.{GameMode, ChatColor}
import scala.collection.JavaConversions._
import org.bukkit.entity._

class MultiPlayerCommands extends ManyCommandsPlugin {

  class GM extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) =
      if(args.length == 0 || ! List("c", "s").contains(args(0))) player.sendMessage(ChatColor.RED + "/gm c or s");
      else player.setGameMode(if(args(0) == "c") GameMode.CREATIVE else GameMode.SURVIVAL)
  }

  class KillHandler extends CommandHandler {
    def handle(killer: Player, cmd: Command, args: Array[String]){
      val world = killer.getWorld
      val entities = world.getEntities
      def usage(){ sendUsage(killer, cmd) }
      def killPlayer(name:String){
        Option(getServer.getPlayer(args(0))) match {
          case Some(p) =>
            p.setHealth(0)
            p.sendMessage(ChatColor.RED + "you have been killed by: " + killer.getName)
          case None => killer.sendMessage(ChatColor.RED + "kill could not find player: " + args(0))
        }
      }
      
      def remove(e:Entity) = e.remove()
      def removeAll(es:Seq[Entity]) = es.foreach(remove)

      if(args.length > 0) args(0).toLowerCase match {
        case "player" => if(args.length == 2) killPlayer(args(1)) else usage()
        case "items" => removeAll(entities.collect{ case i: Item => i })
        case "chickens" => removeAll(entities.collect{ case i: Chicken => i })
        case _ => usage()
      }
      else usage()
    }
  }

  class ShowEntities extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]){
      player.getWorld.getEntities.foreach(println)
      player.getWorld.getEntities.foreach(e => player.sendMessage(e.toString))
    }
  }

  class FeedHandler extends CommandHandler {
    def handle(feeder: Player, cmd: Command, args: Array[String]) = {
      if(feeder.isOp && args.length == 1) {
        Option(getServer.getPlayer(args(0))) match {
          case Some(p) =>
            p.setFoodLevel(20)
            p.sendMessage(ChatColor.GREEN + "you have been fed by " + feeder.getName)
            feeder.sendMessage(ChatColor.GREEN + "you have fed" + feeder.getName)
          case None => feeder.sendMessage(ChatColor.RED + "feed could not find player: " + args(0))
        }
      }
      else feeder.sendMessage(ChatColor.RED + "usage: /feed player-name")
    }
  }

  class StarveHandler extends CommandHandler {
    def handle(feeder: Player, cmd: Command, args: Array[String]){
      if(feeder.isOp && args.length == 1) {
        Option(getServer.getPlayer(args(0))) match {
          case Some(p) =>
            p.setFoodLevel(0)
            p.sendMessage(ChatColor.GREEN + "you have been starved by " + feeder.getName)
            feeder.sendMessage(ChatColor.GREEN + "you have starved " + feeder.getName)
          case None => feeder.sendMessage(ChatColor.RED + "starve could not find player: " + args(0))
        }
      }
      else feeder.sendMessage(ChatColor.RED + "usage: /starve player-name")
    }
  }

  class ChangeTime extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]){
      if(args.length != 1) player.sendMessage(ChatColor.RED + "/changetime h")
      else player.getWorld.setTime(args(0).toInt)
    }
  }

  class DayMaker extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) = player.getWorld.setTime(1)
  }

  class NightMaker extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) = player.getWorld.setTime(15000)
  }

  class Spawner extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) = {
      if(args.length < 1) player.sendMessage(ChatColor.RED + "/spawn name [#]")
      else {
        CreatureType.values.find(_.toString == args(0).toUpperCase) match {
          case Some(c) =>
            // TODO: its probably a really good idea to put some limit on N here.
            for(i <- 1 to (if(args.length == 2) args(1).toInt else 1)){
              player.getWorld.spawnCreature(player.getLocation, c)
            }
          case _ => player.sendMessage(ChatColor.RED + "no such creature: " + args(0))
        }
      }
    }
  }

  val commands = Map(
    "gm" -> new GM,
    "kill" -> new KillHandler with OpOnly,
    "change-time" -> new ChangeTime,
    "day" -> new DayMaker,
    "night" -> new NightMaker,
    "spawn" -> new Spawner,
    "entities" -> new ShowEntities,
    "feed" -> new FeedHandler with OpOnly,
    "starve" -> new StarveHandler with OpOnly)
}


//  val xpAdd = ("xp-add", new CommandHandler {
//    def handle(player: Player, cmd: Command, args: Array[String]) = {
//      if(args.length != 1) player.sendMessage(ChatColor.RED + "/xp-add n")
//      else {
//        try{
//          player.sendMessage(ChatColor.GREEN + "current xp: " + player.getExp)
//          player.setExp(player.getExp + args(0).toFloat)
//          player.sendMessage(ChatColor.GREEN + "xp set to: " + player.getExp)
//        }catch{
//          case e => log.log(java.util.logging.Level.SEVERE, "whoa", e)
//        }
//      }
//    }
//  })
