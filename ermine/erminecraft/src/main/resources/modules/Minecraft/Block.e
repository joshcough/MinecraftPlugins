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
nthBlockAbove b n = translateY b (y -> y + n)
nthBlockBelow b n = translateY b (y -> y - n)

neighbors4    b = [blockNorth b, blockSouth b, blockEast b, blockWest b]
andNeighbors4 b = b :: neighbors4 b
neighbors8    b = neighbors4 b ++ [blockNorthEast b, blockSouthEast b, blockNorthWest b, blockSouthWest b]
andNeighbors8 b = b :: neighbors8 b
neighbors     b = neighbors8 b ++ (andNeighbors8 $ blockBelow b) ++ (andNeighbors8 $ blockAbove b)
andNeighbors  b = b :: neighbors b

private
  wfb = getWorldFromBlock

{--

-- Other crap in the scala stuff not ported over yet

    lazy val (xd, yd, zd) = (b.getX.toDouble, b.getY.toDouble, b.getZ.toDouble)
    lazy val chunk        = world.getChunkAt(b)

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