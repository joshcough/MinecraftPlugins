package com.joshcough.minecraft

import BukkitEnrichment._
import org.bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player

trait CubeState {

  case class Locations(firstLoc : Location, secondLoc : Option[Location]){
    def bothLocationsSet: Boolean = secondLoc.isDefined
  }

  val corners: PlayerState[Locations] = new PlayerState[Locations] {
    override val default: Option[Locations] = None
  }

  def cube(p:Player): Cube[Block] =
    cubeMaybe(p).getOrElse(p bomb "Both corners must be set!")

  def cubeMaybe(p:Player): Option[Cube[Block]] = corners.get(p) match {
    case Some(Locations(c1, Some(c2))) => Some(c1 cubeTo c2)
    case _ => None
  }

  def setFirstPosition(p:Player,loc: Location): Unit = {
    corners += (p -> Locations(loc, None))
    p ! s"first corner set to: ${loc.xyz}"
  }

  def setSecondPosition(p:Player,loc2: Location): Unit = corners.get(p) match {
    case Some(Locations(loc1, _)) =>
      corners += (p -> Locations(loc1, Some(loc2)))
      p ! s"second corner set to: ${loc2.xyz}"
    case _ =>
      p ! "set corner one first! (with a left click)"
  }

  def cubes: collection.Map[Player, Cube[Block]] =
    corners.state.filter(kv => kv._2.bothLocationsSet).view.mapValues {
      case Locations(l1, Some(l2)) => l1 cubeTo l2
      case Locations(_, None) => sys error "impossible, because we filtered."
    }.toMap
}
