package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{CommandsPlugin, ListenersPlugin}
import org.bukkit.{Location, Material}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import Material._
import org.bukkit.block.Block

class WorldEdit extends ListenersPlugin with CommandsPlugin with JCDCPluginFactoryExample {

  val positions = collection.mutable.Map[Player, (Location, Option[Location])]()

  val listeners = List(
    OnLeftClickBlock((p, e)  => if (p isHoldingA WOOD_AXE) {
      positions.update(p, (e.getClickedBlock.loc, None))
      e.cancel
      p ! ("first position set to: " + e.getClickedBlock.loc.xyz)
    }),
    OnRightClickBlock((p, e) => if (p isHoldingA WOOD_AXE) {
      for(l <- positions.get(p).map(_._1)) {
        positions.update(p, (l, Some(e.getClickedBlock.loc)))
        p ! ("second position set to: " + e.getClickedBlock.loc.xyz)
      }
    })
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
      body = args(material){ case p ~ m  => run(p)(_.blocks.foreach(_ changeTo m))}
    ),
    Command(
      name = "/change",
      desc = "Change all the selected blocks of the first material type to the second material type.",
      body = args(material ~ material){ case p ~ (oldM ~ newM) =>
               run(p)(_.blocks.filter(_ is oldM).map(_ changeTo newM).force)}
    ),
    Command(
      name = "/walls",
      desc = "Create walls with the given material type.",
      body = args(material){ case p ~ m => run(p)(g => g.blocks.filter(
               b => b.x == g.l1.x || b.x == g.l2.x || b.z == g.l1.z || b.z == g.l2.z
             ).map(_ changeTo m).force)}
    )
  )

  // helper functions
  def getPositions(p: Player): Option[(Location, Location)] =
    positions.get(p).flatMap(lol   => lol._2.map(loc => (lol._1, loc)))
  case class Grid(l1:Location, l2: Location, blocks: Stream[Block])
  def run(p: Player)(f: Grid => Unit) =
    getPositions(p).fold(p ! "Both positions need to be set!")(locs =>
      f(Grid(locs._1, locs._2, p.world.between(locs._1, locs._2))))
}
