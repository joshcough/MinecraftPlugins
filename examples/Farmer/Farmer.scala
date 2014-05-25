package com.joshcough.minecraft.examples

import org.bukkit.Material._
import org.bukkit.block.Block
import org.bukkit.entity.Player
import com.joshcough.minecraft.{ListenersPlugin, Listeners}
import Listeners._

class Farmer extends ListenersPlugin {
  def dropSeedsAt(b: Block) = b.loc.dropItem(SEEDS)
  def hasHoe(p: Player) = p.isHoldingAnyOf(WOOD_HOE, STONE_HOE, IRON_HOE, GOLD_HOE, DIAMOND_HOE)
  val listeners = List(
    OnBlockBreak{ (b,_,_)     => if (b is LONG_GRASS) dropSeedsAt(b) },
    OnRightClickBlock{ (p, e) => if (hasHoe(p) and (e.block is GRASS)) dropSeedsAt(e.block) }
  )
}
