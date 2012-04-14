package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{Listeners, ListenerPlugin, CommandsPlugin}
import Listeners._
import org.bukkit.Material
import org.bukkit.Material.{DIAMOND_AXE, LOG}
import org.bukkit.entity.EntityType.{ARROW, ZOMBIE}
import org.bukkit.entity.Player

class Thor extends ListeningFor(OnEntityDamageByPlayer { (e, p, _) =>
  if (p isHoldingA DIAMOND_AXE) p.world.strikeLightning(e.loc)
})

class ZombieApocalypse extends ListeningFor(OnPlayerDeath { (p, _) => p.loc.spawn(ZOMBIE) })

class TreeDelogger extends ListeningFor(OnBlockBreak { (b, e) =>
  if (b isA LOG) for (b <- b #:: b.blocksAbove.takeWhile(_ isA LOG)) b.erase
})

class BanArrows extends ListeningFor(OnPlayerDamageByEntity { (p, e) =>
  if (e.getDamager isAn ARROW) p.ban("struck by an arrow!")
})

class BlockChanger extends ListenerPlugin with CommandsPlugin {
  val users = collection.mutable.Map[Player, Material]()
  val listener = OnBlockDamage((b, e) => users.get(e.getPlayer).foreach(b changeTo _))
  val commands = List(
    Command("bc", "Hit blocks to change them to the block with type blockId, or /bc off to turn off.",
      args("off"||material){
        case p ~ Left(off) => users.remove(p); p ! "bc has been disabled"
        case p ~ Right(m)  => users += (p -> m); p ! ("bc using: " + m)
      }
    )
  )
}

class God extends ListenerPlugin with CommandsPlugin {
  val isAGod = collection.mutable.Map[Player, Boolean]().withDefaultValue(false)
  val listener = OnPlayerDamage { (p, e) => e cancelIf isAGod(p) }
  val commands = List(
    Command("god", "Toggle God mode.", noArgs { p =>
      isAGod.update(p, !isAGod(p))
      p ! ("god mode is now " + (if (isAGod(p)) "on" else "off"))
    })
  )
}

class LightningArrows extends ListeningFor(OnEntityDamageByEntity { e =>
  if (e.getDamager isAn ARROW) e.world.strikeLightning(e.loc)
})

class NoRain extends ListenerPlugin {
  val listener = OnWeatherChange(e => e.cancelIf(e.rain, broadcast("Put up an umbrella.")))
}

object Curses {
  import org.bukkit.event.player.PlayerChatEvent
  val curses = List("btt", "tiju", "gvdl", "cjudi").map(_.map(c => (c - 1).toChar))
  def containsSwear(event: PlayerChatEvent) = curses.filter(event.getMessage.toLowerCase.contains(_)).size > 0
  def handle(e: PlayerChatEvent, f: => Unit) = e.cancelIf(containsSwear(e), f)
}

class CurseBan extends ListeningFor(OnPlayerChat((p, e) => Curses.handle(e, e.player.ban("no swearing"))))
class CursePreventer extends ListeningFor(OnPlayerChat((p, e) => Curses.handle(e, ())))
