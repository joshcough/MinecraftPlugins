package jcdc.pluginfactory

import org.bukkit.block.Block
import org.bukkit.entity.Player
import BukkitEnrichment._
import org.bukkit.{Material, Location}
import collection.JavaConversions.asScalaIterator

object MineCraftCube {
  implicit class LocationToCoor(l: Location){
    def coor = Coor(l.xd, l.yd, l.zd)
  }
  implicit class BlockToCoor(b: Block){
    def coor = Coor(b.xd, b.yd, b.zd)
  }
  def apply(b1: Block, b2: Block): MineCraftCube = new MineCraftCube(b1.loc, b2.loc)
}

case class MineCraftCube(loc1: Location, loc2: Location) extends Cube[Block] {

  import MineCraftCube._

  val corner1 = loc1.coor
  val corner2 = loc2.coor
  val f = (c: Coor) => loc1.world(c.xd, c.yd, c.zd)

  val world = loc1.world

  def contains(p: Player)  : Boolean = this.contains(p.loc.coor)
  def contains(l: Location): Boolean = this.contains(l.coor)

  def blocks = toStream

  //  def onWall(b: Block): Boolean = onWall(b.coor)

  /**
   * Set all the the blocks in this cube to the new type.
   * @param newM
   */
  def setAll(newM: Material)       : Int = setAll(new MaterialAndData(newM, None))
  def setAll(newM: MaterialAndData): Int = blocks.count(newM update _)

  /**
   * Change all of the blocks in this cube that are of the old material type
   * to the new material type.
   * @param oldM
   * @param newM
   */
  def changeAll(oldM: Material, newM: Material): Int =
    changeAll(oldM, new MaterialAndData(newM, None))
  def changeAll(oldM: Material, newM: MaterialAndData): Int =
    blocks.filter(_ is oldM).count(newM update _)


  /**
   * Set all the blocks in this Cube to AIR
   */
  def eraseAll: Int = toStream.count(_.erase)

  def players: Iterator[Player] = world.getPlayers.iterator.filter(contains)

  /**
   * this is pretty close to map, on a Cube...
   * @param newL1
   */
  def paste(newL1: Location): Int = {
    def translate(b: Block): Block = world(
      b.xd + (newL1.xd - b.xd) + (b.xd - minXd),
      b.yd + (newL1.yd - b.yd) + (b.yd - minYd),
      b.zd + (newL1.zd - b.zd) + (b.zd - minZd)
    )
    blocks.count(b => b.materialAndData update translate(b))
  }
}