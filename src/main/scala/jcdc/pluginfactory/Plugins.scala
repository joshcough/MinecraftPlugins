package jcdc.pluginfactory

import org.bukkit.event.Event
import org.bukkit.event.player.{PlayerChatEvent, PlayerListener}
import org.bukkit.event.block.{BlockBreakEvent, BlockListener, BlockDamageEvent}
import org.bukkit.inventory.ItemStack
import org.bukkit.{Effect, Material, ChatColor}
import org.bukkit.event.weather.{WeatherChangeEvent, WeatherListener}
import org.bukkit.entity.{Player, Arrow}
import org.bukkit.event.entity.{EntityDamageEvent, EntityListener, EntityDamageByEntityEvent}
import org.bukkit.command.Command

class NoRain extends ListenerPlugin {
  val eventType = Event.Type.WEATHER_CHANGE
  val listener = new WeatherListener {
    override def onWeatherChange(e:WeatherChangeEvent) =
      if(e.toWeatherState) {
        getServer.broadcastMessage("[NoRain] put up an umbrella.")
        e.setCancelled(true)
      }
  }
}

class God extends ListenerPlugin with SingleCommandPlugin {
  val users = collection.mutable.Map[Player, Boolean]()
  val eventType = Event.Type.ENTITY_DAMAGE
  val listener = new EntityListener {
    override def onEntityDamage(e:EntityDamageEvent){
      def isGod(p:Player) = users.getOrElse(p, false)
      if(e.getEntity.isInstanceOf[Player] && isGod(e.getEntity.asInstanceOf[Player]))
        e.setCancelled(true)
    }
  }
  val command = "god"
  val commandHandler = new CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) = {
      val newGodMode = ! users.getOrElseUpdate(player, false)
      users.update(player, newGodMode)
      player.sendMessage(ChatColor.GREEN + name + " is now " + (if(newGodMode) "on" else "off"))
    }
  }
}

class LightningArrows extends ListenerPlugin {
  val eventType = Event.Type.ENTITY_DAMAGE
  val listener = new EntityDamageByEntityListener {
    override def onEntityDamageByEntity(e:EntityDamageByEntityEvent){
      if(e.getDamager.isInstanceOf[Arrow]) e.getEntity.getWorld.strikeLightning(e.getEntity.getLocation)
    }
  }
}

class BanArrows extends ListenerPlugin {
  val eventType = Event.Type.ENTITY_DAMAGE
  val listener = new EntityDamageByEntityListener {
    override def onEntityDamageByEntity(e:EntityDamageByEntityEvent){
      if(e.getDamager.isInstanceOf[Arrow] && e.getEntity.isInstanceOf[Player])
        ban(e.getEntity.asInstanceOf[Player], "struck by an arrow!")
    }
  }
}

class BlockChanger extends ListenerPlugin with SingleCommandPlugin {
  val blockChangerUsers = collection.mutable.Map[Player, Int]()
  val eventType = Event.Type.BLOCK_DAMAGE
  val listener = new BlockListener {
    override def onBlockDamage(event:BlockDamageEvent){
      blockChangerUsers.get(event.getPlayer).foreach(event.getBlock.setTypeId(_))
    }
  }
  val command = "bc"
  val commandHandler = new CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) = {
      if(args.length == 0) player.sendMessage(ChatColor.RED + "/bc blockId or off")
      else if(args(0).equalsIgnoreCase("off")) {
        blockChangerUsers.remove(player)
        player.sendMessage(ChatColor.GREEN + name + " has been disabled :(")
      }
      else {
        blockChangerUsers += (player -> args(0).toInt)
        player.sendMessage(ChatColor.GREEN + name + " using blockId="+args(0).toInt+" :)")
      }
    }
  }
}

class CurseBan extends ListenerPlugin {
  val eventType = Event.Type.PLAYER_CHAT
  val curses = List("btt", "tiju", "gvdl", "cjudi").map(_.map(c => (c - 1).toChar))
  val listener = new PlayerListener {
    override def onPlayerChat(event:PlayerChatEvent){
      val containsSwear = curses.filter(event.getMessage.contains(_)).size > 0
      if(containsSwear) { ban(event.getPlayer, "no swearing"); event.setCancelled(true) }
    }
  }
}

class TreeDelogger extends ListenerPlugin {
  val eventType = Event.Type.BLOCK_BREAK
  val listener = new BlockListener {
    override def onBlockBreak(e:BlockBreakEvent){
      if(e.getBlock.getType == Material.LOG){
        blocksAbove(e.getBlock).takeWhile(_.getType == Material.LOG).foreach{ b =>
          b.getWorld.playEffect(b.getLocation, Effect.SMOKE, 1)
          b.getWorld.dropItem(b.getLocation, new ItemStack(b.getType, 1, b.getData))
          b.setType(Material.AIR)
        }
      }
    }
  }
}