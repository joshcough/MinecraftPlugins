package com.joshcough.minecraft.examples

import org.bukkit.entity.EntityType.ARROW
import com.joshcough.minecraft.Listeners
import Listeners._

class BanArrows extends ListeningFor(OnPlayerDamageByEntity { (p, e) =>
  if (e.getDamager isAn ARROW) p ban "struck by an arrow!"
})