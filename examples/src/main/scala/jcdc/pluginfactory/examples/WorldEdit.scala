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
      for(l <- positions.get(p).map(_._1)) {
        positions.update(p, (l, Some(e.getClickedBlock.loc)))
        p ! ("second position set to: " + e.getClickedBlock.loc.xyz)
      }
    })
  )

  def run(p: Player)(f: (Location, Location) => Unit) =
    getPositions(p).fold(p ! "Both positions need to be set!")(locs => f(locs._1, locs._2))

  val commands = Map(
    "/wand"   -> command((p, _) => p.world.dropItem(p.loc, new ItemStack(WOOD_AXE, 1))),
    "/list"   -> command((p, _) => positions.get(p).fold(
      p ! "No positions set!")(
      lol => p ! ("first: " + lol._1.xyz + ", second: " + lol._2.fold("Unset!")(_.xyz.toString))
    )),
    "/set"    -> materialCommand((p, m, _)  =>
      run(p)((l1, l2) => p.world.between(l1, l2).foreach(_ changeTo m))
    ),
    "/change" -> twoArgs(command((p, c) =>
      p.withMaterial(c.args(0))(oldM => p.withMaterial(c.args(1))(newM =>
        run(p)((l1, l2) => p.world.between(l1, l2).filter(_ is oldM).map(_ changeTo newM).force)
    ))))
  )
}

/**
 TODO: figure out how to change this into a monad.

"/change" -> twoArgs(command((p, c) =>
p.withMaterial(c.args(0))(oldM => p.withMaterial(c.args(1))(newM =>
run(p)((l1, l2) => p.world.between(l1, l2).filter(_ is oldM).map(_ changeTo newM).force)
))))

"/change" -> twoArgs(command((p, c) =>
   for{
    oldM <- player.findMaterial(c.args(0))
    newM <- player.findMaterial(c.args(1))
   } p.world.between(l1, l2).filter(_ is oldM).map(_ changeTo newM).force)
 **/