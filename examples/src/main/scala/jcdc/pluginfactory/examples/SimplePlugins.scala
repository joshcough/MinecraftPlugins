package jcdc.pluginfactory.examples

import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.Material._
import org.bukkit.block.Block
import org.bukkit.entity.EntityType.{ARROW, ZOMBIE}
import org.bukkit.entity.Player
import jcdc.pluginfactory.{CommandPlugin, CommandsPlugin, ListenersPlugin, ListenerPlugin, Listeners}
import Listeners._

class BanArrows extends ListeningFor(OnPlayerDamageByEntity { (p, e) =>
  if (e.getDamager isAn ARROW) p ban "struck by an arrow!"
})

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

class Farmer extends ListenersPlugin {
  def dropSeedsAt(b: Block) = b.loc.dropItem(SEEDS)
  def hasHoe(p: Player) = p.isHoldingAnyOf(WOOD_HOE, STONE_HOE, IRON_HOE, GOLD_HOE, DIAMOND_HOE)
  val listeners = List(
    OnBlockBreak{ (b,_,_)     => if (b is LONG_GRASS) dropSeedsAt(b) },
    OnRightClickBlock{ (p, e) => if (hasHoe(p) and (e.block is GRASS)) dropSeedsAt(e.block) }
  )
}

// this same plugin exists in microexamples, and elsewhere, but that's ok.
class God extends ListenerPlugin with CommandPlugin {
  val gods = collection.mutable.Map[Player, Boolean]().withDefaultValue(false)
  val listener = OnPlayerDamage { (p, e) => e cancelIf gods(p) }
  val command = Command(
    name = "god", desc = "Toggle God mode.")(
    body = p => {
      gods.update(p, ! gods(p))
      p ! s"god mode ${if(gods(p)) "enabled" else "disabled"}"
    }
  )
}

class PermissionsTest extends CommandsPlugin { self =>
  val permission = anyStringAs("permission")
  val commands = List(
    Command("give-perm",  "Give a player a permission", permission){
      case (p, perm) => p.addAttachment(self).setPermission(perm, true)
    },
    Command("test-perm",  "Test", permission){
      case (p, perm) =>
        if (p hasPermission perm) p ! s"you have permission: $perm"
        else p ! RED(s"you don't have permission: $perm")
    }
  )
}

class PluginCommander extends CommandsPlugin {
  val commands = List(
    Command("enable",  "Enable some plugins",  plugin.+){
      case (_, plugins) => plugins foreach pluginManager.enablePlugin
    },
    Command("disable", "Disable some plugins", plugin.+){
      case (_, plugins) => plugins foreach pluginManager.disablePlugin
    }
  )
}

class NYCDemo extends CommandsPlugin {
  val commands = List(
    Command("demo1",  "Enable BlockChangerGold, Disable BlockChanger and WorldEdit"){ p =>
      pluginManager.enable("BlockChangerGold")
      pluginManager.disableAll("BlockChanger", "WorldEdit")
    },
    Command("demo2",  "Enable BlockChanger, Disable BlockChangerGold and WorldEdit"){ p =>
      pluginManager.enable("BlockChanger")
      pluginManager.disableAll("BlockChangerGold", "WorldEdit")
    },
    Command("demo3",  "Enable WorldEdit, Disable BlockChanger and BlockChangerGold"){ p =>
      pluginManager.enable("WorldEdit")
      pluginManager.disableAll("BlockChanger", "BlockChangerGold")
    }
  )
}

class Thor extends ListeningFor(OnEntityDamageByPlayer { (damagee, thor, _) =>
  if (thor isHoldingA DIAMOND_AXE) damagee.shock
})

class TreeDelogger extends ListeningFor(OnBlockBreak { (b,_,_) =>
  if (b isA LOG) for (b <- b.andBlocksAbove.takeWhile(_ isA LOG)) b.erase
})

class NoRain extends ListenerPlugin {
  val listener = OnWeatherChange(e => e.cancelIf(e.rain, broadcast("Put up an umbrella.")))
}

class YellowBrickRoad extends ListeningFor(OnPlayerMove((p, e) =>
  if (p.blockOn isNot AIR) p.blockOn changeTo GOLD_BLOCK
))

class ZombieApocalypse extends ListeningFor(OnPlayerDeath { (p, _) => p.loc spawn ZOMBIE })
