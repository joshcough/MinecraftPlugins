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
    args = material or eof)(
    body = {
      case (p, Left(m)) => users += (p -> m); p ! (s"bc using: $m")
      case (p, _)       => users -= p;        p ! "bc has been disabled"
    }
  )
}

object YMLGenerator {
  def main(args: Array[String]): Unit =
    new BlockChanger().writeYML("Josh Cough", "0.2.2")
}
