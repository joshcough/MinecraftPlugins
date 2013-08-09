package com.joshcough.minecraft.examples

import org.bukkit.Material._
import com.joshcough.minecraft.Listeners
import Listeners._

class TreeDelogger extends ListeningFor(OnBlockBreak { (b,_,_) =>
  if (b isA LOG) for (b <- b.andBlocksAbove.takeWhile(_ isA LOG)) b.erase
})
