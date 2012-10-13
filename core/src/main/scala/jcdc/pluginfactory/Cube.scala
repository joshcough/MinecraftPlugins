package jcdc.pluginfactory

import org.bukkit.block.Block
import org.bukkit.entity.Player
import EnrichmentClasses._
import org.bukkit.{Material, Location}
import collection.JavaConversions.asScalaIterator

object Cube{
  implicit def toStream(c:Cube): Stream[Block] = c.blocks
}

case class Cube(l1: Location, l2: Location) {

  override def toString = s"Cube(l1: ${l1.xyz}, l2: ${l2.xyz})"
  val world     = l1.world

  val maxX      = math.max(l1.x, l2.x)
  val minX      = math.min(l1.x, l2.x)
  val maxY      = math.max(l1.y, l2.y)
  val minY      = math.min(l1.y, l2.y)
  val maxZ      = math.max(l1.z, l2.z)
  val minZ      = math.min(l1.z, l2.z)

  def copy(minX:Int=minX, minY:Int=minY, minZ:Int=minZ,
           maxX:Int=maxX, maxY:Int=maxY, maxZ:Int=maxZ): Cube =
    Cube(new Location(world, minX, minY, minZ), new Location(world, maxX, maxY, maxZ))

  // this must be a def to avoid it memoizing.
  def blocks    = world.between(l1, l2)
  def players: Iterator[Player] = world.getPlayers.iterator.filter(contains)

  def northWall = world.between(world(minX, minY, maxZ), world(maxX, maxY, maxZ))
  def southWall = world.between(world(minX, minY, minZ), world(maxX, maxY, minZ))
  def eastWall  = world.between(world(maxX, minY, minZ), world(maxX, maxY, maxZ))
  def westWall  = world.between(world(minX, minY, minZ), world(minX, maxY, maxZ))

  // TODO: can i do this more efficiently?
  def walls     = blocks.filter(onWall)

  def onWall(b: Block)    = b.x == l1.x or b.x == l2.x or b.z == l1.z or b.z == l2.z

  def floor  = Cube(world(maxX, minY, maxZ), world(minX, minY, minZ))
  def onFloor(b: Block)   = b.y == minY

  def ceiling = Cube(world(maxX, maxY, maxZ), world(minX, maxY, minZ))
  def onCeiling(b: Block) = b.y == maxY

  def insides = world.between(world(maxX-1, maxY-1, maxZ-1), world(minX-1, minY-1, minZ-1))

  // todo: i could really use outsides, or box, or something, that is the inverse of insides.
  // todo: which is walls + ceiling + floor
  // but i should be careful...
  // right now the walls extend into the borders of the ceiling and floor.

  def contains(p: Player)  : Boolean = this.contains(p.loc)
  def contains(l: Location): Boolean = (
    l.xd <= maxX and l.xd >= minX and
    l.yd <= maxY and l.yd >= minY and
    l.zd <= maxZ and l.zd >= minZ
  )

  def eraseAll: Unit = blocks.foreach(_.erase)
  def setAll(newM: MaterialAndData): Unit = for(b <- blocks) newM.update(b)

  def changeAll(oldM: Material, newM: Material, data: Option[Byte]=None): Unit =
    for(b <- blocks; if b is oldM){
      b changeTo newM
      data.foreach(b.setData)
    }

  // expand in all directions
  def expand(n:Int)    = Cube(world(maxX+n, maxY+n, maxZ+n), world(minX-n, minY-n, minZ-n))
  // move the Y up by n
  def expandUp(n:Int)  = growMaxYBy(n)
  // move x and z out by n
  def expandOut(n:Int) = Cube(world(maxX+n, maxY,   maxZ+n), world(minX-n, minY,   minZ-n))
  // move x and z in by n
  def shrinkIn(n:Int)  = {
    val newMaxX = if (maxX - minX <= 1) maxX else maxX - 1
    val newMinX = if (maxX - minX <= 1) minX else minX + 1
    val newMaxZ = if (maxZ - minZ <= 1) maxZ else maxZ - 1
    val newMinZ = if (maxZ - minZ <= 1) minZ else minZ + 1
    Cube(world(newMaxX, maxY, newMaxZ), world(newMinX, minY, newMinZ))
  }
  def growMinXBy(extra:Int)  = copy(minX=minX+extra)
  def growMinYBy(extra:Int)  = copy(minY=minY+extra)
  def growMinZBy(extra:Int)  = copy(minZ=minZ+extra)
  def growMaxXBy(extra:Int)  = copy(maxX=maxX+extra)
  def growMaxYBy(extra:Int)  = copy(maxY=maxY+extra)
  def growMaxZBy(extra:Int)  = copy(maxZ=maxZ+extra)
  def shrinkMinXBy(less:Int) = copy(minX=minX-less)
  def shrinkMinYBy(less:Int) = copy(minY=minY-less)
  def shrinkMinZBy(less:Int) = copy(minZ=minZ-less)
  def shrinkMaxXBy(less:Int) = copy(maxX=maxX-less)
  def shrinkMaxYBy(less:Int) = copy(maxY=maxY-less)
  def shrinkMaxZBy(less:Int) = copy(maxZ=maxZ-less)
  def expandX(i:Int) = growMaxXBy(i).shrinkMinXBy(i)
  def expandZ(i:Int) = growMaxZBy(i).shrinkMinZBy(i)
  def shiftX(i:Int) = growMaxXBy(i).growMinXBy(i)
  def shiftY(i:Int) = growMaxYBy(i).growMinYBy(i)
  def shiftZ(i:Int) = growMaxZBy(i).growMinZBy(i)
}
