package jcdc.pluginfactory.examples

import org.bukkit.Material
import org.bukkit.Material._
import org.bukkit.block.Block
import org.bukkit.entity.EntityType.{ARROW, ZOMBIE}
import org.bukkit.entity.Player
import jcdc.pluginfactory._
import Listeners._
import jcdc.pluginfactory.Listeners.ListeningFor

class BlockChangerGold extends ListeningFor(OnLeftClickBlock((p, e) =>
  if (p is "joshcough") e.block changeTo GOLD_BLOCK
))

class BlockChanger extends ListenerPlugin with CommandPlugin {
  val users    = collection.mutable.Map[Player, Material]()
  val listener = OnLeftClickBlock((p, e) => users.get(p).foreach(e.block changeTo _))
  val command  = Command(
    name = "bc",
    desc = "Hit blocks to change them to the block with type blockId, or just /bc to turn it off.",
    body = args(material.?){
      case p ~ Some(m)  => users += (p -> m); p ! (s"bc using: $m")
      case p ~ None     => users -= p;        p ! "bc has been disabled"
    }
  )
}

class Thor extends ListeningFor(OnEntityDamageByPlayer { (damagee, thor, _) =>
  if (thor isHoldingA DIAMOND_AXE) damagee.shock
})

class Farmer extends ListenersPlugin {
  def dropSeedsAt(b: Block) = b.loc.dropItem(SEEDS)
  def hasHoe(p: Player) = p.isHoldingAnyOf(WOOD_HOE, STONE_HOE, IRON_HOE, GOLD_HOE, DIAMOND_HOE)
  val listeners = List(
    OnBlockBreak{ (b, e)      => if (b is LONG_GRASS) dropSeedsAt(b) },
    OnRightClickBlock{ (p, e) => if (hasHoe(p) and (e.block is GRASS)) dropSeedsAt(e.block) }
  )
}

class ZombieApocalypse extends ListeningFor(OnPlayerDeath { (p, _) => p.loc spawn ZOMBIE })

class TreeDelogger extends ListeningFor(OnBlockBreak { (b, e) =>
  if (b isA LOG) for (b <- b.andBlocksAbove.takeWhile(_ isA LOG)) b.erase
})

class BanArrows extends ListeningFor(OnPlayerDamageByEntity { (p, e) =>
  if (e.getDamager isAn ARROW) p.ban("struck by an arrow!")
})

class NoRain extends ListenerPlugin {
  val listener = OnWeatherChange(e => e.cancelIf(e.rain, broadcast("Put up an umbrella.")))
}

class YellowBrickRoad extends ListeningFor(OnPlayerMove((p, e) =>
  if (p.blockOn isNot AIR) p.blockOn changeTo GOLD_BLOCK
))

class God extends ListenerPlugin with CommandsPlugin {
  val gods = collection.mutable.Map[Player, Boolean]().withDefaultValue(false)
  implicit def playerToGod(p:Player) = new { def isAGod = gods(p) }
  val listener = OnPlayerDamage { (p, e) => e cancelIf p.isAGod }
  val commands = List(
    Command(
      name = "god",
      desc = "Toggle God mode.",
      body = noArgs { p =>
               gods.update(p, ! p.isAGod)
               p ! ("god mode is now " + (if (p.isAGod) "on" else "off"))
             }
    )
  )
}

object Curses {
  import org.bukkit.event.player.PlayerChatEvent
  val curses = List("btt", "tiju", "gvdl", "cjudi").map(_.map(c => (c - 1).toChar))
  def containsSwear(event: PlayerChatEvent) =
    curses.filter(event.getMessage.toLowerCase.contains(_)).size > 0
  def handle(e: PlayerChatEvent, f: => Unit) = e.cancelIf(containsSwear(e), f)
}

class CurseBan extends ListeningFor(OnPlayerChat((p, e) =>
  Curses.handle(e, e.getPlayer.ban("no swearing")))
)
 
class CursePreventer extends ListeningFor(OnPlayerChat((p, e) => Curses.handle(e, ())))
