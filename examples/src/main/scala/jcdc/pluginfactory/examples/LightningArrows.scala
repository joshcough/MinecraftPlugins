package jcdc.pluginfactory.examples

import jcdc.pluginfactory.Listeners._
import org.bukkit.entity.EntityType.ARROW

class LightningArrows extends ListeningFor(OnEntityDamageByEntity { e =>
  if (e.damager isAn ARROW) e.damagee.shock
})