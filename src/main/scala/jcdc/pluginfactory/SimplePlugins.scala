package jcdc.pluginfactory

import org.bukkit.event.Event
import org.bukkit.event.player.{PlayerChatEvent, PlayerListener}
import org.bukkit.event.block.{BlockBreakEvent, BlockListener, BlockDamageEvent}
import org.bukkit.inventory.ItemStack
import org.bukkit.{Effect, Material}
import org.bukkit.event.weather.{WeatherChangeEvent, WeatherListener}
import org.bukkit.entity.{Player, Arrow}
import org.bukkit.event.entity.{EntityDamageEvent, EntityDamageByEntityEvent}
import org.bukkit.command.Command
import ScalaPluginPredef._

class NoRain extends ListenerPlugin {
  val eventType = Event.Type.WEATHER_CHANGE
  val listener = new WeatherListener {
    override def onWeatherChange(e:WeatherChangeEvent) = if(e.toWeatherState) {
      getServer.broadcastMessage("[NoRain] put up an umbrella.")
      e.setCancelled(true)
    }
  }
}

class God extends ListenerPlugin with SingleCommandPlugin {
  val godMap = collection.mutable.Map[Player, Boolean]().withDefaultValue(false)
  val eventType = Event.Type.ENTITY_DAMAGE
  val listener = new PlayerDamageListener {
    def onPlayerDamage(p: Player, e: EntityDamageEvent) = if(godMap(p)) e.setCancelled(true)
  }
  val command = "god"
  val commandHandler = new CommandHandler {
    def handle(p: Player, cmd: Command, args: Array[String]) =
      p.messageAfter("god mode is now " + (if(godMap(p)) "on" else "off")){ godMap.update(p, ! godMap(p)) }
  }
}

class LightningArrows extends VanillaListenerPlugin(Event.Type.ENTITY_DAMAGE, new EntityDamageByEntityListener {
  override def onEntityDamageByEntity(e:EntityDamageByEntityEvent){
    if(e.getDamager.isInstanceOf[Arrow]) e.getEntity.getWorld.strikeLightning(e.getEntity.getLocation)
  }
})

class BanArrows extends VanillaListenerPlugin(Event.Type.ENTITY_DAMAGE, new PlayerDamageByEntityListener {
  override def onPlayerDamageByEntity(p:Player, e:EntityDamageByEntityEvent){
    if(e.getDamager.isInstanceOf[Arrow]) p.ban("struck by an arrow!")
  }
})

class BlockChanger extends ListenerPlugin with SingleCommandPlugin {
  val users = collection.mutable.Map[Player, Int]()
  val eventType = Event.Type.BLOCK_DAMAGE
  val listener = new BlockListener {
    override def onBlockDamage(event:BlockDamageEvent){
      users.get(event.getPlayer).foreach(event.getBlock.setTypeId(_))
    }
  }
  val command = "bc"
  val commandHandler = new BCHandler with OneArg
  trait BCHandler extends CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) = args(0).toLowerCase match {
      case "off" => player.messageAfter("bc has been disabled") { users.remove(player) }
      case n => player.messageAfter("bc using blockId="+args(0).toInt) { users += (player -> n.toInt) }
    }
  }
}

object Curses{
  val curses = List("btt", "tiju", "gvdl", "cjudi").map(_.map(c => (c - 1).toChar))
  def containsSwear(event:PlayerChatEvent) = curses.filter(event.getMessage.contains(_)).size > 0
}

class CurseBan extends VanillaListenerPlugin(Event.Type.PLAYER_CHAT, new PlayerListener{
  override def onPlayerChat(event:PlayerChatEvent) = if(Curses.containsSwear(event)) {
    event.getPlayer.ban("no swearing"); event.setCancelled(true)
  }
})

class CursePreventer extends VanillaListenerPlugin(Event.Type.PLAYER_CHAT, new PlayerListener{
  override def onPlayerChat(event:PlayerChatEvent) = if(Curses.containsSwear(event)) event.setCancelled(true)
})

class TreeDelogger extends VanillaListenerPlugin(Event.Type.BLOCK_BREAK, new BlockListener {
  override def onBlockBreak(e:BlockBreakEvent) = if(e.getBlock.getType == Material.LOG)
    e.getBlock.blocksAbove.takeWhile(_.getType == Material.LOG).foreach{ b =>
      b.getWorld.playEffect(b.getLocation, Effect.SMOKE, 1)
      b.getWorld.dropItem(b.getLocation, new ItemStack(b.getType, 1, b.getData))
      b.setType(Material.AIR)
    }
})