package jcdc.pluginfactory

import org.bukkit.command.Command
import scala.collection.JavaConversions._
import org.bukkit.entity._
import org.bukkit.{Location, World, GameMode, ChatColor}
import javax.persistence.PersistenceException

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

  object Warps {
    def create(n:String, p:Player): Warp = {
      val w = new Warp()
      w.name = n
      w.player = p.getName
      w.x = p.getLocation.getX
      w.y = p.getLocation.getY
      w.z = p.getLocation.getZ
      w
    }

    def insert(warp:Warp) =
      try getDatabase.save(warp)
      catch { case e => logError(e) }
    def getWarp(warpName:String, playerName:String) =
      getDatabase.find(classOf[Warp]).
        where.ieq("name", warpName).
        where.ieq("player", playerName).findList.get(0)

    class SetWarpCommand extends CommandHandler {
      def handle(player: Player, cmd: Command, args: Array[String]){ insert(Warps.create(args(0), player)) }
    }

    class WarpCommand extends CommandHandler {
      def handle(player: Player, cmd: Command, args: Array[String]){
        player.teleport(getWarp(args(0), player.getName).location(player.getWorld))
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
    "set-warp" -> new Warps.SetWarpCommand)

  // TODO: database crap. this should/will be put somewhere nicer and/or redone soon.
  override def getDatabaseClasses = new java.util.ArrayList[Class[_]](){
    add(classOf[Warp])
  }

  override def onEnable(){
    super.onEnable()
    try getDatabase().find(classOf[Warp]).findRowCount()
    catch{
      case e: PersistenceException =>
        println("Installing database for " + name + " due to first time usage");
        installDDL()
    }
  }

}

import javax.persistence._
@javax.persistence.Entity
class Warp {
  @Id @GeneratedValue var id = 0
  var name: String = ""
  var player = ""
  var x = 0d
  var y = 0d
  var z = 0d
  def location(world:World) = new Location(world, x, y, z)
}

/**
 * @(Id @field) @(GeneratedValue @field) id: Int,
  @(Length @field)(max=30) @(NotEmpty @field) name:String,
  @(NotEmpty @field) player:String,
  @field x:Double, @field y:Double, @field z:Double){
  def this() = this(0, "",  "", 0, 0, 0)
  def this(name:String, player:Player) =
    this(0, name, player.getName, player.getLocation.getX, player.getLocation.getY, player.getLocation.getZ)
 */

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
