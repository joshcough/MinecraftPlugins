package jcdc.pluginfactory

object Coor {
  val origin = Coor(0, 0, 0)
  def apply(x: Int, y: Int, z: Int) = new Coor(x.toDouble, y.toDouble, z.toDouble)
  def apply(xyz: (Int, Int, Int))   = new Coor(xyz._1, xyz._2, xyz._3)
}

case class Coor(xd: Double, yd: Double, zd: Double) {
  override def toString = xyz.toString
  val (x, y, z) = (xd.toInt, yd.toInt, zd.toInt)
  val xyz  = (x, y, z)
  val xyzd = (xd, yd, zd)
}

object Cube {
  def apply[T](c1: Coor, c2: Coor)(fn: Coor => T) = new Cube[T]{
    val corner1 = c1; val corner2 = c2; val f = fn
  }
  def coors(c1:(Int, Int, Int), c2:(Int, Int, Int)) = Cube(Coor(c1), Coor(c2))(identity)
// TODO: can applicative be implemented?, yes, but like ZipList, not like below.
//  def pure[T](t: T): Cube[T] = Cube(Coor.origin, Coor.origin)(Function.const(t))
//  def ap[T,U](fs: List[T => U], ts: List[T]): List[U] = fs.flatMap(f => ts map f)
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
 */
trait Cube[T] { self =>
  // @param corner1 one corner of the cube
  // @param corner2 the other corner
  val corner1: Coor
  val corner2: Coor
  val f: Coor => T

  lazy val maxX  = math.max(corner1.x, corner2.x)
  lazy val minX  = math.min(corner1.x, corner2.x)
  lazy val maxY  = math.max(corner1.y, corner2.y)
  lazy val minY  = math.min(corner1.y, corner2.y)
  lazy val maxZ  = math.max(corner1.z, corner2.z)
  lazy val minZ  = math.min(corner1.z, corner2.z)

  lazy val maxXd  = math.max(corner1.xd, corner2.xd)
  lazy val minXd  = math.min(corner1.xd, corner2.xd)
  lazy val maxYd  = math.max(corner1.yd, corner2.yd)
  lazy val minYd  = math.min(corner1.yd, corner2.yd)
  lazy val maxZd  = math.max(corner1.zd, corner2.zd)
  lazy val minZd  = math.min(corner1.zd, corner2.zd)

  def map[U](g: T => U): Cube[U] = Cube(corner1, corner1)(g compose f)
  def mapCoor(f: Coor => T): Cube[T] = Cube(corner1, corner2)(f)

  override def toString = s"Cube(l1: ${(maxX,maxY,maxZ)}, l2: ${(minX,minY,minZ)})"
  override def equals(a:Any) = a match {
    case c:Cube[_] =>
      (maxX,maxY,maxZ) == (c.maxX,c.maxY,c.maxZ) && (minX,minY,minZ) == (c.minX,c.minY,c.minZ)
    case _ => false
  }

  def copy(minX:Int=minX, minY:Int=minY, minZ:Int=minZ,
           maxX:Int=maxX, maxY:Int=maxY, maxZ:Int=maxZ): Cube[T] =
    Cube(Coor(minX, minY, minZ), Coor(maxX, maxY, maxZ))(f)

  // this must be a def to avoid it memoizing.
  def toCoorStream: Stream[Coor] = {
    for {
      x <- (minX to maxX).toStream
      y <- (minY to maxY).toStream
      z <- (minZ to maxZ).toStream
    } yield Coor(x,y,z)
  }

  /**
   *
   * @return
   */
  def toStream: Stream[T] = toCoorStream map f

  /**
   *
   * @return
   */
  def toZippedStream: Stream[(Coor, T)] = toCoorStream zip toStream

  def width  = maxX - minX
  def height = maxY - minY
  def depth  = maxZ - minZ
  def size: BigInt = BigInt(width) * BigInt(height) * BigInt(depth)

  /**
   *
   * @param c
   * @return
   */
  def contains(c: Coor): Boolean = (
    c.xd <= maxX && c.xd >= minX &&
    c.yd <= maxY && c.yd >= minY &&
    c.zd <= maxZ && c.zd >= minZ
  )

  /**
   * The 8 corners of this Cube.
   */
  def cornersCoors: List[Coor] = List(
    Coor(maxX, minY, maxZ),
    Coor(maxX, minY, minZ),
    Coor(minX, minY, maxZ),
    Coor(minX, minY, minZ),
    Coor(maxX, maxY, maxZ),
    Coor(maxX, maxY, minZ),
    Coor(minX, maxY, maxZ),
    Coor(minX, maxY, minZ)
  )

  /**
   *
   * @return
   */
  def corners: List[T] = cornersCoors map f
  
  /**
   * A Stream containing the floor, ceiling and walls of this cube.
   * TODO: This distinct is bad here because of T.
   * TODO: i might just have to get the right blocks myself.
   * TODO: I think it just involves shrinking Y by 1 on all the walls.
   */
  def shell: Stream[T] = (floor.toStream #::: ceiling.toStream #::: walls).distinct

  /**
   * get the floor of this cube
   * @return a new Cube
   */
  def floor  = Cube(Coor(maxX, minY, maxZ), Coor(minX, minY, minZ))(f)
  def bottom = floor _

  /**
   * Returns true if the given coordinate is on the floor of this cube
   * @param c
   * @return
   */
  def onFloor(c: Coor)   = c.y == minY
  def onBottom = onFloor _

  /**
   * get the ceiling of this cube
   * @return a new Cube
   */
  def ceiling = Cube(Coor(maxX, maxY, maxZ), Coor(minX, maxY, minZ))(f)
  def top     = ceiling _

  /**
   * Returns true if the given block is on the ceiling of this cube
   * @param c
   * @return
   */
  def onCeiling(c: Coor) = c.y == maxY
  def onTop = onCeiling _

  def northWall: Cube[T] = Cube(Coor(minX, minY, minZ), Coor(maxX, maxY, minZ))(f)
  def southWall: Cube[T] = Cube(Coor(minX, minY, maxZ), Coor(maxX, maxY, maxZ))(f)
  def eastWall : Cube[T] = Cube(Coor(maxX, minY, minZ), Coor(maxX, maxY, maxZ))(f)
  def westWall : Cube[T] = Cube(Coor(minX, minY, minZ), Coor(minX, maxY, maxZ))(f)

  /**
   * Return a Stream of all the blocks in this cube
   * the order of the blocks in the stream is not particularly meaningful
   * and shouldnt be relied on.
   * TODO: can i do this more efficiently?
   * TODO: can i make this return 4 cubes?
   */
  def walls: Stream[T] = toZippedStream.filter(t => onWall(t._1)).map(_._2)

  /**
   *
   * @param c
   * @return
   */
  def onWall(c: Coor) = 
    c.x == corner1.x || c.x == corner2.x || 
    c.z == corner1.z || c.z == corner2.z

  /**
   * Shrink this cube on all sides by one, giving just the insides of the cube
   * You can also think about this as this cube without its ceiling, floor, and 4 walls
   * TODO: dont shrink if the Cube is too small.
   * @return A new cube
   */
  def insides = Cube(Coor(maxX-1, maxY-1, maxZ-1), Coor(minX+1, minY+1, minZ+1))(f)

  /**
   * A whole pile of operations to change the size of this Cube
   */

  def shrink(xLess:Int, yLess:Int, zLess:Int) = {
    def midpoint(max:Int, min:Int): Int = (max-min) / 2
    def newMaxMin(max:Int, min:Int, less:Int): (Int,Int) =
      if (max - min <= 1) (max,min)
      else if (max - min <= (less*2)) (midpoint(max, min),midpoint(max, min))
      else (max - less, min + less)
    val (newMaxX,newMinX) = newMaxMin(maxX, minX, xLess)
    val (newMaxY,newMinY) = newMaxMin(maxY, minY, yLess)
    val (newMaxZ,newMinZ) = newMaxMin(maxZ, minZ, zLess)
    Cube(Coor(newMaxX, newMaxY, newMaxZ), Coor(newMinX, newMinY, newMinZ))(f)
  }

  /**
   *
   * @param xMore
   * @param yMore
   * @param zMore
   * @return
   */
  def grow(xMore:Int,yMore:Int,zMore:Int)  = {
    val (newMaxX,newMinX) = (maxX + xMore, minX - xMore)
    val (newMaxY,newMinY) = (maxY + yMore, minY - yMore)
    val (newMaxZ,newMinZ) = (maxZ + zMore, minZ - zMore)
    Cube(Coor(newMaxX, newMaxY, newMaxZ), Coor(newMinX, newMinY, newMinZ))(f)
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
  def expand(n:Int)   = grow(n,n,n)

  /**
   * expand by n blocks upwards only.
   */
  def expandUp(n:Int) = growMaxYBy(n)

  /**
   * expand by n blocks N,S,E,W
   */
  def expandXZ(n:Int) = grow(n,0,n)

  /**
   * A whole pile of operations to change the position of this Cube
   */

  def shiftX      (i:Int) = growMaxXBy(i).shrinkMinXBy(i)
  def shiftY      (i:Int) = growMaxYBy(i).shrinkMinYBy(i)
  def shiftZ      (i:Int) = growMaxZBy(i).shrinkMinZBy(i)
  def shiftNorth  (i:Int) = shiftZ(-i)
  def shiftSouth  (i:Int) = shiftZ( i)
  def shiftEast   (i:Int) = shiftX( i)
  def shiftWest   (i:Int) = shiftZ(-i)
  def shiftUp     (i:Int) = shiftY( i)
  def shiftDown   (i:Int) = shiftY(-i)

  /**
   * This seems the same as zip...
   * @param newC1
   * @return
   */
  def paste(newC1: Coor): Cube[(T, T)] = {
    val xDiff = newC1.x - corner1.x
    val yDiff = newC1.y - corner1.y
    val zDiff = newC1.z - corner1.z
    val newC2 = Coor(corner2.x + xDiff, corner2.y + yDiff, corner2.z + zDiff)
    def translateBack(b: Coor): Coor = Coor(b.x - xDiff, b.y - yDiff, b.z - zDiff)
    Cube(newC1, newC2){ c => (f(translateBack(c)), f(c)) }
  }

//  /**
//   * mirror X algo: minX + (maxX - x)
//   * 9 -> 1     minX + (maxX - x) = 0 + (10 - 9) = 0 + 1 = 1
//   * 8 -> 2     minX + (maxX - x) = 0 + (10 - 8) = 0 + 2 = 2
//   * 5 -> 5     minX + (maxX - x) = 0 + (10 - 5) = 0 + 5 = 5
//   * 4 -> 6     minX + (maxX - x) = 0 + (10 - 4) = 0 + 6 = 6
//   * 0 -> 10    minX + (maxX - x) = 0 + (10 - 0) = 0 + 10 = 10
//   */
//  def mirrorX: Cube[T] = mapCoor(c => f(Coor(minX + (maxX - c.x), c.y, c.z)))
//  def mirrorY: Cube[T] = mapCoor(c => f(Coor(c.x, minY + (maxY - c.y), c.z)))
//  def mirrorZ: Cube[T] = mapCoor(c => f(Coor(c.x, c.y, minZ + (maxZ - c.z))))
}
