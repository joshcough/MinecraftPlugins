package jcdc.pluginfactory

import org.bukkit.{Effect, Material}
import org.bukkit.command.Command
import org.bukkit.entity.{Player, Arrow}
import org.bukkit.entity.EntityType._
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.inventory.ItemStack
import ScalaPlugin._
import Listeners._

class NoRain extends ListenerPlugin {
  val listener = OnWeatherChange(e => e.cancelIf(e.rain, broadcast("Put up an umbrella.")))
}

class God extends ListenerPlugin with SingleCommandPlugin {
  val godMap = collection.mutable.Map[Player, Boolean]().withDefaultValue(false)
  val listener = OnPlayerDamage { (p, e) => e.cancelIf(godMap(p)) }
  val command = "god"
  val commandHandler = oneArg((p:Player, c:Command, args:Array[String]) =>
    p.messageAfter("god mode is now " + (if(godMap(p)) "on" else "off")){ godMap.update(p, ! godMap(p)) })
}

class LightningArrows extends Listening(OnEntityDamageByEntity { e =>
  if(e.getDamager.isInstanceOf[Arrow]) e.world.strikeLightning(e.loc)
})

class BanArrows extends Listening(OnPlayerDamageByEntity { (p, e) =>
  if(e.getDamager.isInstanceOf[Arrow]) p.ban("struck by an arrow!")
})

class BlockChanger extends ListenerPlugin with SingleCommandPlugin {
  val users = collection.mutable.Map[Player, Int]()
  val listener = OnBlockDamage((b, e) => users.get(e.getPlayer).foreach(b.setTypeId(_)))
  val command = "bc"
  val commandHandler = oneArg((p:Player, c:Command, args:Array[String]) => args(0).toLowerCase match {
    case "off" => p.messageAfter("bc has been disabled") { users.remove(p) }
    case n => p.messageAfter("bc using blockId="+args(0).toInt) { users += (p -> n.toInt) }
  })
}

object Curses{
  val curses = List("btt", "tiju", "gvdl", "cjudi").map(_.map(c => (c - 1).toChar))
  def containsSwear(event:PlayerChatEvent) = curses.filter(event.getMessage.contains(_)).size > 0
  def check(e: PlayerChatEvent, f: => Unit) = e.cancelIf(containsSwear(e), f)
}

class CurseBan extends Listening(OnPlayerChat((p, e) => Curses.check(e, e.player.ban("no swearing"))))

class CursePreventer extends Listening(OnPlayerChat((p, e) => Curses.check(e, ())))

class ZombieApocalypse extends Listening(OnPlayerDeath { (p, e) =>  p.loc.spawn(ZOMBIE) })

class TreeDelogger extends Listening(OnBlockBreak { (b,e) =>
  if(b.getType == Material.LOG)
    b.blocksAbove.takeWhile(_.getType == Material.LOG).foreach{ b =>
      b.world.playEffect(b.loc, Effect.SMOKE, 1)
      b.world.dropItem(b.loc, new ItemStack(b.getType, 1, b.getData))
      b.setType(Material.AIR)
    }
})
