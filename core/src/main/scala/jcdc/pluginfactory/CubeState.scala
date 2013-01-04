package jcdc.pluginfactory

import BukkitEnrichment._
import org.bukkit.Location
import org.bukkit.entity.Player

trait CubeState {

  val corners = new PlayerState[List[Location]] {
    override val default = Some(Nil)
  }

  def cube(p:Player): MineCraftCube = corners(p) match {
    case List(c1, c2) => MineCraftCube(c1, c2)
    case _ => p bomb "Both corners must be set!"
  }

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
    corners.state.filter(kv => kv._2.size == 2).mapValues {
      case List(l1, l2) => MineCraftCube(l1, l2)
    }
}