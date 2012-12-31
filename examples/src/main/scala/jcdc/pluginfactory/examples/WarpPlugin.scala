package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{CommandsPlugin, SingleClassDBPlugin}
import org.bukkit.{Location, World}
import org.bukkit.entity.Player
import scala.collection.JavaConversions._

/**
 * Classic Warp Plugin example, done in Scala.
 *
 * Allows players to create warp locations that they can
 * later warp to. This is good for getting around quickly,
 * and even better for not worrying about getting lost.
 *
 * This plugin also serves as a useful example of how to use the database.
 * The Warp class is an 'Entity' that gets saved in the database.
 * Any data saved in the database persists across user logouts, and server restarts.
 */
class WarpPlugin extends CommandsPlugin with SingleClassDBPlugin[Warp] {

  val dbClass = classOf[Warp]

  def warpToken = anyStringAs("warp-name")

  val commands = List(

    Command("warps", "List all warps.")(p => warpsFor(p).foreach(w => p ! w.toString)),

    Command("warp",  "Warp to the given warp location.", warpToken){ case (p, wt) =>
      withWarp(p, wt)(w => p teleport w.location(p.world))
    },

    Command("delete-warp", "Delete a warp location.", warpToken){ case (p, wt) =>
      withWarp(p, wt){w => db.delete(w); p ! s"deleted warp: ${w.name}" }
    },

    Command("delete-all", "Delete all your warps.")(p =>
      warpsFor(p).foreach{ w => p ! s"deleting: $w"; db.delete(w); }
    ),

    Command("set-warp", "Create a new warp location.", warpToken){ case (p, warpName) =>
      println(s"creating warp: $warpName")
      // TODO: can i use an update here?
      db.firstWhere(Map("player" -> p.name, "name" -> warpName)).foreach{ w =>
      // warp already exists, so delete it.
        db.delete(w)
        p ! s"deleted warp: $warpName"
      }
      db.insert(createWarp(warpName, p))
      p ! s"created warp: $warpName"
    },

    OpOnly(Command("purge-warps-database", "Delete all warps in the database.")(p =>
      db.foreach { w => p ! s"deleting: $w"; db.delete(w) })
    )
  )

  def createWarp(n: String, p: Player): Warp = {
    val w = new Warp(); w.name = n; w.player = p.name; w.x = p.x; w.y = p.y; w.z = p.z; w
  }

  // go to the database to find all the warps for the given player
  def warpsFor(p: Player) = db.query.where.ieq("player", p.getName).findList()

  // tries to find a warp for the given player, and then runs a function on it
  // (such as warping to it, or deleting it)
  def withWarp(p: Player, w: String)(f: Warp => Unit) =
    warpsFor(p).find(_.name == w).fold(p ! s"No such warp $w")(f)
}

import javax.persistence._
import scala.beans.BeanProperty

// Note: Don't make this a case class, it had a lot of horrible issues.
// Note: Also don't make it an inner class of your plugin, because that
//       gives another set of horrible issues...
// TODO: I should really test this again...and try to make it easier.
@Entity
class Warp {
  @Id @GeneratedValue @BeanProperty var id = 0
  @BeanProperty var name: String = ""
  @BeanProperty var player = ""
  @BeanProperty var x = 0d
  @BeanProperty var y = 0d
  @BeanProperty var z = 0d
  def location(world: World) = new Location(world, x, y, z)
  override def toString = player + "." + name + (x, y, z)
}
