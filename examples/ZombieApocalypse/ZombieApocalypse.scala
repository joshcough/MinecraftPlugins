package jcdc.pluginfactory.examples

import org.bukkit.entity.EntityType.ZOMBIE
import jcdc.pluginfactory.Listeners
import Listeners._

class ZombieApocalypse extends ListeningFor(OnPlayerDeath { (p, _) => p.loc spawn ZOMBIE })
