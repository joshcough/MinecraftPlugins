package com.joshcough.minecraft

import org.bukkit.block.Block
import org.bukkit.Material
import com.joshcough.minecraft.BukkitEnrichment._
import Cube._

// GIANT TODO
// GIANT TODO: force some of the streams in mirroring!
// GIANT TODO
object CubeModifier {

  object PotentialChange {
    def apply(c: Change) = new PotentialChange(c.b, c.oldM)
    def apply(b: Block, m: Material) = new PotentialChange(b, m.andData)
  }

  /**
   * Represents a change that might happen in the world at some later time.
   * @param b
   * @param newM
   */
  case class PotentialChange(b: Block, newM: MaterialAndData){
    val oldM = b.materialAndData
    def run: Boolean = newM update b
  }

  type PotentialChanges = Stream[PotentialChange]

  /**
   * Represents a change that actually took place in the world.
   * @param b The block that was changed.
   * @param oldM The blocks previous material before it was changed
   */
  case class Change(b: Block, oldM: MaterialAndData){
    override def toString = s"Change(b:${b.loc.xyz} m:${oldM.m.name})"
  }

  /**
   * Represents a number of changes that actually took place in the world.
   * @param cs
   */
  case class Changes(cs:Array[Change]){
    override def toString = cs.toList.mkString(",")
    def size = cs.length
    def ++(cs: Changes) = Changes(this.cs ++ cs.cs)
  }

  /**
   * Actually execute some PotentialChanges,
   * handing back a Seq of all the changes that really took place.
   * (A potential change might not happen, if for example, you try to change AIR to AIR.)
   * @param newData
   * @return
   */
  def runChanges(newData: Seq[PotentialChange]): Changes =
    Changes(newData.filter(_.run).map(p => Change(p.b, p.oldM)).toArray)

  /**
   * TODO: document me!
   */
  def getTransformationChanges(cube: Cube[Block],
                               force: Boolean = false): Stream[PotentialChange] = {
    val s = cube.toZippedStream.map{ case (c,b) =>
      PotentialChange(cube.world(c.x, c.y, c.z), b.materialAndData)
    }
    if(force) s.force else s
  }

  def translateAll(cube: Cube[Block], force: Boolean = false): Changes =
    runChanges(getTransformationChanges(cube, force))

  /**
   * Set all the blocks in this cube to the given Material
   */
  def setAll(c: Cube[Block], newM: Material): Changes = setAll(c.blocks, newM.andData)

  /**
   * Set all the blocks in this stream to the given Material
   */
  def setAll(bms: Stream[Block], newM: MaterialAndData) = runChanges(
    bms.zip(Stream.continually(newM)).map{ case (b,n) => PotentialChange(b,n) }
  )

  /**
   * Change all the blocks of the old material type to the new material type.
   */
  def changeAll(c: Cube[Block], oldM: Material, newM: MaterialAndData): Changes =
    setAll(c.blocks.filter(_ is oldM), newM)

  /**
   * Set all the blocks in this cube to air
   * TODO: this really could be removed...
   */
  def eraseAll(c: Cube[Block]): Changes = setAll(c.blocks, MaterialAndData.AIR)
}

//  case class PotentialSwap(b1: Block, b2: Block){
//    def run: Seq[Change] = {
//      val oldB1M = b1.materialAndData
//      val oldB2M = b2.materialAndData
//      List(
//        oldB1M.update(b2).toOption(Change(b2, oldB2M)),
//        oldB2M.update(b1).toOption(Change(b1, oldB1M))
//      ).flatten
//    }
//  }
//    def runSwaps(swaps: Seq[PotentialSwap]): Changes = Changes(swaps.flatMap(_.run).toArray)


//
//    def paste(newL1: Location): Changes = Changer.runChanges(run(c.paste(newL1.coor)))
//    def pasteMirrorY(newL1: Location): Changes = Changer.runChanges(run(c.paste(newL1.coor).mirrorY))

//    /**
//     * We have to force these (.force), because if they are run lazily,
//     * then a will be replaced with b, and later when b trieds to get replaced with a,
//     * a's material type is already what b is, so b just gets set to itself.
//     * Forcing guarantees that we get the right data values in the list.
//     * @return
//     */
//    def mirrorXChanges: Changes = Changer.runChanges(run(c.mirrorX).force)
//    def mirrorYChanges: Changes = Changer.runChanges(run(c.mirrorY).force)
//    def mirrorZChanges: Changes = Changer.runChanges(run(c.mirrorZ).force)
//
//    /**
//     * @param newL1
//     */
//    def move(newL1: Location): Changes = paste(newL1) ++ setAll(Material.AIR)

//import org.squeryl.{KeyedEntity, Schema}
//import org.squeryl.dsl.{OneToMany, ManyToOne}
//import org.squeryl.PrimitiveTypeMode._
