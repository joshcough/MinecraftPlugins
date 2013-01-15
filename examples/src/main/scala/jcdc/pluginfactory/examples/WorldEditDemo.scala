package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{CommandsPlugin, ListenersPlugin}
import org.bukkit.{Location, Material}
import org.bukkit.block.Block
import org.bukkit.entity.Player
import Material.WOOD_AXE

class WorldEditDemo extends ListenersPlugin with CommandsPlugin {

  val corners = collection.mutable.Map[Player, List[Location]]().withDefaultValue(Nil)

  val listeners = List(
    OnLeftClickBlock ((p, e) => if (p isHoldingA WOOD_AXE) { setFirstPos (p, e.loc); e.cancel }),
    OnRightClickBlock((p, e) => if (p isHoldingA WOOD_AXE) { setSecondPos(p, e.loc) })
  )

  val commands = List(
    Command(
      name = "set" ,
      desc = "Set all the selected blocks to the given material type.",
      args = material)(
      body = { case (p, m) => for(b <- cube(p)) b changeTo m }
    ),
    Command(
      name = "replace",
      desc = "Replace all the selected blocks of the first material type to the second material type.",
      args = material ~ material)(
      body = { case (p, oldM ~ newM) => for(b <- cube(p); if(b is oldM)) b changeTo newM }
    )
  )

  def cube(p:Player):Stream[Block] = corners(p).filter(_.length == 2) match {
    case List(loc1, loc2) => loc1.cubeTo(loc2).blocks
    case _                => p ! "Both corners must be set!"; Stream[Block]()
  }

  def setFirstPos(p:Player,loc: Location): Unit = {
    corners += (p -> List(loc))
    p ! s"first corner set to: ${loc.xyz}"
  }

  def setSecondPos(p:Player,loc2: Location): Unit = corners(p) match {
    case loc1 :: _ =>
      corners += (p -> List(loc1, loc2))
      p ! s"second corner set to: ${loc2.xyz}"
    case Nil =>
      p ! "set corner one first! (with a left click)"
  }
}