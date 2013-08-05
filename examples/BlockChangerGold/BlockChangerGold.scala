package com.joshcough.minecraft.examples

import com.joshcough.minecraft.Listeners._
import com.joshcough.minecraft.Listeners.ListeningFor
import org.bukkit.Material
import Material.GOLD_BLOCK

/**
 * A plugin that changes blocks to gold whenever they are punched.
 */
class BlockChangerGold extends ListeningFor(OnLeftClickBlock{(p, e) =>
  e.block changeTo GOLD_BLOCK; e.cancel
})
