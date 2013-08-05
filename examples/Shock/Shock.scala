package com.joshcough.minecraft.examples

import com.joshcough.minecraft.CommandPlugin

class Shock extends CommandPlugin {
  val command = Command("shock", "shock a player", player){ case (you, them) =>
    them.shock
  }
}
