package com.joshcough.minecraft.examples

import com.joshcough.minecraft.PluginConfig
import com.joshcough.minecraft.Listeners._
import org.bukkit.entity.EntityType.ARROW

class LightningArrowsPlugin extends ListeningFor(OnEntityDamageByEntity { e =>
  if (e.damager isAn ARROW) e.damagee.shock
})

class LightningArrowsConfig extends PluginConfig[LightningArrowsPlugin] {
  val pluginClass = classOf[LightningArrowsPlugin]
}
