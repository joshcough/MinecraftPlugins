package jcdc.pluginfactory

import java.util.logging.Logger
import org.bukkit.{ChatColor, OfflinePlayer, Location, Server, World}
import org.bukkit.block.Block
import org.bukkit.command.{Command, CommandSender}
import org.bukkit.entity.{Entity, EntityType, Player}
import org.bukkit.event.{Cancellable, EventHandler => EH, Listener}
import org.bukkit.event.block.{BlockBreakEvent, BlockDamageEvent}
import org.bukkit.event.entity.{EntityEvent, EntityDeathEvent, EntityDamageEvent, EntityDamageByEntityEvent}
import org.bukkit.event.player.{PlayerEvent, PlayerChatEvent}
import org.bukkit.event.weather.WeatherChangeEvent
import ChatColor._

object ScalaPlugin {

  type CommandHandler = (Player, Command, Array[String]) => Unit
  type PlayerToPlayerCommand = (Player, Player, Command, Array[String]) => Unit
  type PlayerToOfflinePlayerCommand = (Player, OfflinePlayer, Command, Array[String]) => Unit

  val log = Logger.getLogger("Minecraft")

  implicit def pimpedPlayer(player:Player)      = new PimpedPlayer(player)
  implicit def pimpedLocaton(l:Location)        = new PimpedLocation(l)
  implicit def pimpedWorld(w:World)             = new PimpedWorld(w)
  implicit def pimpedServer(s:Server)           = new PimpedServer(s)
  implicit def pimpedBlock(b:Block)             = new PimpedBlock(b)
  implicit def pimpedCancellable(c:Cancellable) = new PimpedCancellable(c)
  implicit def pimpedEntityEvent(e:EntityEvent) = new PimpedEntity(e.getEntity)
  implicit def pimpedPlayerEvent(e:PlayerEvent) = new PimpedPlayer(e.getPlayer)
  implicit def pimpedWeatherChangeEvent(e:WeatherChangeEvent) = new {
    def rain = e.toWeatherState
    def sun  = ! e.toWeatherState
  }

  case class PimpedBlock(b:Block) {
    def world = b.getWorld
    def loc   = b.getLocation
    def blocksAbove: Stream[Block] = {
      val ba = new Location(b.getWorld, b.getX.toDouble, b.getY.toDouble + 1, b.getZ.toDouble).getBlock
      ba #:: ba.blocksAbove
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
  }
  case class PimpedWorld(w:World){ def entities = w.getEntities }
  case class PimpedLocation(loc: Location){
    def world = loc.getWorld
    def spawn(entityType: EntityType) = world.spawnCreature(loc, entityType)
  }
  case class PimpedServer(s:Server){ def findPlayer(name:String) = Option(s.getPlayer(name)) }
  case class PimpedPlayer(player:Player){
    def name   = player.getName
    def x      = player.getLocation.getX
    def y      = player.getLocation.getY
    def z      = player.getLocation.getZ
    def loc    = player.getLocation
    def world  = player.getWorld
    def server = player.getServer

    def messageAfter[T](message: => String)(f: => T): T = { val t = f; player.sendMessage(message); t }
    def messageBefore[T](message:String)(f: => T): T = { player.sendMessage(message); f }
    def messageAround[T](beforeMessage:String, afterMessage: => String)(f: => T): T = {
      player.sendMessage(beforeMessage); val t = f; player.sendMessage(afterMessage); t
    }
    def sendError(message:String) = player.sendMessage(RED + message)
    def sendUsage(cmd:Command) = sendError(cmd.getUsage)
    def findPlayer(name:String)(f: Player => Unit) = server.findPlayer(name) match {
      case Some(p) => f(p)
      case None => sendError("kill could not find player: " + name)
    }
    def findPlayers(names:List[String])(f: Player => Unit) = names.foreach(n => findPlayer(n)(f))
    def ban(reason:String){ player.setBanned(true); player.kickPlayer("banned: " + reason) }
    def kill(playerName:String) = player.findPlayer(playerName){ p =>
      p.messageAfter(RED + "you have been killed by: " + player.getName){ p.setHealth(0) }
    }
  }
  def opOnly(ch:CommandHandler) = (player: Player, cmd: Command, args: Array[String]) =>
    if(player.isOp) ch(player, cmd, args) else player.sendMessage(RED + "You must be an op to run /" + cmd.getName)
  def minArgs(n:Int, ch:CommandHandler) = (player: Player, cmd: Command, args: Array[String]) =>
    if(args.length >= n) ch(player, cmd, args) else player.sendUsage(cmd)
  def oneArg(ch:CommandHandler) = minArgs(1, ch)
  def oneOrMoreArgs(ch:CommandHandler) = oneArg(ch)
  def p2p(p2pc:PlayerToPlayerCommand): CommandHandler = (sender: Player, cmd: Command, args: Array[String]) =>
    sender.findPlayer(args(0)) { receiver => p2pc(sender, receiver, cmd, args) }
  def p2pMany(p2pc:PlayerToPlayerCommand): CommandHandler = (sender: Player, cmd: Command, args: Array[String]) =>
    sender.findPlayers(args.toList) { receiver => p2pc(sender, receiver, cmd, args) }
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
      catch{
        case e: javax.persistence.PersistenceException =>
          logInfoAround("Installing database...", "installed"){ installDDL() }
      }
  }
  // db commands
  def dbInsert[A](a:A) = try getDatabase.insert(a) catch { case e => logError(e) }
  def dbQuery[T](c:Class[T]) = getDatabase.find[T](c)
  def findAll[T](c:Class[T]) = dbQuery[T](c).findList
  def dbDelete(a:AnyRef){ getDatabase.delete(a) }
}

trait MultiListenerPlugin extends ScalaPlugin {
  val listeners:List[Listener]
  override def onEnable(){ super.onEnable(); listeners.foreach(registerListener) }
}

trait ListenerPlugin extends ScalaPlugin {
  val listener:Listener
  override def onEnable(){ super.onEnable(); registerListener(listener) }
}
case class Listening(listener:Listener) extends ListenerPlugin

trait SingleCommandPlugin extends ScalaPlugin {
  val command: String
  val commandHandler: CommandHandler
  override def onCommand(sender:CommandSender, cmd:Command, commandLabel:String, args:Array[String]) = {
    if(cmd.getName.equalsIgnoreCase(command)) commandHandler(sender.asInstanceOf[Player], cmd, args)
    true
  }
}

trait ManyCommandsPlugin extends ScalaPlugin {
  val commands: Map[String, CommandHandler]
  private def lowers: Map[String, CommandHandler] = commands.map{ case (k,v) => (k.toLowerCase, v)}
  override def onEnable(){
    super.onEnable()
    lowers.keys.foreach{ k => logInfo("["+name+"] command: " + k) }
  }
  override def onCommand(sender:CommandSender, cmd:Command, commandLabel:String, args:Array[String]) = {
    lowers.get(cmd.getName.toLowerCase).foreach(_(sender.asInstanceOf[Player], cmd, args))
    true
  }
}

object Listeners {
  def OnEntityDamageByEntity(f: EntityDamageByEntityEvent => Unit) = new Listener {
    @EH def onEntityDamage(e:EntityDamageEvent) =
      if(e.isInstanceOf[EntityDamageByEntityEvent]) f(e.asInstanceOf[EntityDamageByEntityEvent])
  }
  def OnPlayerDamageByEntity(f: (Player, EntityDamageByEntityEvent) => Unit) = new Listener {
    @EH def onEntityDamage(e:EntityDamageByEntityEvent) = e.whenPlayer(f(_, e))
  }
  def OnPlayerDamage(f: (Player, EntityDamageEvent) => Unit) = new Listener {
    @EH def onEntityDamage(e:EntityDamageEvent) = e.whenPlayer(f(_, e))
  }
  def OnPlayerDeath(f: (Player, EntityDeathEvent) => Unit) = new Listener {
    @EH def onEntityDeath(e:EntityDeathEvent)   = e.whenPlayer(f(_, e))
  }
  def OnPlayerChat(f: (Player, PlayerChatEvent) => Unit) = new Listener {
    @EH def on(e:PlayerChatEvent) = f(e.getPlayer, e)
  }
  def OnBlockBreak(f: (Block, BlockBreakEvent) => Unit) = new Listener {
    @EH def on(e:BlockBreakEvent) = f(e.getBlock, e)
  }
  def OnBlockDamage(f: (Block, BlockDamageEvent) => Unit) = new Listener {
    @EH def on(e:BlockDamageEvent) = f(e.getBlock, e)
  }
  def OnWeatherChange(f: WeatherChangeEvent => Unit) = new Listener {
    @EH def on(e:WeatherChangeEvent) = f(e)
  }
}

object Spawner {
  def spawn(entityType: String, number: Int, loc: Location, onError: String => Unit){
    EntityType.values.find(_.toString == entityType.toUpperCase) match {
      case Some(creature) => for (i <- 1 to number ) loc.spawn(creature)
      case _ => onError("no such creature: " + entityType)
    }
  }
}
