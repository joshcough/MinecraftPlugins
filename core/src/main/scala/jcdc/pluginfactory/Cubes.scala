package jcdc.pluginfactory

import BukkitEnrichment._
import org.bukkit.Location
import org.bukkit.entity.Player

trait Cubes {
  val corners = collection.mutable.Map[Player, List[Location]]()

  def cube(p:Player): MineCraftCube = corners.get(p).filter(_.size == 2).
    flipFold(ls => MineCraftCube(ls(0), ls(1)))(p bomb "Both corners must be set!")

  def setFirstPosition(p:Player,loc: Location): Unit = {
    corners += (p -> List(loc))
    p ! s"first corner set to: ${loc.xyz}"
  }

  def setSecondPosition(p:Player,loc2: Location): Unit = corners(p) match {
    case loc1 :: _ =>
      corners += (p -> List(loc1, loc2))
      p ! s"second corner set to: ${loc2.xyz}"
    case Nil =>
      p ! "set corner one first! (with a left click)"
  }

  def cubes: collection.Map[Player, MineCraftCube] =
    corners.filter(kv => kv._2.size == 2).mapValues{
      case List(l1, l2) => MineCraftCube(l1, l2)
    }
}