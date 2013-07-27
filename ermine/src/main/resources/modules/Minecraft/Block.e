module Minecraft.Block where

import Function
import List
import Minecraft.Helpers
import Minecraft.Native
import Primitive

translate b fx fy fz = getBlock $ location (wfb b) (fx (blockX b)) (fy (blockY b)) (fz (blockZ b))
translateX b fx = translate b fx id id
translateY b fy = translate b id fy id
translateZ b fz = translate b id id fz
blockAbove b = translateY b (y -> y + 1)
blockBelow b = translateY b (y -> y - 1)
blockNorth b = translateZ b (z -> z - 1)
blockSouth b = translateZ b (z -> z + 1)
blockEast  b = translateX b (x -> x + 1)
blockWest  b = translateX b (x -> x - 1)
blockNorthEast = blockNorth . blockEast
blockSouthEast = blockSouth . blockEast
blockNorthWest = blockNorth . blockWest
blockSouthWest = blockSouth . blockWest
blocksAbove b = blockAbove b :: blocksAbove (blockAbove b)
andBlocksAbove b = b :: blocksAbove b
blocksBelow b = blockBelow b :: blocksBelow (blockBelow b)
andBlocksBelow b = b :: blocksBelow b

private
  wfb = getWorldFromBlock

{--
    lazy val (xd, yd, zd) = (b.getX.toDouble, b.getY.toDouble, b.getZ.toDouble)
    lazy val chunk        = world.getChunkAt(b)

    // the nth block above b
    def nthBlockAbove(n:Int) = world(xd, yd + n, zd)
    // the nth block below b
    def nthBlockBelow(n:Int) = world(xd, yd - n, zd)
    // b, and all the blocks above b
    def andBlocksAbove: Stream[Block] = b #:: blocksAbove
    // the four blocks north, south, east and west of b
    def neighbors4: Stream[Block] =
      blockNorth #:: blockSouth #:: blockEast #:: blockWest #:: Stream.empty
    // b, and the four blocks north, south, east and west of b
    def andNeighbors4: Stream[Block] = b #:: neighbors4
    // the four blocks north, south, east and west of b
    // and the four blocks northeast, southeast, northwest, and southwest of b
    def neighbors8   : Stream[Block] = neighbors4 ++ (
      blockNorthEast #:: blockSouthEast #:: blockNorthWest #:: blockSouthWest #:: Stream.empty
    )
    // b and all of b's neighbors8
    def andNeighbors8: Stream[Block] = b #:: neighbors8

    /**
     * @return all of b's 26 neighbors in 3D space
     */
    def neighbors    : Stream[Block] =
      neighbors8 ++ (b.blockBelow.andNeighbors8) #::: (b.blockAbove.andNeighbors8)

    /**
     * @return b, and all of b's 26 neighbors in 3D space
     */
    def andNeighbors : Stream[Block] = b #:: neighbors

    def is(m:Material)    = b.getType == m
    def isA(m:Material)   = b.getType == m
    def isNot(m:Material) = b.getType != m

    /**
     * drop the item for the current material of this block, and then set this block to AIR
     */
    def erase: Boolean = if(! (b is AIR)) {
      b.world.dropItem  (b.loc, b.itemStack)
      b.world.playEffect(b.loc, SMOKE, 1)
      changeTo(AIR)
    } else false

    /**
     * Change this block to the given material.
     */
    def changeTo(m: Material): Boolean = {
      try if(! chunk.isLoaded) chunk.load
      catch { case e: Exception => println("unable to load chunk.") }
      MaterialAndData(m, None) update b
    }

    def itemStack = new ItemStack(b.getType, 1, b.getData)
    def materialAndData = MaterialAndData(b.getType, Some(b.getData))

    def point: Point = Point(b.x, b.y, b.z)

    /**
     * Returns a Cube of all of the blocks between two locations of the world.
     */
    def cubeTo(b2: Block): Cube[Block] = b.loc.cubeTo(b2.loc)
  }
--}