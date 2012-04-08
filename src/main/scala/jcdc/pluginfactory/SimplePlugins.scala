package jcdc.pluginfactory

import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.block.{BlockBreakEvent, BlockDamageEvent}
import org.bukkit.inventory.ItemStack
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.command.Command
import ScalaPlugin._
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.entity.{EntityDeathEvent, EntityDamageEvent, EntityDamageByEntityEvent}
import org.bukkit.entity.{CreatureType, Player, Arrow}
import org.bukkit.{Location, World, Effect, Material}

class NoRain extends ListenerPlugin {
  val listener = new Listener {
    @EventHandler def onWeatherChange(e:WeatherChangeEvent) = if(e.toWeatherState) {
      getServer.broadcastMessage("[NoRain] put up an umbrella.")
      e.setCancelled(true)
    }
  }
}

class God extends ListenerPlugin with SingleCommandPlugin {
  val godMap = collection.mutable.Map[Player, Boolean]().withDefaultValue(false)
  val listener = new PlayerDamageListener {
    def onPlayerDamage(p: Player, e: EntityDamageEvent) = if(godMap(p)) e.setCancelled(true)
  }
  val command = "god"
  val commandHandler = oneArg((p:Player, c:Command, args:Array[String]) =>
    p.messageAfter("god mode is now " + (if(godMap(p)) "on" else "off")){ godMap.update(p, ! godMap(p)) })
}

class LightningArrows extends VanillaListenerPlugin(new EntityDamageByEntityListener {
  def onEntityDamageByEntity(e:EntityDamageByEntityEvent) =
    if(e.getDamager.isInstanceOf[Arrow]) e.getEntity.getWorld.strikeLightning(e.getEntity.getLocation)
})

class BanArrows extends VanillaListenerPlugin(new PlayerDamageByEntityListener {
  def onPlayerDamageByEntity(p:Player, e:EntityDamageByEntityEvent) =
    if(e.getDamager.isInstanceOf[Arrow]) p.ban("struck by an arrow!")
})

class BlockChanger extends ListenerPlugin with SingleCommandPlugin {
  val users = collection.mutable.Map[Player, Int]()
  val listener = new Listener {
    @EventHandler def onBlockDamage(event:BlockDamageEvent) =
      users.get(event.getPlayer).foreach(event.getBlock.setTypeId(_))
  }
  val command = "bc"
  val commandHandler = oneArg((p:Player, c:Command, args:Array[String]) => args(0).toLowerCase match {
    case "off" => p.messageAfter("bc has been disabled") { users.remove(p) }
    case n => p.messageAfter("bc using blockId="+args(0).toInt) { users += (p -> n.toInt) }
  })
}

object Curses{
  val curses = List("btt", "tiju", "gvdl", "cjudi").map(_.map(c => (c - 1).toChar))
  def containsSwear(event:PlayerChatEvent) = curses.filter(event.getMessage.contains(_)).size > 0
}

class CurseBan extends VanillaListenerPlugin(new Listener{
  @EventHandler def onPlayerChat(event:PlayerChatEvent) = if(Curses.containsSwear(event)) {
    event.getPlayer.ban("no swearing"); event.setCancelled(true)
  }
})

class CursePreventer extends VanillaListenerPlugin(new Listener{
  @EventHandler def onPlayerChat(event:PlayerChatEvent) = if(Curses.containsSwear(event)) event.setCancelled(true)
})

class TreeDelogger extends VanillaListenerPlugin(new Listener {
  @EventHandler def onBlockBreak(e:BlockBreakEvent) = if(e.getBlock.getType == Material.LOG)
    e.getBlock.blocksAbove.takeWhile(_.getType == Material.LOG).foreach{ b =>
      b.getWorld.playEffect(b.getLocation, Effect.SMOKE, 1)
      b.getWorld.dropItem(b.getLocation, new ItemStack(b.getType, 1, b.getData))
      b.setType(Material.AIR)
    }
})

object Spawner {
  def spawn(creatureType: String, number: Int, world: World, loc: Location, onError: String => Unit){
    CreatureType.values.find(_.toString == creatureType.toUpperCase) match {
      case Some(creature) => for (i <- 1 to number )  world.spawnCreature(loc, creature)
      case _ => onError("no such creature: " + creatureType)
    }
  }
}

class ZombieApocalypse extends VanillaListenerPlugin(new PlayerDeathListener {
  def onPlayerDeath(p: Player, e: EntityDeathEvent) {
    Spawner.spawn("zombie", 1, p.getWorld, p.getLocation, sys.error(_))
  }
})