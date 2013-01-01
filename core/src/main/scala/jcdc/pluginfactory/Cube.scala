package jcdc.pluginfactory

import org.bukkit.block.Block
import org.bukkit.entity.Player
import BukkitEnrichment._
import org.bukkit.{World, Material, Location}
import collection.JavaConversions.asScalaIterator

object Cube{
  type TLoc = (Int,Int,Int)
  implicit def toStream(c:Cube): Stream[Block] = c.blocks
  implicit def toLocation(t:TLoc)(implicit w:World) = w(t._1, t._2, t._3)
  def apply(c1:TLoc,  c2:TLoc) (implicit w:World): Cube = Cube(toLocation(c1), toLocation(c2))
  def apply(b1:Block, b2:Block)(implicit w:World): Cube = Cube(b1.loc, b2.loc)
}

/**
 * Awesome class for manipulating all blocks in a 3D cube between two corners of the world.
 * The minecraft world looks like this:
 *
 *          (minZ)
 *           -Z
 *            N
 *
 *  (-X)  W       E (+X)
 * (minX)          (maxX)
 *            S
 *           +Z
 *          (maxZ)
 *
 *  y increases going up towards the clouds
 *  y decreases going down towards the bottom (or core) of the world.
 *
 *  north: z goes down
 *  south: z goes up
 *  east: x goes up
 *  west: x goes down
 *  x axis is width
 *  y axis is height
 *  z axis is depth
 *
 * @param l1 one corner of the cube
 * @param l2 the other corner
 */
case class Cube(l1: Location, l2: Location) {

  import Cube._

  val maxX  = math.max(l1.x, l2.x)
  val minX  = math.min(l1.x, l2.x)
  val maxY  = math.max(l1.y, l2.y)
  val minY  = math.min(l1.y, l2.y)
  val maxZ  = math.max(l1.z, l2.z)
  val minZ  = math.min(l1.z, l2.z)

  val maxXd  = math.max(l1.xd, l2.xd)
  val minXd  = math.min(l1.xd, l2.xd)
  val maxYd  = math.max(l1.yd, l2.yd)
  val minYd  = math.min(l1.yd, l2.yd)
  val maxZd  = math.max(l1.zd, l2.zd)
  val minZd  = math.min(l1.zd, l2.zd)

  implicit val world = l1.world

  override def toString = s"Cube(l1: ${(maxX,maxY,maxZ)}, l2: ${(minX,minY,minZ)})"
  override def equals(a:Any) = a match {
    case c@Cube(_,_) =>
      (maxX,maxY,maxZ) == (c.maxX,c.maxY,c.maxZ) &&
        (minX,minY,minZ) == (c.minX,c.minY,c.minZ) &&
        c.world.name == world.name
    case _ => false
  }
  def copy(minX:Int=minX, minY:Int=minY, minZ:Int=minZ,
           maxX:Int=maxX, maxY:Int=maxY, maxZ:Int=maxZ): Cube =
    Cube(new Location(world, minX, minY, minZ), new Location(world, maxX, maxY, maxZ))

  // this must be a def to avoid it memoizing.
  def blocks = world.between(l1, l2)
  def width  = maxX - minX
  def height = maxY - minY
  def depth  = maxZ - minZ
  def size: BigInt = BigInt(width) * BigInt(height) * BigInt(depth)

  def contains(p: Player)  : Boolean = this.contains(p.loc)
  def contains(l: Location): Boolean = (
    l.xd <= maxX and l.xd >= minX and
      l.yd <= maxY and l.yd >= minY and
      l.zd <= maxZ and l.zd >= minZ
    )

  /**
   * The 8 corners of this Cube.
   */
  def corners: List[Block] = List(
    world(maxX, minY, maxZ),
    world(maxX, minY, minZ),
    world(minX, minY, maxZ),
    world(minX, minY, minZ),
    world(maxX, maxY, maxZ),
    world(maxX, maxY, minZ),
    world(minX, maxY, maxZ),
    world(minX, maxY, minZ)
  )
  
  /**
   * A Stream containing the floor, ceiling and walls of this cube.
   */
  def shell: Stream[Block] = (floor.blocks #::: ceiling.blocks #::: walls).distinct

  /**
   * get the floor of this cube
   * @return a new Cube
   */
  def floor  = Cube(world(maxX, minY, maxZ), world(minX, minY, minZ))
  def bottom = floor _

  /**
   * Returns true if the given block is on the floor of this cube
   * @param b
   * @return
   */
  def onFloor(b: Block)   = b.y == minY
  def onBottom = onFloor _

  /**
   * get the ceiling of this cube
   * @return a new Cube
   */
  def ceiling = Cube(world(maxX, maxY, maxZ), world(minX, maxY, minZ))
  def top     = ceiling _

  /**
   * Returns true if the given block is on the ceiling of this cube
   * @param b
   * @return
   */
  def onCeiling(b: Block) = b.y == maxY
  def onTop = onCeiling _

  def northWall: Cube = Cube(world(minX, minY, minZ), world(maxX, maxY, minZ))
  def southWall: Cube = Cube(world(minX, minY, maxZ), world(maxX, maxY, maxZ))
  def eastWall : Cube = Cube(world(maxX, minY, minZ), world(maxX, maxY, maxZ))
  def westWall : Cube = Cube(world(minX, minY, minZ), world(minX, maxY, maxZ))

  /**
   * Return a Stream of all the blocks in this cube
   * the order of the blocks in the stream is not particularly meaningful
   * and shouldnt be relied on.
   * TODO: can i do this more efficiently?
   * TODO: can i make this return 4 cubes?
   */
  def walls: Stream[Block] = blocks.filter(onWall)

  def onWall(b: Block)     = b.x == l1.x or b.x == l2.x or b.z == l1.z or b.z == l2.z

  /**
   * Shrink this cube on all sides by one, giving just the insides of the cube
   * You can also think about this as this cube without its ceiling, floor, and 4 walls
   * TODO: dont shrink if the Cube is too small.
   * @return A new cube
   */
  def insides = Cube(world(maxX-1, maxY-1, maxZ-1), world(minX+1, minY+1, minZ+1))

  /**
   * Set all the blocks in this Cube to AIR
   */
  def eraseAll: Unit = blocks.foreach(_.erase)

  /**
   * Set all the the blocks in this cube to the new type.
   * @param newM
   */
  def setAll(newM: Material)       : Unit = setAll(new MaterialAndData(newM, None))
  def setAll(newM: MaterialAndData): Unit = for(b <- blocks) newM.update(b)

  /**
   * Change all of the blocks in this cube that are of the old material type
   * to the new material type.
   * @param oldM
   * @param newM
   */
  def changeAll(oldM: Material, newM: Material): Unit =
    changeAll(oldM, new MaterialAndData(newM, None))
  def changeAll(oldM: Material, newM: MaterialAndData): Unit =
    for(b <- blocks; if b is oldM) newM.update(b)

  /**
   * A whole pile of operations to change the size of this Cube
   */

  def shrink(xLess:Int, yLess:Int, zLess:Int)  = {
    def midpoint(max:Int, min:Int): Int = (max-min) / 2
    def newMaxMin(max:Int, min:Int, less:Int): (Int,Int) =
      if (max - min <= 1) (max,min)
      else if (max - min <= (less*2)) (midpoint(max, min),midpoint(max, min))
      else (max - less, min + less)
    val (newMaxX,newMinX) = newMaxMin(maxX, minX,xLess)
    val (newMaxY,newMinY) = newMaxMin(maxY, minY,yLess)
    val (newMaxZ,newMinZ) = newMaxMin(maxZ, minZ,zLess)
    Cube(world(newMaxX, newMaxY, newMaxZ), world(newMinX, newMinY, newMinZ))
  }

  def grow(xMore:Int,yMore:Int,zMore:Int)  = {
    val (newMaxX,newMinX) = (maxX + xMore, minX - xMore)
    val (newMaxY,newMinY) = (maxY + yMore, minY - yMore)
    val (newMaxZ,newMinZ) = (maxZ + zMore, minZ - zMore)
    Cube(world(newMaxX, newMaxY, newMaxZ), world(newMinX, newMinY, newMinZ))
  }

  /**
   * All grow operations make the cube bigger
   * Min (X,Y or Z) becomes min (X,Y or Z) - extra
   * Max (X,Y or Z) becomes min (X,Y or Z) + extra
   * @return a new Cube
   **/

  def growMinXBy  (extra:Int) = copy(minX=minX-extra)
  def growMinYBy  (extra:Int) = copy(minY=minY-extra)
  def growMinZBy  (extra:Int) = copy(minZ=minZ-extra)
  def growMaxXBy  (extra:Int) = copy(maxX=maxX+extra)
  def growMaxYBy  (extra:Int) = copy(maxY=maxY+extra)
  def growUp      (extra:Int) = growMaxYBy(extra)
  def growDown    (extra:Int) = growMinYBy(extra)
  def growMaxZBy  (extra:Int) = copy(maxZ=maxZ+extra)

  /**
   * All shrink operations make the cube smaller
   * TODO: But, you can't shrink smaller than a 1x1x1 cube.
   * Min (X,Y or Z) becomes min (X,Y or Z) - extra
   * Max (X,Y or Z) becomes min (X,Y or Z) + extra
   * @return a new Cube
   **/

  def shrinkMinXBy(less:Int)  = copy(minX=minX+less)
  def shrinkMinYBy(less:Int)  = copy(minY=minY+less)
  def shrinkMinZBy(less:Int)  = copy(minZ=minZ+less)
  def shrinkMaxXBy(less:Int)  = copy(maxX=maxX-less)
  def shrinkMaxYBy(less:Int)  = copy(maxY=maxY-less)
  def shrinkMaxZBy(less:Int)  = copy(maxZ=maxZ-less)
  // move x and z in by n
  def shrinkXZ(n:Int) = shrink(n,0,n)

  def expandX     (i:Int)     = grow(i,0,0)
  def expandY     (i:Int)     = grow(0,i,0)
  def expandZ     (i:Int)     = grow(0,0,i)

  /**
   * expand by n blocks in all directions  (N,S,E,W,Up,Down)
   */
  def expand(n:Int)    = grow(n,n,n)

  /**
   * expand by n blocks upwards only.
   */
  def expandUp(n:Int)  = growMaxYBy(n)

  /**
   * expand by n blocks N,S,E,W
   */
  def expandXZ(n:Int) = grow(n,0,n)


  /**
   * A whole pile of operations to change the position of this Cube
   */

  def shiftX      (i:Int)     = growMaxXBy(i).shrinkMinXBy(i)
  def shiftY      (i:Int)     = growMaxYBy(i).shrinkMinYBy(i)
  def shiftZ      (i:Int)     = growMaxZBy(i).shrinkMinZBy(i)
  def shiftNorth  (i:Int)     = shiftZ(-i)
  def shiftSouth  (i:Int)     = shiftZ( i)
  def shiftEast   (i:Int)     = shiftX( i)
  def shiftWest   (i:Int)     = shiftZ(-i)
  def shiftUp     (i:Int)     = shiftY( i)
  def shiftDown   (i:Int)     = shiftY(-i)

  /**
   * get all the players inside this cube at the time of this call.
   */
  def players: Iterator[Player] = world.getPlayers.iterator.filter(contains)

  /**
   * this is pretty close to map, on a Cube...
   * @param newL1
   */
  def paste(newL1: Location): Unit = {
    def translate(b: Block): Block =
      world(
        b.xd + (newL1.xd - b.xd) + (b.xd - minXd),
        b.yd + (newL1.yd - b.yd) + (b.yd - minYd),
        b.zd + (newL1.zd - b.zd) + (b.zd - minZd))
    blocks.foreach { b => translate(b) update b.materialAndData }
  }
}
