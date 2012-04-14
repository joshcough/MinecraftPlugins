package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{CommandsPlugin, ListenersPlugin}
import org.bukkit.{Location, Material}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import Material._

class WorldEdit extends ListenersPlugin with CommandsPlugin {

  val positions   = collection.mutable.Map[Player, (Location, Option[Location])]()

  val listeners = List(
    OnPlayerLeftClickBlock((p, e)  => if (p isHoldingA WOOD_AXE) {
      positions.update(p, (e.getClickedBlock.loc, None))
      e.cancel
      p ! ("first position set to: " + e.getClickedBlock.loc.xyz)
    }),
    OnPlayerRightClickBlock((p, e) => if (p isHoldingA WOOD_AXE) {
      for(l <- positions.get(p).map(_._1)) {
        positions.update(p, (l, Some(e.getClickedBlock.loc)))
        p ! ("second position set to: " + e.getClickedBlock.loc.xyz)
      }
    })
  )

  val commands = List(
    Command("/wand",
      "Get a WorldEdit wand.",
      noArgs(p => p.world.dropItem(p.loc, new ItemStack(WOOD_AXE, 1)))),
    Command("/set",
      "Set all the selected blocks to the given material type.",
      args(material){ case p ~ m  => run(p)((l1, l2) => p.world.between(l1, l2).foreach(_ changeTo m))}),
    Command("/change",
      "Change all the selected blocks from the first material type to the second material type.",
      args(material ~ material){ case p ~ (oldM ~ newM) =>
      run(p)((l1, l2) => p.world.between(l1, l2).filter(_ is oldM).map(_ changeTo newM).force)
    })
  )

  // helper functions
  def getPositions(p: Player): Option[(Location, Location)] =
    positions.get(p).flatMap(lol   => lol._2.map(loc => (lol._1, loc)))
  def run(p: Player)(f: (Location, Location) => Unit) =
    getPositions(p).fold(p ! "Both positions need to be set!")(locs => f(locs._1, locs._2))
}
