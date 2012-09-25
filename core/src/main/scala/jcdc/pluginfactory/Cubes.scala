package jcdc.pluginfactory

import EnrichmentClasses._
import org.bukkit.Location
import org.bukkit.entity.Player

trait Cubes {
  val positions = collection.mutable.Map[Player, (Location, Option[Location])]()

  def setFirstPosition(p: Player, loc: Location) {
    positions.update(p, loc -> None)
    p ! ("first position set to: " + loc.xyz)
  }
  def setSecondPosition(p: Player, loc: Location) {
    positions.get(p).map(_._1).fold(p ! "You must set position 1 first!")(l => {
      positions.update(p, l -> Some(loc))
      p ! ("second position set to: " + loc.xyz)
    })
  }
  def cube(p: Player): Option[Cube] = positions.get(p).flatMap(lol => lol._2.map(Cube(lol._1, _)))
  def run(p: Player)(f: Cube => Unit) = cube(p).fold(p ! "Both positions must be set!")(f)
  def cubes: collection.Map[Player, Cube] = positions.filter(_._2._2.isDefined).mapValues {
    case (l1, ol2) => Cube(l1, ol2.get)
  }
}