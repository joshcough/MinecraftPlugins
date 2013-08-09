package com.joshcough.minecraft.examples

import org.bukkit.entity.EntityType.ZOMBIE
import com.joshcough.minecraft.Listeners
import Listeners._

class ZombieApocalypse extends ListeningFor(OnPlayerDeath { (p, _) => p.loc spawn ZOMBIE })
