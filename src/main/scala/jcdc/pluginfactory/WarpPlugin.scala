package jcdc.pluginfactory

import org.bukkit.entity.Player
import scala.collection.JavaConversions._
import org.bukkit.{Location, World}
import ScalaPlugin._

class WarpPlugin extends CommandsPlugin {

  val commands = Map(
    "warp"             -> warpCommand,
    "warps"            -> listWarpsCommand,
    "set-warp"         -> setWarpCommand,
    "delete-all-warps" -> opOnly(deleteAllWarpsCommand),
    "delete-warp"      -> deleteWarpCommand)

  override def dbClasses = List(classOf[Warp])

  def createWarp(n:String, p:Player): Warp = {
    val w = new Warp(); w.name = n; w.player = p.getName; w.x = p.x; w.y = p.y; w.z = p.z; w
  }

  def warpsFor(p:Player) = dbQuery(classOf[Warp]).where.ieq("player", p.getName).findList().toList
  // filtering here instead of in sql because the number of warps should be small. nbd.
  def getWarp(warpName:String, p:Player) = warpsFor(p).filter(_.name == warpName).headOption

  val setWarpCommand = oneArg((p, c) => {
    val warpName = c.args.head
    getWarp(warpName, p) match {
      case None => p.messageAfter("created warp: " + warpName) { dbInsert(createWarp(warpName, p)) }
      case Some(w) => p.messageAfter("overwrote warp: " + warpName) { dbDelete(w); dbInsert(createWarp(warpName, p)) }
    }
  })

  val warpCommand = oneArg((p, c) => getWarp(c.args.head, p) match {
    case Some(w) => p.teleport(w.location(p.getWorld))
    case _ => p.sendError("no such warp: " + c.args.head)
  })

  val listWarpsCommand = command((p,c) => warpsFor(p).foreach{w => p.sendMessage(w.toString) })

  val deleteAllWarpsCommand = command((p, c) =>
    findAll(classOf[Warp]).foreach{ w => logInfo("deleting: " + w); dbDelete(w) })

  val deleteWarpCommand = oneArg((p,c) => getWarp(c.args.head, p) match {
    case Some(w) => p.messageAfter("deleted warp: " + c.args.head) { dbDelete(w) }
    case _ => p.sendError("no such warp: " + c.args.head)
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
