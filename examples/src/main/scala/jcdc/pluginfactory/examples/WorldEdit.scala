package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{CommandsPlugin, ListenersPlugin}
import org.bukkit.{Location, Material}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import Material._

class WorldEdit extends ListenersPlugin with CommandsPlugin {

  val positions   = collection.mutable.Map[Player, (Location, Option[Location])]()
  def getPositions(p: Player): Option[(Location, Location)] =
    positions.get(p).flatMap(lol   => lol._2.map(loc => (lol._1, loc)))

  val listeners = List(
    OnPlayerLeftClickBlock((p, e)  => if (p isHoldingA WOOD_AXE) {
      positions.update(p, (e.getClickedBlock.loc, None))
      p ! ("first position set to: " + e.getClickedBlock.loc.xyz)
    }),
    OnPlayerRightClickBlock((p, e) => if (p isHoldingA WOOD_AXE) {
      positions.get(p).map(_._1).foreach(l => {
        positions.update(p, (l, Some(e.getClickedBlock.loc)))
        p ! ("second position set to: " + e.getClickedBlock.loc.xyz)
      })
    }
  ))

  val commands = Map(
    "/wand" -> command((p, _) => p.world.dropItem(p.loc, new ItemStack(WOOD_AXE, 1))),
    "/list" -> oneArg((p, _)  => positions.get(p).fold(
      p ! "No positions set!",
      lol => p ! ("first: " + lol._1.xyz + ", second: " + lol._2.map(_.xyz).getOrElse("None"))
    )),
    "/set"  -> oneArg((p, c)  => getPositions(p).fold(
      p ! "Both positions need to be set!",
      locs => p.withMaterial(c.args.head)(m => {
        val (x1, y1, z1) = locs._1.xyz
        val (x2, y2, z2) = locs._2.xyz
        def range(i1: Int, i2: Int) = if(i1 < i2) i1 to i2 else i2 to i1
        for (x <- range(x1, x2); y <- range(y1, y2); z <- range(z1, z2)){
          p.world.blockAt(x, y, z).setType(m)
        }
      })
    ))
  )
}
