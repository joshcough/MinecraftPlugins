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

  case class Change(b: Block, oldM: MaterialAndData){
    override def toString = s"Change(b:${b.loc.xyz} m:${oldM.m.name})"
  }

  object PotentialChange {
    def apply(c: Change) = new PotentialChange(c.b, c.oldM)
    def apply(b: Block, m: Material) = new PotentialChange(b, m.andData)
  }

  case class PotentialChange(b: Block, newM: MaterialAndData){
    val oldM = b.materialAndData
    def run: Boolean = newM update b
  }

  case class PotentialSwap(b1: Block, b2: Block){
    def run: Seq[Change] = {
      val oldB1M = b1.materialAndData
      val oldB2M = b2.materialAndData
      List(
        oldB1M.update(b2).toOption(Change(b2, oldB2M)),
        oldB2M.update(b1).toOption(Change(b1, oldB1M))
      ).flatten
    }
  }

  case class Changes(cs:Array[Change]){
    override def toString = cs.toList.mkString(",")
    def size = cs.length
  }
  type PotentialChanges = Stream[PotentialChange]

  object Changer {
    def changeAll(bms: Stream[Block], newM: MaterialAndData) = runChanges(
      bms.zip(Stream.continually(newM)).map{ case (b,n) => PotentialChange(b,n) }
    )

    def runChanges(newData: Seq[PotentialChange]): Changes =
      Changes(newData.filter(_.run).map(p => Change(p.b, p.oldM)).toArray)

    def runSwaps(swaps: Seq[PotentialSwap]): Changes = Changes(swaps.flatMap(_.run).toArray)
  }
}

case class MineCraftCube(loc1: Location, loc2: Location) extends Cube[Block] {

  import MineCraftCube._

  val corner1 = loc1.coor
  val corner2 = loc2.coor
  val f = (c: Coor) => loc1.world(c.xd, c.yd, c.zd)

  val world = loc1.world
  def players: Iterator[Player] = world.getPlayers.iterator.filter(contains)

  def blocks = toStream
  def blocksAndMaterials = blocks.map(b => (b, b.materialAndData))

  def contains(p: Player)  : Boolean = this.contains(p.loc.coor)
  def contains(l: Location): Boolean = this.contains(l.coor)

  /**
   * Set all the the blocks in this cube to the new type.
   * @param newM
   */
  def setAll(newM: Material): Changes = setAll(new MaterialAndData(newM, None))

  /**
   * Set all the the blocks in this cube to the new type.
   * @param newM
   */
  def setAll(newM: MaterialAndData): Changes = Changer.changeAll(blocks, newM)

  /**
   * Change all of the blocks in this cube that are of the old material type
   * to the new material type.
   * @param oldM
   * @param newM
   */
  def changeAll(oldM: Material, newM: Material): Changes =
    changeAll(oldM, new MaterialAndData(newM, None))

  /**
   * Change all of the blocks in this cube that are of the old material type
   * to the new material type.
   * @param oldM
   * @param newM
   */
  def changeAll(oldM: Material, newM: MaterialAndData): Changes =
    Changer.changeAll(blocks.filter(_ is oldM), newM)

  /**
   * Set all the blocks in this Cube to AIR
   */
  def eraseAll: Changes = Changer.changeAll(blocks, MaterialAndData.AIR)

  /**
   * this is pretty close to map, on a Cube...
   * @param newL1
   */
  def paste(newL1: Location): Changes = Changer.runChanges(
    paste(newL1.coor).toStream.map { case (oldB, newB) =>
      PotentialChange(newB, oldB.materialAndData)
    }
  )

  /**
   * @param newL1
   */
  def move(newL1: Location): Changes = Changer.runChanges(
    paste(newL1.coor).toStream.flatMap { case (oldB, newB) =>
      Stream(PotentialChange(newB, oldB.materialAndData), PotentialChange(oldB, Material.AIR))
    }
  )

  def mirrorYChanges: Changes = Changer.runSwaps(
    mirrorY.toStream.map { case (oldB, newB) => PotentialSwap(oldB, newB) }
  )

//  /**
//   *
//   * @param newL1
//   * @return
//   */
//  def pasteMirrorY(newL1: Location): Changes = Changer.runChanges(
//    paste(newL1.coor).mirrorY.toStream.map { case (oldB, newB) =>
//      PotentialChange(newB, oldB.materialAndData)
//    }
//  )
}