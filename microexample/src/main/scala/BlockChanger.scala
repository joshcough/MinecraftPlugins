import jcdc.pluginfactory.{CommandPlugin, ListenerPlugin}
import org.bukkit.entity.Player
import org.bukkit.Material

class BlockChanger extends ListenerPlugin with CommandPlugin {
  val users    = collection.mutable.Map[Player, Material]()
  val listener = OnLeftClickBlock{ (p, e) =>
    for(m <- users.get(p)) { e.block changeTo m; e.cancel }
  }
  val command  = Command(
    name = "bc",
    desc = "Specify which material to change blocks to, or just /bc to turn off",
    args = material.?)(
    body = {
      case (p, Some(m)) => users += (p -> m); p ! (s"bc using: $m")
      case (p, None)    => users -= p;        p ! "bc has been disabled"
    }
  )
}
