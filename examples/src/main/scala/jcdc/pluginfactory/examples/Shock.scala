package jcdc.pluginfactory.examples

import jcdc.pluginfactory.CommandPlugin

class Shock extends CommandPlugin {
  val command = Command("shock", "shock a player", player){ case (you, them) =>
    them.shock
  }
}
