package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{Command, CommandsPlugin, SingleClassDBPlugin}
import org.bukkit.{Location, World}
import org.bukkit.entity.Player
import scala.collection.JavaConversions._
import com.avaje.ebean.bean.EntityBean

class WarpPlugin extends CommandsPlugin with SingleClassDBPlugin[Warp] {

  val dbClass = classOf[Warp]

  val commands = List(

    Command("warps", "List all warps.", noArgs(p => warpsFor(p).foreach(w => p ! w.toString))),

    Command("warp", "Warp to the given warp location.",
      args(warpToken){ case (p, wt) => withWarp(p, wt)(w => p.teleport(w.location(p.world))) }),

    Command("delete-warp", "Delete a warp location.",
      args(warpToken){ case (p, wt) => withWarp(p, wt){w =>
        db.delete(w)
        p ! s"deleted warp: ${w.name}"
      }}),

    Command("delete-all", "Delete all your warps.",
      noArgs(p => warpsFor(p).foreach{ w => p ! s"deleting: $w"; db.delete(w); })),

    Command("set-warp", "Create a new warp location.",
      args(warpToken){ case (p, warpName)  =>
        println(s"creating warp: $warpName")
        // TODO: can i use an update here?
        // making Warp a case class gave lots of unsolved issues...
        warpsFor(p).find(_.name == warpName).foreach{ w =>
          // warp already exists, so delete it.
          db.delete(w)
          p ! s"deleted warp: $warpName"
        }
        db.insert(createWarp(warpName, p))
        p ! s"created warp: $warpName"
      }
    ),

    Command("purge-warps-database", "Delete all warps in the database.",
      opOnly(noArgs(p => db.foreach { w => p ! s"deleting: $w"; db.delete(w) })))
  )

  def warpToken = anyString.named("warp-name")

  def createWarp(n: String, p: Player): Warp = {
    val w = new Warp(); w.name = n; w.player = p.name; w.x = p.x; w.y = p.y; w.z = p.z; w
  }

  def warpsFor(p: Player) = db.query.where.ieq("player", p.getName).findList()

  def withWarp(p:Player, w:String)(f: Warp => Unit) =
    warpsFor(p).find(_.name == w).fold(p ! s"No such warp $w")(f)
}

import javax.persistence._
import scala.beans.BeanProperty

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
