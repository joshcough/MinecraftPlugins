package jcdc.pluginfactory

import org.bukkit.entity.Player
import org.bukkit.command.Command
import scala.collection.JavaConversions._
import org.bukkit.{Location, World}
import ScalaPluginPredef._

class WarpPlugin extends ManyCommandsPlugin {

  val commands = Map(
    "warp" -> warpCommand,
    "warps" -> new ListWarpsCommand,
    "set-warp" -> setWarpCommand,
    "delete-all-warps" -> new DeleteAllWarpsCommand with OpOnly,
    "delete-warp" -> deleteWarpCommand)

  override def dbClasses = List(classOf[Warp])

  def createWarp(n:String, p:Player): Warp = {
    val w = new Warp()
    w.name = n; w.player = p.getName; w.x = p.x; w.y = p.y; w.z = p.z
    w
  }

  def warpsFor(p:Player) = dbQuery(classOf[Warp]).where.ieq("player", p.getName).findList().toList
  // filtering here instead of in sql because the number of warps should be small. nbd.
  def getWarp(warpName:String, p:Player) = warpsFor(p).filter(_.name == warpName).headOption

  def setWarpCommand = new SetWarpCommand with OneArg
  class SetWarpCommand extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]){
      val warpName = args(0)
      getWarp(warpName, player) match {
        case None => player.messageAround("creating warp: " + warpName, "created warp: " + warpName){
          dbInsert(createWarp(warpName, player))
        }
        case Some(w) => player.messageAround("overwriting warp: " + warpName, "overwrote warp: " + warpName){
          dbDelete(w)
          dbInsert(createWarp(warpName, player))
        }
      }
    }
  }

  def warpCommand = new WarpCommand with OneArg
  class WarpCommand extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]){
      getWarp(args(0), player) match {
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

  def deleteWarpCommand = new DeleteWarpCommand with OneArg
  class DeleteWarpCommand extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]){
      getWarp(args(0), player) match {
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
  override def toString = player + "." + name + (List(x, y, z).mkString("(", ",", ")"))
}
