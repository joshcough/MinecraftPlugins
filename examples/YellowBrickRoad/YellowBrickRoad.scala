package jcdc.pluginfactory.examples

import org.bukkit.Material._
import jcdc.pluginfactory.Listeners
import Listeners._

class YellowBrickRoad extends ListeningFor(OnPlayerMove((p, e) =>
  if (p.blockOn isNot AIR) p.blockOn changeTo GOLD_BLOCK
))
