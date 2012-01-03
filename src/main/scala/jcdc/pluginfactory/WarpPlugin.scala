package jcdc.pluginfactory

import org.bukkit.entity.Player
import org.bukkit.command.Command
import scala.collection.JavaConversions._
import org.bukkit.{Location, World}

class WarpPlugin extends ManyCommandsPlugin {

  val commands = Map(
    "warp" -> new WarpCommand,
    "warps" -> new ListWarpsCommand,
    "set-warp" -> new SetWarpCommand,
    "delete-all-warps" -> new DeleteAllWarpsCommand with OpOnly,
    "delete-warp" -> new DeleteWarpCommand)

  override def dbClasses = List(classOf[Warp])

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
