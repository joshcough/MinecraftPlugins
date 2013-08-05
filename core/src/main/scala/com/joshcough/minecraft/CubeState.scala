package com.joshcough.minecraft

import BukkitEnrichment._
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player

trait CubeState {

  val corners = new PlayerState[List[Location]] {
    override val default = Some(Nil)
  }

  def cube(p:Player): Cube[Block] =
    cubeMaybe(p).getOrElse(p bomb "Both corners must be set!")

  def cubeMaybe(p:Player): Option[Cube[Block]] = corners(p) match {
    case List(c1, c2) => Some(c1 cubeTo c2)
    case _ => None
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

  def cubes: collection.Map[Player, Cube[Block]] =
    corners.state.filter(kv => kv._2.size == 2).mapValues {
      case List(l1, l2) => l1 cubeTo l2
    }
}