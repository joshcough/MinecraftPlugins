package jcdc.pluginfactory

import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.{PlayerChatEvent, PlayerListener}
import org.bukkit.event.block.{BlockBreakEvent, BlockListener, BlockDamageEvent}
import org.bukkit.inventory.ItemStack
import org.bukkit.{Effect, Material, GameMode, ChatColor}
import org.bukkit.command.Command
import scala.collection.JavaConversions._
import org.bukkit.event.weather.{WeatherChangeEvent, WeatherListener}
import org.bukkit.entity.{Item, CreatureType, Player, Arrow}

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

class MultiPlayerCommands extends ManyCommandsPlugin {
  val gm = ("gm", new CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) =
      if(args.length == 0 || ! List("c", "s").contains(args(0))) player.sendMessage(ChatColor.RED + "/gm c or s");
      else player.setGameMode(if(args(0) == "c") GameMode.CREATIVE else GameMode.SURVIVAL)
  })
  val kill = ("kill", new CommandHandler {
    def handle(killer: Player, cmd: Command, args: Array[String]) = {
      if(killer.isOp && args.length == 1) {
        Option(getServer.getPlayer(args(0))) match {
          case Some(p) =>
            p.setHealth(0)
            p.sendMessage(ChatColor.RED + "you have been killed by: " + killer.getName)
          case None => killer.sendMessage(ChatColor.RED + "kill could not find player: " + args(0))
        }  
      }
      else killer.sendMessage(ChatColor.RED + "usage: /kill player-name")
    }
  })
  val entities = ("entities", new CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) = {
      player.getWorld.getEntities.foreach(println)
      player.getWorld.getEntities.foreach(e => player.sendMessage(e.toString))
    }
  })
  val killItems = ("kill-items", new CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) = {
      player.getWorld.getEntities.collect{ case i: Item => i }.foreach{ i: Item =>
        i.remove()
      }}}
    )
  val feed = ("feed", new CommandHandler {
    def handle(feeder: Player, cmd: Command, args: Array[String]) = {
      if(feeder.isOp && args.length == 1) {
        Option(getServer.getPlayer(args(0))) match {
          case Some(p) =>
            p.setFoodLevel(20)
            p.sendMessage(ChatColor.GREEN + "you have been fed by " + feeder.getName)
            feeder.sendMessage(ChatColor.GREEN + "you have fed" + feeder.getName)
          case None => feeder.sendMessage(ChatColor.RED + "feed could not find player: " + args(0))
        }
      }
      else feeder.sendMessage(ChatColor.RED + "usage: /feed player-name")
    }
  })
  val starve = ("starve", new CommandHandler {
    def handle(feeder: Player, cmd: Command, args: Array[String]) = {
      if(feeder.isOp && args.length == 1) {
        Option(getServer.getPlayer(args(0))) match {
          case Some(p) =>
            p.setFoodLevel(0)
            p.sendMessage(ChatColor.GREEN + "you have been starved by " + feeder.getName)
            feeder.sendMessage(ChatColor.GREEN + "you have starved " + feeder.getName)
          case None => feeder.sendMessage(ChatColor.RED + "starve could not find player: " + args(0))
        }
      }
      else feeder.sendMessage(ChatColor.RED + "usage: /starve player-name")
    }
  })
  val changetime = ("changetime", new CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) = {
      if(args.length != 1) player.sendMessage(ChatColor.RED + "/changetime h")
      else player.getWorld.setTime(args(0).toInt)
    }
  })
//  val xpAdd = ("xp-add", new CommandHandler {
//    def handle(player: Player, cmd: Command, args: Array[String]) = {
//      if(args.length != 1) player.sendMessage(ChatColor.RED + "/xp-add n")
//      else {
//        try{
//          player.sendMessage(ChatColor.GREEN + "current xp: " + player.getExp)
//          player.setExp(player.getExp + args(0).toFloat)
//          player.sendMessage(ChatColor.GREEN + "xp set to: " + player.getExp)
//        }catch{
//          case e => log.log(java.util.logging.Level.SEVERE, "whoa", e)
//        }
//      }
//    }
//  })
  val day = ("day", new CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) = player.getWorld.setTime(1)
  })
  val night = ("night", new CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) = player.getWorld.setTime(15000)
  })
  val spawn = ("spawn", new CommandHandler {
    def handle(player: Player, cmd: Command, args: Array[String]) = {
      if(args.length < 1) player.sendMessage(ChatColor.RED + "/spawn name [#]")
      else {
        CreatureType.values.find(_.toString == args(0).toUpperCase) match {
          case Some(c) =>
            for(i <- 1 to (if(args.length == 2) args(1).toInt else 1)){
              player.getWorld.spawnCreature(player.getLocation, c)
            }
          case _ => player.sendMessage(ChatColor.RED + "no such creature: " + args(0))
        }
      } 
    }
  })

  val commands = Map(gm, kill, changetime, day, night, spawn, entities, feed, starve, killItems)
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