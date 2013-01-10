package jcdc.pluginfactory

import org.bukkit.block.Block
import org.bukkit.entity.Player
import BukkitEnrichment._
import org.bukkit.Location
import collection.JavaConversions.asScalaIterator

object MineCraftCube {

  def apply(b1: Block, b2: Block): Cube[Block] = this(b1.loc, b2.loc)
  def apply(l1: Location, l2: Location): Cube[Block] =
    Cube[Block](l1.coor, l2.coor)((c: Coor) => l1.world(c.xd, c.yd, c.zd))

  implicit class LocationToCoor(l: Location){ def coor = Coor(l.xd, l.yd, l.zd) }
  implicit class BlockToCoor(b: Block){ def coor = Coor(b.xd, b.yd, b.zd) }

  implicit class RichCubeOfBlocks(c: Cube[Block]) {
    def world = c(Coor(0,0,0)).world
    def blocks = c.toStream
    def blocksAndMaterials = blocks.map(b => (b, b.materialAndData))
    def players: Iterator[Player] = world.getPlayers.iterator.filter(contains)
    def contains(p: Player)  : Boolean = c.contains(p.loc.coor)
    def contains(l: Location): Boolean = c.contains(l.coor)
  }
}
