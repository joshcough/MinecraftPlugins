import jcdc.pluginfactory.{CommandPlugin, ListenerPlugin}
import org.bukkit.entity.Player

class GodPlugin extends ListenerPlugin with CommandPlugin {
  val gods = collection.mutable.Map[Player, Boolean]().withDefaultValue(false)
  val listener = OnPlayerDamage { (p, e) => e cancelIf gods(p) }
  val command = Command(name = "god", desc = "Toggle God mode."){ p =>
    gods.update(p, ! gods(p))
    p ! s"god mode ${if(gods(p)) "enabled" else "disabled"}"
  }
}

object GodPlugin {
  def main(args: Array[String]): Unit = new GodPlugin().writeYML("Josh Cough", "0.2.0")
}
