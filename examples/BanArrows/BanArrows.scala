package jcdc.pluginfactory.examples

import org.bukkit.entity.EntityType.ARROW
import jcdc.pluginfactory.Listeners
import Listeners._

class BanArrows extends ListeningFor(OnPlayerDamageByEntity { (p, e) =>
  if (e.getDamager isAn ARROW) p ban "struck by an arrow!"
})