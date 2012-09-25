package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{Command, CommandsPlugin, Cube, ListenersPlugin}
import org.bukkit.{Location, Material}
import org.bukkit.entity.Player
import Material._
import org.bukkit.event.player.PlayerInteractEvent

class WorldEdit extends ListenersPlugin with CommandsPlugin {

  val positions = collection.mutable.Map[Player, (Location, Option[Location])]()

  val listeners = List(
    OnLeftClickBlock((p, e)  => if (p isHoldingA WOOD_AXE) setFirstPos (p, e)),
    OnRightClickBlock((p, e) => if (p isHoldingA WOOD_AXE) setSecondPos(p, e.loc))
  )

  val commands = List(
    Command(
      name = "wand",
      desc = "Get a WorldEdit wand.",
      body = noArgs(_.loc.dropItem(WOOD_AXE))
    ),
    Command(
      name = "/set",
      desc = "Set all the selected blocks to the given material type.",
      body = args(material){ case p ~ m  => run(p)(_.blocks.foreach(_ changeTo m))}
    ),
    Command(
      name = "/change",
      desc = "Change all the selected blocks of the first material type to the second material type.",
      body = args(material ~ material){ case p ~ (oldM ~ newM) =>
               run(p)(_.blocks.filter(_ is oldM).foreach(_ changeTo newM))}
    )
  )

  // helper functions
  def setFirstPos(p:Player, e:PlayerInteractEvent): Unit = {
    positions.update(p, (e.loc, None))
    e.cancel
    p ! ("first position set to: " + e.loc.xyz)
  }
  def setSecondPos(p:Player, loc: Location): Unit =
    for(l <- positions.get(p).map(_._1)) {
      positions.update(p, (l, Some(loc)))
      p ! ("second position set to: " + loc.xyz)
    }
  def cube(p: Player): Option[Cube] = positions.get(p).flatMap(lol => lol._2.map(Cube(lol._1, _)))
  def run (p: Player)(f: Cube => Unit) = cube(p).fold(p ! "Both positions must be set!")(f)
}
