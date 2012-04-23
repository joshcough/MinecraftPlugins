package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{CommandsPlugin, ListenersPlugin}
import org.bukkit.{Location, Material}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import Material._
import org.bukkit.block.Block

class WorldEditV2 extends ListenersPlugin with CommandsPlugin with JCDCPluginFactoryExample {

  val positions = collection.mutable.Map[Player, (Location, Option[Location])]()

  val listeners = List(
    OnLeftClickBlock((p, e) => if (p isHoldingA WOOD_AXE) {
      setFirstPosition(p, e.getClickedBlock)
      e.cancel
    }),
    OnRightClickBlock((p, e) =>
      if (p isHoldingA WOOD_AXE) setSecondPosition(p, e.getClickedBlock))
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
      body = noArgs (p => run(p)(_.blocks.foreach(_ changeTo AIR)))
    ),
    Command(
      name = "/change",
      desc = "Change all the selected blocks of the first material type to the second material type.",
      body = args(material ~ material) { case p ~ (oldM ~ newM) =>
        run(p)(_.blocks.filter(_ is oldM).map(_ changeTo newM).force)
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
      body = args(material) { case p ~ m =>
        run(p)(g => g.blocks.map(b => if (g.onWall(b)) b changeTo m else b changeTo AIR))
      }
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

  // helper functions
  case class Grid(l1: Location, l2: Location, blocks: Stream[Block]) {
    def walls = blocks.filter(onWall)
    def onWall(b: Block) = b.x == l1.x || b.x == l2.x || b.z == l1.z || b.z == l2.z
  }
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
  def getPositions(p: Player): Option[(Location, Location)] =
    positions.get(p).flatMap(lol => lol._2.map(loc => (lol._1, loc)))
  def run(p: Player)(f: Grid => Unit) =
    getPositions(p).fold(p ! "Both positions need to be set!")(locs =>
      f(Grid(locs._1, locs._2, p.world.between(locs._1, locs._2))))
}
