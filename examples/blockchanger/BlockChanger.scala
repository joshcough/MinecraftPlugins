package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{CommandPlugin, ListenerPlugin}
import org.bukkit.entity.Player
import org.bukkit.Material

/**
 * A more in depth version of BlockChangerGold, that has a command
 * that allows the user to change the type of block they use when
 * they punch blocks, and also allows for block changing to be disabled.
 */
class BlockChanger extends ListenerPlugin with CommandPlugin {
  // a map that stores users material preference.
  val users    = collection.mutable.Map[Player, Material]()
  val listener = OnLeftClickBlock{ (p, e) =>
  /**
   * a block has been punched. if the user has a block preference,
   * change the punched block to that material. if they dont, do nothing.
   * (they either haven't enabled bc, or they have explicitly disabled it)
   */
    for(m <- users get p){ e.block changeTo m; e.cancel }
  }

  /**
   * This code specifies that there is one command in the plugin, called bc.
   * Users can enter
   *   /bc some-material
   *   /bc
   *
   * The first updates their material preference so that if they bunch a block
   * the block changes to that material.
   *
   * The second turns block changer off, so that if they punch a block
   * it doesn't change.
   */
  val command  = Command(
    name = "bc",
    desc = "Specify which material to change blocks to, or just /bc to turn off",
    // this command works with either a material argument, or no arguments.
    args = material or eof)(
    body = {
      // if they have typed in a material argument, save it in the map
      case (p, Left(m)) => users += (p -> m); p ! s"bc using: $m"
      // if they type no arguments, turn block changer off (remove them from the map).
      case (p, _)       => users -= p;        p ! "bc has been disabled"
    }
  )
}