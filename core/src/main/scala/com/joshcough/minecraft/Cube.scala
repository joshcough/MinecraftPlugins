package com.joshcough.minecraft

object Point {
  def apply(xyz: (Int, Int, Int)): Point = new Point(xyz._1, xyz._2, xyz._3)
}

case class Point(x: Int, y: Int, z: Int){
  def adjust(x: Int = x, y: Int = y, z: Int = z): Point = Point(x, y, z)
}

object Cube {
  val origin   = Point(0, 0, 0)
  val maxPoint = Point(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)
  val minPoint = Point(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE)

  // applicative functor stuff.
  def pure[T](t: T): Cube[T] = Cube(maxPoint, minPoint)(Function.const(t))
  def ap[T,U](fs: Cube[T => U], c: Cube[T]): Cube[U] = Cube(
    Point(math.min(fs.maxX, c.maxX), math.min(fs.maxY, c.maxY), math.min(fs.maxZ, c.maxZ)),
    Point(math.max(fs.minX, c.minX), math.max(fs.minY, c.minY), math.max(fs.minZ, c.minZ))
  )(coor => fs(coor)(c(coor)))
}

import Cube._

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
case class Cube[T](corner1: Point, corner2: Point)(f: Point => T) { self =>

  lazy val maxX  = math.max(corner1.x, corner2.x)
  lazy val minX  = math.min(corner1.x, corner2.x)
  lazy val maxY  = math.max(corner1.y, corner2.y)
  lazy val minY  = math.min(corner1.y, corner2.y)
  lazy val maxZ  = math.max(corner1.z, corner2.z)
  lazy val minZ  = math.min(corner1.z, corner2.z)

  def apply(c: Point): T = f(c)

  def map[U](g: T => U): Cube[U] = Cube(corner1, corner1)(g compose f)
  def mapCoor[U](f: Point => U): Cube[U] = Cube(corner1, corner2)(f)

  override def toString = s"Cube(l1: ${(maxX,maxY,maxZ)}, l2: ${(minX,minY,minZ)})"
  override def equals(a:Any) = a match {
    case c:Cube[_] =>
      (maxX,maxY,maxZ) == (c.maxX,c.maxY,c.maxZ) && (minX,minY,minZ) == (c.minX,c.minY,c.minZ)
    case _ => false
  }

  def copy(minX:Int=minX, minY:Int=minY, minZ:Int=minZ,
           maxX:Int=maxX, maxY:Int=maxY, maxZ:Int=maxZ): Cube[T] =
    Cube(Point(minX, minY, minZ), Point(maxX, maxY, maxZ))(f)

  // this must be a def to avoid it memoizing.
  def toCoorStream: Stream[Point] = {
    for {
      x <- (minX to maxX).toStream
      y <- (minY to maxY).toStream
      z <- (minZ to maxZ).toStream
    } yield Point(x,y,z)
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
  def toZippedStream: Stream[(Point, T)] = toCoorStream zip toStream

  def width : Long = maxX.toLong - minX.toLong + 1L
  def height: Long = maxY.toLong - minY.toLong + 1L
  def depth : Long = maxZ.toLong - minZ.toLong + 1L
  def size  : BigInt = BigInt(width) * BigInt(height) * BigInt(depth)

  /**
   *
   * @param c
   * @return
   */
  def contains(c: Point): Boolean = (
    c.x <= maxX && c.x >= minX &&
    c.y <= maxY && c.y >= minY &&
    c.z <= maxZ && c.z >= minZ
  )

  /**
   * The 8 corners of this Cube.
   */
  def cornersCoors: List[Point] = List(
    Point(maxX, minY, maxZ),
    Point(maxX, minY, minZ),
    Point(minX, minY, maxZ),
    Point(minX, minY, minZ),
    Point(maxX, maxY, maxZ),
    Point(maxX, maxY, minZ),
    Point(minX, maxY, maxZ),
    Point(minX, maxY, minZ)
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
  def floor  = Cube(Point(maxX, minY, maxZ), Point(minX, minY, minZ))(f)
  def bottom = floor _

  /**
   * Returns true if the given coordinate is on the floor of this cube
   * @param c
   * @return
   */
  def onFloor(c: Point)   = c.y == minY
  def onBottom = onFloor _

  /**
   * get the ceiling of this cube
   * @return a new Cube
   */
  def ceiling = Cube(Point(maxX, maxY, maxZ), Point(minX, maxY, minZ))(f)
  def top     = ceiling _

  /**
   * Returns true if the given block is on the ceiling of this cube
   * @param c
   * @return
   */
  def onCeiling(c: Point) = c.y == maxY
  def onTop = onCeiling _

  def northWall: Cube[T] = Cube(Point(minX, minY, minZ), Point(maxX, maxY, minZ))(f)
  def southWall: Cube[T] = Cube(Point(minX, minY, maxZ), Point(maxX, maxY, maxZ))(f)
  def eastWall : Cube[T] = Cube(Point(maxX, minY, minZ), Point(maxX, maxY, maxZ))(f)
  def westWall : Cube[T] = Cube(Point(minX, minY, minZ), Point(minX, maxY, maxZ))(f)

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
  def onWall(c: Point) = c.x == corner1.x || c.x == corner2.x || c.z == corner1.z || c.z == corner2.z

  /**
   * Shrink this cube on all sides by one, giving just the insides of the cube
   * You can also think about this as this cube without its ceiling, floor, and 4 walls
   * TODO: dont shrink if the Cube is too small.
   * @return A new cube
   */
  def insides = Cube(Point(maxX-1, maxY-1, maxZ-1), Point(minX+1, minY+1, minZ+1))(f)

  /**
   * A whole pile of operations to change the size of this Cube
   */

  /**
   * Conceptually, shrinking a cube by 1 means 'shrink the cube on ALL sides by 1'
   * Shrinking a 5x5x5 cube by 1 gives you back a 3x3x3 cube.
   * Shrinking a 3x3x3 cube by 1 gives you back a 1x1x1 cube.
   * A 1x1x1 cube can't shrink any further.
   *
   * A 2x2x2 cube will not shrink! It would be arbitrary to choose
   * whether the shrink the max, or grow the min, and I've decided
   * to just not shrink it at all in that case.
   * You can still do this with the other shrink and grow functions on Cube though.
   *
   * Shinking a cube by 1 means passing xLess=1, yLess=1, xLess=1
   *
   * Hopefully obviously, this means you can just shrink x, or y, or z
   * by passing in 0 for the other values.
   *
   * Shrinking a 5x5x5 cube with xLess=1 will give back a 3x5x5 cube.
   *
   * I don't recommend passing in negative values to try to grow.
   * That isn't tested.
   *
   * @param xLess
   * @param yLess
   * @param zLess
   * @return
   */
  def shrink(xLess:Int, yLess:Int, zLess:Int) = {
    @scala.annotation.tailrec
    def newMaxMin(max:Int, min:Int, less:Int): (Int,Int) =
      if(max - min <= 1) (max,min)
      else if(less == 0) (max,min)
      else newMaxMin(max-1, min+1, less-1)
    val (newMaxX,newMinX) = newMaxMin(maxX, minX, xLess)
    val (newMaxY,newMinY) = newMaxMin(maxY, minY, yLess)
    val (newMaxZ,newMinZ) = newMaxMin(maxZ, minZ, zLess)
    Cube(Point(newMaxX, newMaxY, newMaxZ), Point(newMinX, newMinY, newMinZ))(f)
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
    Cube(Point(newMaxX, newMaxY, newMaxZ), Point(newMinX, newMinY, newMinZ))(f)
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

  // TODO: these have to be more than just mapCoor...
  // TODO: they have to change corner1 and corner2!
  def shiftX      (i:Int) = mapCoor(c => f(c.adjust(x = c.x + i)))
  def shiftY      (i:Int) = mapCoor(c => f(c.adjust(y = c.y + i)))
  def shiftZ      (i:Int) = mapCoor(c => f(c.adjust(z = c.z + i)))
  def shiftNorth  (i:Int) = shiftZ(-i)
  def shiftSouth  (i:Int) = shiftZ( i)
  def shiftEast   (i:Int) = shiftX( i)
  def shiftWest   (i:Int) = shiftZ(-i)
  def shiftUp     (i:Int) = shiftY( i)
  def shiftDown   (i:Int) = shiftY(-i)

  /**
   * Translate this cube to the new position.
   * Shifts this cubes first corner to the newC1, and
   * shifts all other blocks by the same offsets.
   * @param newC1
   * @return
   */
  def translateTo(newC1: Point): Cube[T] = {
    val xDiff = newC1.x - corner1.x
    val yDiff = newC1.y - corner1.y
    val zDiff = newC1.z - corner1.z
    val newC2 = Point(corner2.x + xDiff, corner2.y + yDiff, corner2.z + zDiff)
    def translateBack(b: Point): Point = Point(b.x - xDiff, b.y - yDiff, b.z - zDiff)
    Cube(newC1, newC2){ c => f(translateBack(c)) }
  }

  /**
   * mirror X algo: minX + (maxX - x)
   * 9 -> 1     minX + (maxX - x) = 0 + (10 - 9) = 0 + 1 = 1
   * 8 -> 2     minX + (maxX - x) = 0 + (10 - 8) = 0 + 2 = 2
   * 5 -> 5     minX + (maxX - x) = 0 + (10 - 5) = 0 + 5 = 5
   * 4 -> 6     minX + (maxX - x) = 0 + (10 - 4) = 0 + 6 = 6
   * 0 -> 10    minX + (maxX - x) = 0 + (10 - 0) = 0 + 10 = 10
   */
  def mirrorX: Cube[T] = mapCoor(c => f(Point(minX + maxX - c.x, c.y, c.z)))
  def mirrorY: Cube[T] = mapCoor(c => f(Point(c.x, minY + maxY - c.y, c.z)))
  def mirrorZ: Cube[T] = mapCoor(c => f(Point(c.x, c.y, minZ + maxZ - c.z)))
}
