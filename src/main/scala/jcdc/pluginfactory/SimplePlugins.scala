package jcdc.pluginfactory

import org.bukkit.Effect
import org.bukkit.Material._
import org.bukkit.entity.{Player, Arrow}
import org.bukkit.entity.EntityType._
import ScalaPlugin._
import Listeners._

class NoRain extends ListenerPlugin {
  val listener = OnWeatherChange(e => e.cancelIf(e.rain, broadcast("Put up an umbrella.")))
}

class God extends ListenerPlugin with CommandsPlugin {
  val godMap = collection.mutable.Map[Player, Boolean]().withDefaultValue(false)
  val listener = OnPlayerDamage { (p, e) => e.cancelIf(godMap(p)) }
  val commands = Map("god" -> oneArg((p, _) => {
    godMap.update(p, ! godMap(p))
    p ! ("god mode is now " + (if(godMap(p)) "on" else "off"))
  }))
}

class LightningArrows extends ListeningFor(OnEntityDamageByEntity { e =>
  if(e.getDamager.isInstanceOf[Arrow]) e.world.strikeLightning(e.loc)
})

class BanArrows extends ListeningFor(OnPlayerDamageByEntity { (p, e) =>
  if(e.getDamager.isInstanceOf[Arrow]) p.ban("struck by an arrow!")
})

class BlockChanger extends ListenerPlugin with CommandsPlugin {
  val users = collection.mutable.Map[Player, Int]()
  val listener = OnBlockDamage((b, e) => users.get(e.getPlayer).foreach(b.setTypeId(_)))
  val commands = Map("bc" -> oneArg((p, c) => c.args.head.toLowerCase match {
    case "off" =>
      users.remove(p)
      p ! ("bc has been disabled")
    case n     =>
      users += (p -> n.toInt)
      p !("bc using blockId="+c.args.head.toInt)
  }))
}

object Curses{
  import org.bukkit.event.player.PlayerChatEvent
  val curses = List("btt", "tiju", "gvdl", "cjudi").map(_.map(c => (c - 1).toChar))
  def containsSwear(event:PlayerChatEvent) = curses.filter(event.getMessage.toLowerCase.contains(_)).size > 0
  def handle(e: PlayerChatEvent, f: => Unit) = e.cancelIf(containsSwear(e), f)
}

class CurseBan extends ListeningFor(OnPlayerChat((p, e) => Curses.handle(e, e.player.ban("no swearing"))))

class CursePreventer extends ListeningFor(OnPlayerChat((p, e) => Curses.handle(e, ())))

class ZombieApocalypse extends ListeningFor(OnPlayerDeath { (p, e) =>  p.loc.spawn(ZOMBIE) })

class TreeDelogger extends ListeningFor(OnBlockBreak { (b,e) =>
  if(b.isA(LOG)) for(b <- b.blocksAbove.takeWhile(_.isA(LOG))){
    b.world.playEffect(b.loc, Effect.SMOKE, 1)
    b.world.dropItem(b.loc, b.itemStack(1))
    b.setType(AIR)
  }
})
