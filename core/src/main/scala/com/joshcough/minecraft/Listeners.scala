package com.joshcough.minecraft

import org.bukkit.block.Block
import org.bukkit.entity.{Entity, Player}
import org.bukkit.event.{EventHandler => EH, Listener}
import org.bukkit.event.block.{BlockBreakEvent, BlockDamageEvent}
import org.bukkit.event.block.Action._
import org.bukkit.event.entity.{EntityDamageEvent, PlayerDeathEvent, EntityDamageByEntityEvent}
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.event.player.{PlayerQuitEvent, PlayerInteractEvent, PlayerMoveEvent, PlayerChatEvent,
                                PlayerJoinEvent, PlayerKickEvent, PlayerLoginEvent, PlayerEvent}

/**
 * A trait that supports exactly one listener.
 * This is really just a tiny convenience wrapper over ListenersPlugin,
 * so that you can say:
 *
 *   val listener = createMyListener
 *
 * instead of:
 *
 *   val listeners = List(createMyListener)
 */
trait ListenerPlugin extends ListenersPlugin {
  def listener: Listener
  def listeners = List(listener)
  override def onEnable(){ super.onEnable(); registerListener(listener) }
}

/**
 * A trait that can have many Listeners.
 *
 * All clients need to do is specify the listeners val, like so:
 *
 * val listeners = List(
 *   createListener1,
 *   createListener2,
 *   ...
 * )
 *
 * Convenience functions for creating Listeners are provided in the Listeners trait.
 */
trait ListenersPlugin extends ScalaPlugin with Listeners {
  def listeners: List[Listener]
  override def onEnable{ super.onEnable(); listeners.foreach(registerListener) }
}

object Listeners extends Listeners
object ListenersObject extends Listeners

/**
 * This trait supports many convenience wrappers for creating Listeners with
 * higher order functions. Creating Listeners in Bukkit is fairly awkward.
 * You have to create a Listener instance with an annotated method,
 * that method can have any name, and it must take some Event as an argument, like so:
 *
 * new Listener {
 *   @EventHandler def on(e:PlayerMoveEvent): Unit = doSomething(e)
 * }
 *
 * This is all abstracted away from the user here. A user now just says:
 *
 * OnPlayerMove(doSomething)
 *
 * (where doSomething is just a function that takes a PlayerMoveEvent, same as above)
 *
 * There are piles of examples of this in the examples code.
 **/
trait Listeners extends BukkitEnrichment {
  abstract case class ListeningFor(listener:Listener) extends ListenerPlugin

  def OnPlayerMove(f: PlayerMoveEvent => Unit) = new Listener {
    @EH def on(e:PlayerMoveEvent): Unit = f(e)
  }
  def OnEntityDamageByEntity(f: EntityDamageByEntityEvent => Unit) = new Listener {
    @EH def on(e:EntityDamageByEntityEvent): Unit = f(e)
  }
  def OnPlayerDamageByEntity(f: (Player, EntityDamageByEntityEvent) => Unit) = new Listener {
    @EH def on(e:EntityDamageByEntityEvent): Unit = e.getEntity.whenPlayer(f(_, e))
  }
  def OnEntityDamageByPlayer(f: (Entity, Player, EntityDamageByEntityEvent) => Unit) = new Listener {
    @EH def on(e:EntityDamageByEntityEvent): Unit = e.getDamager match {
      case p: Player => f(e.getEntity,p, e)
      case _ =>
    }
  }
  def OnPlayerDamage(f: (Player, EntityDamageEvent) => Unit) = new Listener {
    @EH def on(e:EntityDamageEvent): Unit   = e.getEntity.whenPlayer(f(_, e))
  }
  def OnPlayerDeath(f: (Player, PlayerDeathEvent) => Unit) = new Listener {
    @EH def on(e:PlayerDeathEvent): Unit    = f(e.getEntity, e)
  }
  def OnPlayerChat(f: (Player, PlayerChatEvent) => Unit) = new Listener {
    @EH def on(e:PlayerChatEvent): Unit     = f(e.getPlayer, e)
  }
  def OnBlockBreak(f: (Block, Player, BlockBreakEvent) => Unit) = new Listener {
    @EH def on(e:BlockBreakEvent): Unit     = f(e.getBlock, e.getPlayer, e)
  }
  def OnBlockDamage(f: (Block, BlockDamageEvent) => Unit) = new Listener {
    @EH def on(e:BlockDamageEvent): Unit    = f(e.getBlock, e)
  }
  def OnWeatherChange(f: WeatherChangeEvent => Unit) = new Listener {
    @EH def on(e:WeatherChangeEvent): Unit  = f(e)
  }
  def OnPlayerInteract(f: (Player, PlayerInteractEvent) => Unit) = new Listener {
    @EH def on(e:PlayerInteractEvent): Unit = f(e.getPlayer, e)
  }
  def OnRightClickBlock(f: (Player, PlayerInteractEvent) => Unit) = new Listener {
    @EH def on(e:PlayerInteractEvent): Unit = if (e.getAction == RIGHT_CLICK_BLOCK) f(e.getPlayer, e)
  }
  def OnLeftClickBlock(f: (Player, PlayerInteractEvent) => Unit) = new Listener {
    @EH def on(e:PlayerInteractEvent): Unit = if (e.getAction == LEFT_CLICK_BLOCK)  f(e.getPlayer, e)
  }
  def OnPlayerRightClickAir(f: (Player, PlayerInteractEvent) => Unit) = new Listener {
    @EH def on(e:PlayerInteractEvent): Unit = if (e.getAction == RIGHT_CLICK_AIR)   f(e.getPlayer, e)
  }
  def OnPlayerLeftClickAir(f: (Player, PlayerInteractEvent) => Unit) = new Listener {
    @EH def on(e:PlayerInteractEvent): Unit = if (e.getAction == LEFT_CLICK_AIR)    f(e.getPlayer, e)
  }
  def OnPlayerMove(f: (Player, PlayerMoveEvent) => Unit) = new Listener {
    @EH def on(e:PlayerMoveEvent): Unit     = f(e.getPlayer, e)
  }
  def OnPlayerQuit(f: (Player, PlayerQuitEvent) => Unit) = new Listener {
    @EH def on(e: PlayerQuitEvent): Unit = f(e.getPlayer, e)
  }
  def OnPlayerKick(f: (Player, PlayerKickEvent) => Unit) = new Listener {
    @EH def on(e: PlayerKickEvent): Unit = f(e.getPlayer, e)
  }
  def OnPlayerLogin(f: (Player, PlayerLoginEvent) => Unit) = new Listener {
    @EH def on(e: PlayerLoginEvent): Unit = f(e.getPlayer, e)
  }
  def OnPlayerJoin(f: (Player, PlayerJoinEvent) => Unit) = new Listener {
    @EH def on(e: PlayerJoinEvent): Unit = f(e.getPlayer, e)
  }
}
