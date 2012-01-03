package jcdc.pluginfactory

import org.bukkit.command.Command
import scala.collection.JavaConversions._
import org.bukkit.entity._
import org.bukkit.{Location, World, GameMode, ChatColor}

class MultiPlayerCommands extends ManyCommandsPlugin {

  class GM extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) =
      if(args.length == 0 || ! List("c", "s").contains(args(0))) player.sendError("/gm c or s");
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
            p.messageAfter(ChatColor.RED + "you have been killed by: " + killer.getName){ p.setHealth(0) }
          case None => killer.sendError("kill could not find player: " + args(0))
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
            p.messageAfter(ChatColor.GREEN + "you have been fed by " + feeder.getName){
              p.setFoodLevel(20)
            }
            feeder.sendMessage(ChatColor.GREEN + "you have fed" + feeder.getName)
          case None => feeder.sendError("feed could not find player: " + args(0))
        }
      }
      else feeder.sendError("usage: /feed player-name")
    }
  }

  class StarveHandler extends CommandHandler {
    def handle(feeder: Player, cmd: Command, args: Array[String]){
      if(feeder.isOp && args.length == 1) {
        Option(getServer.getPlayer(args(0))) match {
          case Some(p) =>
            p.messageAfter(ChatColor.GREEN + "you have been starved by " + feeder.getName){
              p.setFoodLevel(0)
            }
            feeder.sendMessage(ChatColor.GREEN + "you have starved " + feeder.getName)
          case None => feeder.sendError("starve could not find player: " + args(0))
        }
      }
      else feeder.sendError("usage: /starve player-name")
    }
  }

  class ChangeTime extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]){
      if(args.length != 1) player.sendError("/changetime h")
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
      if(args.length < 1) sendUsage(player, cmd)
      else {
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

  object Warps {
    def createWarp(n:String, p:Player): Warp = {
      val w = new Warp()
      w.name = n
      w.player = p.getName
      w.x = p.getLocation.getX
      w.y = p.getLocation.getY
      w.z = p.getLocation.getZ
      w
    }

    // filtering here instead of in sql because the number of warps should be small. nbd.
    def getWarp(warpName:String, p:Player) = warpsFor(p).filter(_.name == warpName).headOption
    def warpsFor(p:Player) = dbQuery(classOf[Warp]).where.ieq("player", p.getName).findList().toList

    class SetWarpCommand extends CommandHandler {
      def handle(player: Player, cmd: Command, args: Array[String]){
        if(args.length != 1) sendUsage(player, cmd)
        else logInfoAround("creating warp: " + args(0), "created warp: " + args(0)){
          getWarp(args(0), player) match {
            case None => player.messageAround("creating warp: " + args(0), "created warp: " + args(0)){
              dbInsert(createWarp(args(0), player))
            }
            case Some(w) => player.messageAround("overwriting warp: " + args(0), "overwrote warp: " + args(0)){
              dbDelete(w)
              dbInsert(createWarp(args(0), player))
            }
          }
        }
      }
    }

    class WarpCommand extends CommandHandler {
      def handle(player: Player, cmd: Command, args: Array[String]){
        if(args.length != 1) sendUsage(player, cmd)
        else getWarp(args(0), player) match {
          case Some(w) => player.teleport(w.location(player.getWorld))
          case _ => player.sendError("no such warp: " + args(0))
        }
      }
    }

    class ListWarpsCommand extends CommandHandler {
      def handle(player: Player, cmd: Command, args: Array[String]){
        warpsFor(player).map(_.toString).foreach{w => logInfo(w); player.sendMessage(w) }
      }
    }

    class DeleteAllWarpsCommand extends CommandHandler {
      def handle(player: Player, cmd: Command, args: Array[String]){
        findAll(classOf[Warp]).foreach{ w => logInfo("deleting: " + w); dbDelete(w) }
      }
    }

    class DeleteWarpCommand extends CommandHandler {
      def handle(player: Player, cmd: Command, args: Array[String]){
        if(args.length != 1) sendUsage(player, cmd)
        else getWarp(args(0), player) match {
          case Some(w) => player.messageAfter("deleted warp: " + args(0)){ dbDelete(w) }
          case _ => player.sendError("no such warp: " + args(0))
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
    "starve" -> new StarveHandler with OpOnly,
    "warp" -> new Warps.WarpCommand,
    "warps" -> new Warps.ListWarpsCommand,
    "set-warp" -> new Warps.SetWarpCommand,
    "delete-all-warps" -> new Warps.DeleteAllWarpsCommand with OpOnly,
    "delete-warp" -> new Warps.DeleteWarpCommand)

  override def dbClasses = List(classOf[Warp])
}

@javax.persistence.Entity
class Warp {
  @javax.persistence.Id
  @javax.persistence.GeneratedValue
  var id = 0
  var name: String = ""
  var player = ""
  var x = 0d
  var y = 0d
  var z = 0d
  def location(world:World) = new Location(world, x, y, z)
  override def toString = new PrintableWarp(id, name, player, x, y, z).toString
  case class PrintableWarp(id:Int, name:String, player:String, x:Double, y:Double, z:Double)
}
