package com.joshcough.minecraft.examples

import org.bukkit.Material._
import com.joshcough.minecraft.Listeners
import Listeners._

class YellowBrickRoad extends ListeningFor(OnPlayerMove((p, e) =>
  if (p.blockOn isNot AIR) p.blockOn changeTo GOLD_BLOCK
))
