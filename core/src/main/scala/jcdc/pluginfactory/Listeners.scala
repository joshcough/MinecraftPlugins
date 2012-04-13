package jcdc.pluginfactory

import org.bukkit.block.Block
import org.bukkit.entity.{Entity, Player}
import org.bukkit.event.{EventHandler => EH, Listener}
import org.bukkit.event.block.{BlockBreakEvent, BlockDamageEvent}
import org.bukkit.event.block.Action._
import org.bukkit.event.entity.{EntityDamageEvent, PlayerDeathEvent, EntityDamageByEntityEvent}
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.event.player.{PlayerInteractEvent, PlayerMoveEvent, PlayerChatEvent}

trait MultiListenerPlugin extends ScalaPlugin {
  val listeners:List[Listener]
  override def onEnable(){ super.onEnable(); listeners.foreach(registerListener) }
}

trait ListenerPlugin extends ScalaPlugin {
  val listener:Listener
  override def onEnable(){ super.onEnable(); registerListener(listener) }
}

trait ListenersPlugin extends ScalaPlugin with Listeners {
  val listeners:List[Listener]
  override def onEnable(){ super.onEnable(); listeners.foreach(registerListener) }
}

object Listeners extends Listeners

trait Listeners extends Pimps {
  case class ListeningFor(listener:Listener) extends ListenerPlugin

  def OnPlayerMove(f: PlayerMoveEvent => Unit) = new Listener {
    @EH def on(e:PlayerMoveEvent) = f(e)
  }
  def OnEntityDamageByEntity(f: EntityDamageByEntityEvent => Unit) = new Listener {
    @EH def on(e:EntityDamageByEntityEvent) = f(e)
  }
  def OnPlayerDamageByEntity(f: (Player, EntityDamageByEntityEvent) => Unit) = new Listener {
    @EH def on(e:EntityDamageByEntityEvent) = e.whenPlayer(f(_, e))
  }
  def OnEntityDamageByPlayer(f: (Entity, Player, EntityDamageByEntityEvent) => Unit) = new Listener {
    @EH def on(e:EntityDamageByEntityEvent) =
      if(e.getDamager.isInstanceOf[Player]) f(e.getEntity,e.getDamager.asInstanceOf[Player], e)
  }
  def OnPlayerDamage(f: (Player, EntityDamageEvent) => Unit) = new Listener {
    @EH def on(e:EntityDamageEvent)  = e.whenPlayer(f(_, e))
  }
  def OnPlayerDeath(f: (Player, PlayerDeathEvent) => Unit) = new Listener {
    @EH def on(e:PlayerDeathEvent)   = f(e.getEntity, e)
  }
  def OnPlayerChat(f: (Player, PlayerChatEvent) => Unit) = new Listener {
    @EH def on(e:PlayerChatEvent)    = f(e.getPlayer, e)
  }
  def OnBlockBreak(f: (Block, BlockBreakEvent) => Unit) = new Listener {
    @EH def on(e:BlockBreakEvent)    = f(e.getBlock, e)
  }
  def OnBlockDamage(f: (Block, BlockDamageEvent) => Unit) = new Listener {
    @EH def on(e:BlockDamageEvent)   = f(e.getBlock, e)
  }
  def OnWeatherChange(f: WeatherChangeEvent => Unit) = new Listener {
    @EH def on(e:WeatherChangeEvent) = f(e)
  }
  def OnPlayerInteract(f: (Player, PlayerInteractEvent) => Unit) = new Listener {
    @EH def on(e:PlayerInteractEvent) = f(e.getPlayer, e)
  }
  def OnPlayerRightClickBlock(f: (Player, PlayerInteractEvent) => Unit) = new Listener {
    @EH def on(e:PlayerInteractEvent) = if (e.getAction == RIGHT_CLICK_BLOCK) f(e.getPlayer, e)
  }
  def OnPlayerLeftClickBlock(f: (Player, PlayerInteractEvent) => Unit) = new Listener {
    @EH def on(e:PlayerInteractEvent) = if (e.getAction == LEFT_CLICK_BLOCK) f(e.getPlayer, e)
  }
  def OnPlayerRightClickAir(f: (Player, PlayerInteractEvent) => Unit) = new Listener {
    @EH def on(e:PlayerInteractEvent) = if (e.getAction == RIGHT_CLICK_AIR) f(e.getPlayer, e)
  }
  def OnPlayerLeftClickAir(f: (Player, PlayerInteractEvent) => Unit) = new Listener {
    @EH def on(e:PlayerInteractEvent) = if (e.getAction == LEFT_CLICK_AIR) f(e.getPlayer, e)
  }
}
