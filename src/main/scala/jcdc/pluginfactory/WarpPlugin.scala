package jcdc.pluginfactory

import org.bukkit.entity.Player
import org.bukkit.command.Command
import scala.collection.JavaConversions._
import org.bukkit.{Location, World}
import ScalaPlugin._

class WarpPlugin extends ManyCommandsPlugin {

  val commands = Map(
    "warp" -> warpCommand,
    "warps" -> listWarpsCommand,
    "set-warp" -> setWarpCommand,
    "delete-all-warps" -> opOnly(deleteAllWarpsCommand),
    "delete-warp" -> deleteWarpCommand)

  override def dbClasses = List(classOf[Warp])

  def createWarp(n:String, p:Player): Warp = {
    val w = new Warp(); w.name = n; w.player = p.getName; w.x = p.x; w.y = p.y; w.z = p.z; w
  }

  def warpsFor(p:Player) = dbQuery(classOf[Warp]).where.ieq("player", p.getName).findList().toList
  // filtering here instead of in sql because the number of warps should be small. nbd.
  def getWarp(warpName:String, p:Player) = warpsFor(p).filter(_.name == warpName).headOption

  val setWarpCommand = oneArg((p: Player, c: Command, args: Array[String]) => {
    val warpName = args(0)
    getWarp(warpName, p) match {
      case None => p.messageAfter("created warp: " + warpName) { dbInsert(createWarp(warpName, p)) }
      case Some(w) => p.messageAfter("overwrote warp: " + warpName) { dbDelete(w); dbInsert(createWarp(warpName, p)) }
    }
  })

  val warpCommand = oneArg((p:Player, c:Command, args:Array[String]) => getWarp(args(0), p) match {
    case Some(w) => p.teleport(w.location(p.getWorld))
    case _ => p.sendError("no such warp: " + args(0))
  })

  val listWarpsCommand = (p:Player, c:Command, args:Array[String]) =>
    warpsFor(p).map(_.toString).foreach{w => logInfo(w); p.sendMessage(w) }

  val deleteAllWarpsCommand = (p:Player, c:Command, args:Array[String]) =>
    findAll(classOf[Warp]).foreach{ w => logInfo("deleting: " + w); dbDelete(w) }

  val deleteWarpCommand = oneArg((p:Player, c:Command, args:Array[String]) => getWarp(args(0), p) match {
    case Some(w) => p.messageAfter("deleted warp: " + args(0)) { dbDelete(w) }
    case _ => p.sendError("no such warp: " + args(0))
  })
}

@javax.persistence.Entity
class Warp {
  @javax.persistence.Id
  @javax.persistence.GeneratedValue
  var id = 0
  var name: String = ""
  var player = ""
  var x = 0d; var y = 0d; var z = 0d
  def location(world:World) = new Location(world, x, y, z)
  override def toString = player + "." + name + (List(x, y, z).mkString("(", ",", ")"))
}
