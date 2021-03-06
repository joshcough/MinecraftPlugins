//package com.joshcough.minecraft.examples
//
//import com.joshcough.minecraft.DBPluginWithCommands
//import org.bukkit.{Location, World}
//import org.bukkit.entity.Player
////import org.squeryl.Schema
////import org.squeryl.PrimitiveTypeMode._
//
///**
// * Classic Warp Plugin example, done in Scala.
// *
// * Allows players to create warp locations that they can
// * later warp to. This is good for getting around quickly,
// * and even better for not worrying about getting lost.
// *
// * This plugin also serves as a useful example of how to use the database.
// * The Warp class is an 'Entity' that gets saved in the database.
// * Any data saved in the database persists across user logouts, and server restarts.
// */
//class WarpPlugin extends DBPluginWithCommands {
//
//  lazy val db = new Schema {
//    private val warps = table[Warp]
//    // insert a warp
//    def insert(w: Warp) = warps.insert(w)
//    // find all the warps
//    def allWarps = from(warps)(select(_)).toList
//    // delete a warp
//    def delete(w: Warp) = warps.deleteWhere(_.id === w.id)
//    // find all the warps for the given player
//    def warpsFor(p: Player): List[Warp] =
//      from(warps)(w => where(w.player === p.name) select(w)).toList
//    // Tries to find a warp for the given player, and then runs a function on it
//    // (such as warping to it, or deleting it)
//    def withWarp(p: Player, warpName: String)(f: Warp => Unit) =
//      from(warps)(w => where(w.player === p.name and w.name === warpName) select(w)).
//        headOption.fold(p ! s"No such warp $warpName")(f)
//  }
//
//  def warpToken = anyStringAs("warp-name")
//
//  val commands = List(
//    DBCommand("warps", "List all warps.")(p => db.warpsFor(p).foreach(w => p ! w.toString)),
//    DBCommand("warp",  "Warp to the given warp location.", warpToken){ case (p, wt) =>
//      db.withWarp(p, wt)(w => p teleport w.location(p.world))
//    },
//    DBCommand("set-warp", "Create a new warp location.", warpToken){ case (p, warpName) =>
//      db.insert(Warp(0, warpName, p.name, p.x, p.y, p.z))
//      p ! s"created warp: $warpName"
//    },
//    DBCommand("delete-warp", "Delete a warp location.", warpToken){ case (p, wt) =>
//      db.withWarp(p, wt){w => db.delete(w); p ! s"deleted warp: ${w.name}" }
//    },
//    DBCommand("delete-all", "Delete all your warps.")(p =>
//      db.warpsFor(p).foreach{ w => p ! s"deleting: $w"; db.delete(w); }
//    ),
//    OpOnly(DBCommand("purge-warps-database", "Delete all warps in the database.")(p =>
//      db.allWarps.foreach { w => p ! s"deleting: $w"; db.delete(w) })
//    )
//  )
//}
//
//case class Warp(id: Long, name: String, player: String, x: Double, y: Double, z: Double) {
//  def this() = this(0,"","",0,0,0)
//  def location(world: World) = new Location(world, x, y, z)
//  override def toString = player + "." + name + (x, y, z)
//}
