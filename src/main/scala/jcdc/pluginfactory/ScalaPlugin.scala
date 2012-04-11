package jcdc.pluginfactory

import java.util.logging.Logger
import org.bukkit.{ChatColor, Effect, Location, Material, OfflinePlayer, Server, World}
import org.bukkit.block.Block
import org.bukkit.command.{Command, CommandSender}
import org.bukkit.entity.{Entity, EntityType, Player}
import org.bukkit.event.{Cancellable, EventHandler => EH, Listener}
import org.bukkit.event.block.{BlockBreakEvent, BlockDamageEvent}
import org.bukkit.event.entity.{EntityEvent, EntityDamageEvent, PlayerDeathEvent, EntityDamageByEntityEvent}
import org.bukkit.event.player.{PlayerMoveEvent, PlayerEvent, PlayerChatEvent}
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.ItemStack
import ChatColor._
import Effect._
import Material._

object ScalaPlugin {

  val log = Logger.getLogger("Minecraft")

  implicit def pimpedEntity(e:Entity)           = new PimpedEntity(e)
  implicit def pimpedPlayer(player:Player)      = new PimpedPlayer(player)
  implicit def pimpedLocaton(l:Location)        = new PimpedLocation(l)
  implicit def pimpedWorld(w:World)             = new PimpedWorld(w)
  implicit def pimpedServer(s:Server)           = new PimpedServer(s)
  implicit def pimpedBlock(b:Block)             = new PimpedBlock(b)
  implicit def pimpedItemStack(i:ItemStack)     = new PimpedItemStack(i)
  implicit def pimpedCancellable(c:Cancellable) = new PimpedCancellable(c)
  implicit def pimpedEntityEvent(e:EntityEvent) = new PimpedEntity(e.getEntity)
  implicit def pimpedPlayerEvent(e:PlayerEvent) = new PimpedPlayer(e.getPlayer)
  implicit def pimpedWeatherChangeEvent(e:WeatherChangeEvent) = new {
    def rain = e.toWeatherState
    def sun  = ! e.toWeatherState
  }

  case class PimpedBlock(b:Block) {
    lazy val world = b.getWorld
    lazy val loc   = b.getLocation
    lazy val (xd, yd, zd) = (b.getX.toDouble, b.getY.toDouble, b.getZ.toDouble)
    lazy val blockAbove = world.blockAt(xd, yd + 1, zd)
    lazy val blockBelow = world.blockAt(xd, yd - 1, zd)
    def blocksAbove: Stream[Block] = blockAbove #:: blockAbove.blocksAbove
    def blocksBelow: Stream[Block] = blockBelow #:: blockBelow.blocksBelow
    def is(m:Material)    = b.getType == m
    def isA(m:Material)   = b.getType == m
    def isNot(m:Material) = b.getType != m
    def itemStack(n:Int) = new ItemStack(b.getType, 1, b.getData)
    def erase = {
      b.world.dropItem(b.loc, b.itemStack(1))
      b.world.playEffect(b.loc, SMOKE, 1)
      b.setType(AIR)
    }
  }
  case class PimpedCancellable(c:Cancellable){
    def cancel = c.setCancelled(true)
    def cancelIf(b: => Boolean, runBeforeCancelling: => Unit = () => ()){
      if(b) { runBeforeCancelling; c.setCancelled(true) }
    }
  }
  case class PimpedEntity(e:Entity){
    def loc      = e.getLocation
    def server   = e.getServer
    def world    = e.getWorld
    def whenPlayer(f: Player => Unit) = if(e.isInstanceOf[Player]) f(e.asInstanceOf[Player])
    def isA(et:EntityType)  = e.getType == et
    def isAn(et:EntityType) = e.getType == et
  }
  case class PimpedItemStack(i:ItemStack){
    def isA(m:Material)  = i.getType == m
    def isAn(m:Material) = i.getType == m
  }
  case class PimpedWorld(w:World){
    def entities = w.getEntities
    def blockAt(x: Double, y: Double, z: Double) = new Location(w, x, y, z).getBlock
  }
  case class PimpedLocation(loc: Location){
    def world = loc.getWorld
    def block = loc.getBlock
    def spawn(entityType:  EntityType) = world.spawnCreature(loc, entityType)
    def spawnN(entityType: EntityType, n: Int) = for (i <- 1 to n) spawn(entityType)
  }
  case class PimpedServer(s:Server){
    def findPlayer(name:String) = Option(s.getPlayer(name))
    def findOnlinePlayer = findPlayer _
    def findOfflinePlayer(name:String) = Option(s.getOfflinePlayer(name))
    def findOnlinePlayers(names: List[String]): List[Player] = names.map(findOnlinePlayer).flatten
    def findOfflinePlayers(names: List[String]): List[OfflinePlayer] = names.map(findOfflinePlayer).flatten
  }
  case class PimpedPlayer(player:Player){
    def name   = player.getName
    def x      = player.getLocation.getX
    def y      = player.getLocation.getY
    def z      = player.getLocation.getZ
    def loc    = player.getLocation
    def world  = player.getWorld
    def server = player.getServer

    def doTo(otherPlayer: Player, f: => Unit, actionName: String){
      f
      otherPlayer  ! (GREEN + "you have been " + actionName + " by " + player.name)
      player       ! (GREEN + "you have " + actionName + " " + otherPlayer.name)
    }

    def !  (s:String)    = player.sendMessage(s)
    def !* (ss: String*) = ss.foreach(s => player ! s)
    def sendError(message:String) = player.sendMessage(RED + message)
    def sendUsage(cmd:Command)    = sendError(cmd.getUsage)
    def sendUsage(cmd:CommandArguments) = sendError(cmd.cmd.getUsage)
    def findPlayer(name:String)(f: Player => Unit) = server.findPlayer(name) match {
      case Some(p) => f(p)
      case None => sendError("kill could not find player: " + name)
    }
    def findPlayers(names:List[String])(f: Player => Unit) = names.foreach(n => findPlayer(n)(f))
    def ban(reason:String){ player.setBanned(true); player.kickPlayer("banned: " + reason) }
    def kill(playerName:String) = findPlayer(playerName)(p => doTo(p, p.setHealth(0), "killed"))
    def teleportTo(otherPlayer: Player) = player.teleport(otherPlayer)
    def strike = world.strikeLightning(loc)
  }
  // Command combinators.
  case class CommandArguments(cmd:Command, args: List[String])
  type CommandHandler = (Player, CommandArguments) => Unit
  type PlayerToPlayerCommand = (Player, Player, CommandArguments) => Unit
  def findEntity(name:String) = EntityType.values.find(_.toString == name.toUpperCase)
  def opOnly(ch:CommandHandler) = (player: Player, args:CommandArguments) =>
    if(player.isOp) ch(player, args) else player.sendMessage(RED + "You must be an op to run /" + args.cmd.getName)
  def minArgs(n:Int, ch:CommandHandler) = (player: Player, cmd: CommandArguments) =>
    if(cmd.args.length >= n) ch(player, cmd) else player.sendUsage(cmd.cmd)
  def command(ch:CommandHandler)       = minArgs(0, ch)
  def oneArg(ch:CommandHandler)        = minArgs(1, ch)
  def oneOrMoreArgs(ch:CommandHandler) = oneArg(ch)
  def p2p(p2pc:PlayerToPlayerCommand): CommandHandler = oneOrMoreArgs((sender, cmd) =>
    sender.findPlayer(cmd.args(0)) { receiver => p2pc(sender, receiver, cmd) })
  def p2pMany(p2pc:PlayerToPlayerCommand): CommandHandler = (sender: Player, args: CommandArguments) =>
    sender.findPlayers(args.args.toList) { receiver => p2pc(sender, receiver, args) }

  def foldOption[T, U](o:Option[T])(n: => U, s: T => U) = o.map(s).getOrElse(n)
}

import ScalaPlugin._

class ScalaPlugin extends org.bukkit.plugin.java.JavaPlugin {
  def name = this.getDescription.getName
  def server = getServer

  // setup stuff
  override def onEnable(){ super.onEnable(); setupDatabase(); logInfo(name + " enabled!") }
  override def onDisable(){ super.onDisable(); logInfo(name + " disabled!") }
  def registerListener(listener:Listener): Unit = server.getPluginManager.registerEvents(listener, this)

  // logging
  def logInfo(message:String) { log.info("["+name+"] - " + message) }
  def logInfoAround[T](beforeMessage:String, afterMessage:String)(f: => T): T = {
    logInfo(beforeMessage); val t = f; logInfo(afterMessage); t
  }
  def logError(e:Throwable){ log.log(java.util.logging.Level.SEVERE, "["+name+"] - " + e.getMessage, e) }

  def broadcast(message:String) = server.broadcastMessage("["+name+"] - " + message)

  // db setup stuff.
  def dbClasses: List[Class[_]] = Nil
  override def getDatabaseClasses = new java.util.ArrayList[Class[_]](){ dbClasses.foreach(add) }
  def setupDatabase(){
    if(dbClasses.nonEmpty)
      try getDatabase.find(dbClasses.head).findRowCount
      catch{ case e: javax.persistence.PersistenceException =>
        logInfoAround("Installing database...", "installed"){ installDDL() }
      }
  }
  object db {
    import scala.collection.JavaConversions._
    // db commands
    def insert[A](a:A) = try getDatabase.insert(a) catch { case e => logError(e) }
    def query[T](c:Class[T]) = getDatabase.find[T](c)
    def findAll[T](c:Class[T]) = query[T](c).findList
    def foreach[T,U](c:Class[T])(f: T => U) = findAll(c).foreach(f)
    def delete(a:AnyRef){ getDatabase.delete(a) }
  }
}

trait MultiListenerPlugin extends ScalaPlugin {
  val listeners:List[Listener]
  override def onEnable(){ super.onEnable(); listeners.foreach(registerListener) }
}

trait ListenerPlugin extends ScalaPlugin {
  val listener:Listener
  override def onEnable(){ super.onEnable(); registerListener(listener) }
}
case class ListeningFor(listener:Listener) extends ListenerPlugin

trait CommandsPlugin extends ScalaPlugin {

  val commands: Map[String, CommandHandler]
  private def lowers: Map[String, CommandHandler] = commands.map{ case (k,v) => (k.toLowerCase, v)}
  override def onEnable(){
    super.onEnable()
    lowers.keys.foreach{ k => logInfo("["+name+"] command: " + k) }
  }
  override def onCommand(sender:CommandSender, cmd:Command, commandLabel:String, args:Array[String]) = {
    for(ch <- lowers.get(cmd.getName.toLowerCase))
      ch(sender.asInstanceOf[Player], CommandArguments(cmd, args.toList))
    true
  }
}

object Listeners {
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
}
