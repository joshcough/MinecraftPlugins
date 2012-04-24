package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{CommandsPlugin, ListenersPlugin}
import org.bukkit.{Location, Material}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import Material._
import jcdc.pluginfactory.Pimps._


trait CubePlugin {
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
  def cubes: collection.Map[Player, Cube] = positions.filter(_._2._2.isDefined).mapValues{
    case (l1, ol2) => Cube(l1, ol2.get)
  }
}

class WorldEditV2 extends ListenersPlugin with CommandsPlugin with CubePlugin with JCDCPluginFactoryExample {

  val listeners = List(
    OnLeftClickBlock((p, e) => if (p isHoldingA WOOD_AXE) {
      setFirstPosition(p, e.getClickedBlock)
      e.cancel
    }),
    OnRightClickBlock((p, e) => if (p isHoldingA WOOD_AXE) setSecondPosition(p, e.getClickedBlock))
  )

  val commands = List(
    Command(
      name = "/wand",
      desc = "Get a WorldEdit wand.",
      body = noArgs(p => p.world.dropItem(p.loc, new ItemStack(WOOD_AXE, 1)))
    ),
    Command(
      name = "/set",
      desc = "Set all the selected blocks to the given material type.",
      body = args(material) { case p ~ m => run(p)(_.blocks.foreach(_ changeTo m)) }
    ),
    Command(
      name = "/pos1",
      desc = "Set the first position",
      body = noArgs(p => setFirstPosition(p, p.loc))
    ),
    Command(
      name = "/pos2",
      desc = "Set the second position",
      body = noArgs(p => setSecondPosition(p, p.loc))
    ),
    Command(
      name = "/erase",
      desc = "Set all the selected blocks to air.",
      body = noArgs(run(_)(_.erase))
    ),
    Command(
      name = "/change",
      desc = "Change all the selected blocks of the first material type to the second material type.",
      body = args(material ~ material) { case p ~ (oldM ~ newM) =>
        run(p)(cube => for(b <- cube.blocks; if b is oldM) b changeTo newM)
      }
    ),
    Command(
      name = "/walls",
      desc = "Create walls with the given material type.",
      body = args(material) { case p ~ m => run(p)(_.walls.foreach(_ changeTo m)) }
    ),
    Command(
      name = "/empty-tower",
      desc = "Create walls and floor with the given material type, and set everything inside to air.",
      body = args(material) { case p ~ m => run(p)(cube =>
        for(b <- cube.blocks) if (cube.onWall(b) || cube.onFloor(b)) b changeTo m else b.erase
      )}
    ),
    Command(
      name = "/excavate",
      desc = "Dig",
      body = args(oddNum ~ num) { case p ~ (radius ~ depth) =>
        val b = radius / 2
        val (x, y, z) = p.loc.xyzd
        setFirstPosition (p, p.world.blockAt(x + b, y, z + b))
        setSecondPosition(p, p.world.blockAt(x - b, y - depth, z - b))
        run(p)(_.blocks.foreach(_.erase))
      }
    )
  )
}
