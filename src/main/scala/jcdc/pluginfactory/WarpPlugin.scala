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

  def warpsFor(p:Player) = db.query(classOf[Warp]).where.ieq("player", p.getName).findList()
  // filtering here instead of in sql because the number of warps should be small. nbd.
  def getWarp[T](warpName:String, p:Player)(t: => T, w: Warp => T) =
    foldOption(warpsFor(p).filter(_.name == warpName).headOption)(t, w)

  val setWarpCommand = oneArg((p, c) => {
    val warpName = c.args.head
    getWarp(warpName, p)(
      p.messageAfter("created warp: " + warpName) { db.insert(createWarp(warpName, p)) },
      w => p.messageAfter("overwrote warp: " + warpName) { db.delete(w); db.insert(createWarp(warpName, p)) }
    )
  })

  val warpCommand = oneArg((p, c) => getWarp(c.args.head, p)(
    p.sendError("no such warp: " + c.args.head), w => p.teleport(w.location(p.getWorld))))

  val listWarpsCommand = command((p,c) => warpsFor(p).foreach{w => p.sendMessage(w.toString) })

  val deleteAllWarpsCommand = command((p, c) =>
    db.foreach(classOf[Warp]){w => logInfo("deleting: " + w); db.delete(w)})

  val deleteWarpCommand = oneArg((p,c) => getWarp(c.args.head, p)(
    p.sendError("no such warp: " + c.args.head),
    w => p.messageAfter("deleted warp: " + c.args.head) { db.delete(w) }
  ))
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
  override def toString = player + "." + name + (x, y, z)
}
