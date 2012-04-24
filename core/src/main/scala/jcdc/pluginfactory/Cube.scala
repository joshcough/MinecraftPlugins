package jcdc.pluginfactory

import org.bukkit.block.Block
import org.bukkit.entity.Player
import Pimps._
import org.bukkit.{Material, Location}

case class Cube(l1: Location, l2: Location) {
  override def toString = "Cube(l1: " + l1.xyz + ", l2: " + l2.xyz + ")"
  val world   = l1.world
  val maxX    = math.max(l1.xd, l2.xd)
  val minX    = math.min(l1.xd, l2.xd)
  val maxY    = math.max(l1.yd, l2.yd)
  val minY    = math.min(l1.yd, l2.yd)
  val maxZ    = math.max(l1.zd, l2.zd)
  val minZ    = math.min(l1.zd, l2.zd)
  val blocks  = world.between(l1, l2)

  def walls   = blocks.filter(onWall)
  def onWall(b: Block)    = b.x == l1.x or b.x == l2.x or b.z == l1.z or b.z == l2.z

  def floors  = world.between(world(maxX, minY, maxZ), world(minX, minY, minZ))
  def onFloor(b: Block)   = b.y == minY.toInt

  def ceiling = world.between(world(maxX, maxY, maxZ), world(minX, maxY, minZ))
  def onCeiling(b: Block) = b.y == maxY.toInt

  def insides = world.between(world(maxX-1 , maxY-1, maxZ-1), world(minX-1, minY-1, minZ-1))

  def contains(p: Player)  : Boolean = this.contains(p.loc)
  def contains(l: Location): Boolean =
    l.xd <= maxX and l.xd >= minX and l.yd <= maxY and l.yd >= minY and l.zd <= maxZ and l.zd >= minZ

  def erase: Unit = blocks.foreach(_.erase)
  def set(newM: Material): Unit = for(b <- blocks) b changeTo newM
  def change(oldM: Material, newM: Material): Unit = for(b <- blocks; if b is oldM) b changeTo newM
}
