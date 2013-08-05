package com.joshcough.minecraft.examples

import com.joshcough.minecraft.Listeners._
import com.joshcough.minecraft.Listeners.ListeningFor
import org.bukkit.Material.DIAMOND_AXE

class Thor extends ListeningFor(OnEntityDamageByPlayer { (damagee, thor, _) =>
  if (thor isHoldingA DIAMOND_AXE) damagee.shock
})