package jcdc.pluginfactory.examples

import jcdc.pluginfactory.Listeners._
import jcdc.pluginfactory.Listeners.ListeningFor
import org.bukkit.Material.DIAMOND_AXE

class Thor extends ListeningFor(OnEntityDamageByPlayer { (damagee, thor, _) =>
  if (thor isHoldingA DIAMOND_AXE) damagee.shock
})