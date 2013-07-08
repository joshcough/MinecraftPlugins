package jcdc.pluginfactory.examples

import org.bukkit.Material._
import jcdc.pluginfactory.Listeners
import Listeners._

class TreeDelogger extends ListeningFor(OnBlockBreak { (b,_,_) =>
  if (b isA LOG) for (b <- b.andBlocksAbove.takeWhile(_ isA LOG)) b.erase
})
