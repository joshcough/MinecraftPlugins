package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{Command, CommandsPlugin, ListenersPlugin}
import org.bukkit.{Location, Material}
import org.bukkit.entity.Player
import Material._
import org.bukkit.block.Block

class WorldEditDemo extends ListenersPlugin with CommandsPlugin {

  val corners = collection.mutable.Map[Player, List[Location]]().withDefaultValue(Nil)

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
      body = args(material){ case (p, m) => run(p)(blocks => for(b <- blocks){ b changeTo m }) }
    ),
    Command(
      name = "change",
      desc = "Change all the selected blocks of the first material type to the second material type.",
      body = args(material ~ material){
        case (p, oldM ~ newM) =>
          run(p)(blocks => for(b <- blocks; if(b is oldM)){ b changeTo newM })
      }
    )
  )

  // helper functions
  def setFirstPos(p:Player, loc: Location): Unit = {
    corners.update(p, List(loc))
    p ! (s"first corner set to: ${loc.xyz}")
  }
  def setSecondPos(p:Player, loc2: Location): Unit = corners(p) match {
    case List(loc1) =>
      corners.update(p, List(loc1, loc2))
      p ! (s"second corner set to: ${loc2.xyz}")
    case List(loc1, _) =>
      corners.update(p, List(loc1, loc2))
      p ! (s"second corner set to: ${loc2.xyz}")
    case _ =>
      p ! "set corner one first! (with a left click)"
  }

  def blocksBetween(loc1:Location, loc2: Location): Iterator[Block] = {
    val ((x1, y1, z1), (x2, y2, z2)) = (loc1.xyz, loc2.xyz)
    def range(i1: Int, i2: Int) = (if(i1 < i2) i1 to i2 else i2 to i1).iterator
    for (x <- range(x1,x2); y <- range(y1,y2); z <- range(z1,z2)) yield loc1.world(x,y,z)
  }

  def run(p: Player)(f: Iterator[Block] => Unit) = corners.get(p).filter(_.size == 2).
    fold(p ! "Both corners must be set!")(ls => f(blocksBetween(ls(0), ls(1))))
}
