package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{Command, CommandsPlugin, Cube, ListenersPlugin}
import org.bukkit.{Location, Material}
import org.bukkit.entity.Player
import Material._

class WorldEdit extends ListenersPlugin with CommandsPlugin {

  val corners = collection.mutable.Map[Player, Corners]().withDefaultValue(NoCorners)

  trait Corners
  case object NoCorners extends Corners
  case class  OneCorner(loc: Location) extends Corners
  case class  BothCorners(loc1: Location, loc2: Location) extends Corners

  val listeners = List(
    OnLeftClickBlock((p, e)  => if (p isHoldingA WOOD_AXE) { setFirstPos (p, e.loc); e.cancel }),
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
    ),
    Command(
      name = "corner1",
      desc = "Set the first corner of your cube to your position, or an x, y, z",
      body = args(opt(num ~ num ~ num)){ case p ~ loc => setFirstPos(p,
        loc.fold(p.loc){ case x ~ y ~ z => p.world(x,y,z) }
      )}
    ),
    Command(
      name = "corner2",
      desc = "Set the second corner of your cube to your position, or an x, y, z",
      body = args(opt(num ~ num ~ num)){ case p ~ loc => setSecondPos(p,
        loc.fold(p.loc){ case x ~ y ~ z => p.world(x,y,z) }
      )}
    )
  )

  // helper functions
  def setFirstPos(p:Player, loc: Location): Unit = {
    corners.update(p, OneCorner(loc))
    p ! ("first corner set to: " + loc.xyz)
  }
  def setSecondPos(p:Player, loc2: Location): Unit = corners(p) match {
    case OneCorner(loc1) =>
      corners.update(p, BothCorners(loc1, loc2))
      p ! ("second corner set to: " + loc2.xyz)
    case BothCorners(loc1, _) =>
      corners.update(p, BothCorners(loc1, loc2))
      p ! ("second corner set to: " + loc2.xyz)
    case _ =>
      p ! "set corner one first! (with a left click)"
  }
  def cube(p: Player): Option[Cube] = corners(p) match {
    case BothCorners(loc1, loc2) => Some(Cube(loc1, loc2))
    case _ => None
  }
  def run (p: Player)(f: Cube => Unit) = cube(p).fold(p ! "Both corners must be set!")(f)
}
