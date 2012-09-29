package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{Command, CommandsPlugin, Cube, ListenersPlugin}
import org.bukkit.{Location, Material}
import org.bukkit.entity.Player
import Material._
import org.bukkit.block.Block

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
      // TODO: maybe show that WOOD_AXE is implicitly converted to an ItemStack here
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
        case (p, oldM ~ newM) => run(p)(c => for(b <- c.blocks; if(b is oldM)){ b changeTo newM })
      }
    ),
    // TODO: early termination example. maybe put in a different file
    Command(
      name = "find",
      desc = "Checks if your cube contains any of the given material, and tells where.",
      body = args(material){ case (p, m) =>
        run(p){_.blocks.find(_ is m).fold(
          s"No $m found in your cube!")(b => s"$m found at ${b.loc.xyz}")
        }
      }
    ),
    Command(
      name = "fib-tower",
      desc = "create a tower from the fib numbers",
      body = args(int ~ material){ case (p, i ~ m) =>
        lazy val fibs: Stream[Int] = 0 #:: 1 #:: fibs.zip(fibs.tail).map{case (i,j) => i+j}
        for{
          (startBlock,n) <- p.world.fromX(p.loc).zip(fibs take i)
          towerBlock     <- startBlock.andBlocksAbove take n
        } towerBlock changeTo m
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
