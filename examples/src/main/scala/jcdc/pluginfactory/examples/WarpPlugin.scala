package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{CommandsPlugin, SingleClassDBPlugin}
import org.bukkit.{Location, World}
import org.bukkit.entity.Player
import scala.collection.JavaConversions._

class WarpPlugin extends CommandsPlugin with SingleClassDBPlugin[Warp]{

  val dbClass = classOf[Warp]

  val commands = Map(
    "warps"            -> warpsCommand,
    "warp"             -> warpCommand,
    "delete-warp"      -> deleteWarpCommand,
    "set-warp"         -> setWarpCommand,
    "delete-all-warps" -> opOnly(deleteAllCommand)
  )

  val warpsCommand = command((p, c) => warpsFor(p).foreach { w => p.sendMessage(w.toString) })
  val warpCommand  = oneArg ((p, c) => getWarp(c.args.head, p)(
    p.sendError("no such warp: " + c.args.head), w => p.teleport(w.location(p.getWorld)))
  )
  val deleteWarpCommand = oneArg((p, c)  => getWarp(c.args.head, p)(
    p.sendError("no such warp: " + c.args.head),
    w => { db.delete(w); p ! ("deleted warp: " + c.args.head)}
  ))
  val deleteAllCommand  = command((p, _) => db.foreach { w =>
    logInfo("deleting: " + w); db.delete(w)
  })
  val setWarpCommand = oneArg((p, c)  => {
    val warpName = c.args.head
    getWarp(warpName, p)(
    { db.insert(createWarp(warpName, p)); p ! "created warp: " + warpName },
    w => {
      // TODO: can i use an update here?
      db.delete(w)
      db.insert(createWarp(warpName, p))
      p ! ("overwrote warp: " + warpName)
    }
  )})

  def createWarp(n: String, p: Player): Warp = {
    val w = new Warp(); w.name = n; w.player = p.name; w.x = p.x; w.y = p.y; w.z = p.z; w
  }

  def warpsFor(p: Player) = db.query.where.ieq("player", p.getName).findList()

  // filtering here instead of in sql because the number of warps should be small. nbd.
  def getWarp[T](warpName: String, p: Player)(t: => T, w: Warp => T) =
    foldOption(warpsFor(p).filter(_.name == warpName).headOption)(t, w)
}

@javax.persistence.Entity
class Warp {
  @javax.persistence.Id
  @javax.persistence.GeneratedValue
  var id = 0
  var name: String = ""
  var player = ""
  var x = 0d;
  var y = 0d;
  var z = 0d
  def location(world: World) = new Location(world, x, y, z)
  override def toString = player + "." + name +(x, y, z)
}
