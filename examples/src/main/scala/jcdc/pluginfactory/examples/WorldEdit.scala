package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{Command, CommandsPlugin, Cube, ListenersPlugin}
import org.bukkit.{Location, Material}
import org.bukkit.entity.Player
import Material._

class WorldEdit extends ListenersPlugin with CommandsPlugin {

  trait Corners
  case object NoCorners extends Corners
  case class  OneCorner(loc: Location) extends Corners
  case class  BothCorners(cube:Cube)   extends Corners

  val corners = collection.mutable.Map[Player, Corners]().withDefaultValue(NoCorners)

  val listeners = List(
    OnLeftClickBlock((p, e)  => if (p isHoldingA WOOD_AXE) { setFirstPos (p, e.loc); e.cancel }),
    OnRightClickBlock((p, e) => if (p isHoldingA WOOD_AXE) { setSecondPos(p, e.loc) })
  )

  val commands = List(
    Command(
      name = "wand",
      desc = "Get a WorldEdit wand.",
      body = noArgs(_.loc.dropItem(WOOD_AXE))
    ),
    Command(
      name = "set",
      desc = "Set all the selected blocks to the given material type.",
      body = args(material){ case (p, m) => run(p)(_.blocks.foreach(_ changeTo m)) }
    ),
    Command(
      name = "change",
      desc = "Change all the selected blocks of the first material type to the second material type.",
      body = args(material ~ material){
        case (p, oldM ~ newM) => run(p)(_.blocks.filter(_ is oldM).foreach(_ changeTo newM))
      }
    )
  )

  // helper functions
  def setFirstPos(p:Player, loc: Location): Unit = {
    corners.update(p, OneCorner(loc))
    p ! ("first corner set to: " + loc.xyz)
  }
  def setSecondPos(p:Player, loc2: Location): Unit = corners(p) match {
    case OneCorner(loc1) =>
      corners.update(p, BothCorners(Cube(loc1, loc2)))
      p ! ("second corner set to: " + loc2.xyz)
    case BothCorners(Cube(loc1, _)) =>
      corners.update(p, BothCorners(Cube(loc1, loc2)))
      p ! ("second corner set to: " + loc2.xyz)
    case _ =>
      p ! "set corner one first! (with a left click)"
  }
  def run (p: Player)(f: Cube => Unit) = corners.get(p).collect{ case b: BothCorners => b }.
    map(_.cube).fold(p ! "Both corners must be set!")(f)
}
