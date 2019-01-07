package com.joshcough.minecraft.examples

import com.joshcough.minecraft.Listeners._
import org.bukkit.entity.EntityType.ARROW

class LightningArrowsPlugin extends ListeningFor(OnEntityDamageByEntity { e =>
  if (e.damager isAn ARROW) e.damagee.shock
})

object LightningArrowsCommands {
  def main(args: Array[String]): Unit = {}
}
