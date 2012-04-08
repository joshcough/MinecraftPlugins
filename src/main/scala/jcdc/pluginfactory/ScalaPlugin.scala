package jcdc.pluginfactory

import java.util.logging.Logger
import org.bukkit.command.{Command, CommandSender}
import org.bukkit.entity.Player
import org.bukkit.event.{EventHandler, Listener, Event}
import org.bukkit.block.Block
import org.bukkit.{ChatColor, Location}
import ChatColor._
import org.bukkit.event.entity.{EntityDeathEvent, EntityDamageEvent, EntityDamageByEntityEvent}

object ScalaPlugin {

  type CommandHandler = (Player, Command, Array[String]) => Unit
  type PlayerToPlayerCommand = (Player, Player, Command, Array[String]) => Unit

  val log = Logger.getLogger("Minecraft")

  implicit def pimpedBlock(b:Block) = new PimpedBlock(b)
  implicit def pimpedPlayer(player:Player) = new PimpedPlayer(player)

  case class PimpedBlock(b:Block) {
    def blocksAbove: Stream[Block] = {
      val ba = new Location(b.getWorld, b.getX.toDouble, b.getY.toDouble + 1, b.getZ.toDouble).getBlock
      ba #:: ba.blocksAbove
    }
  }

  case class PimpedPlayer(player:Player){
    def x = player.getLocation.getX
    def y = player.getLocation.getY
    def z = player.getLocation.getZ

    def messageAfter[T](message: => String)(f: => T): T = { val t = f; player.sendMessage(message); t }
    def messageBefore[T](message:String)(f: => T): T = { player.sendMessage(message); f }
    def messageAround[T](beforeMessage:String, afterMessage: => String)(f: => T): T = {
      player.sendMessage(beforeMessage); val t = f; player.sendMessage(afterMessage); t
    }
    def sendError(message:String) = player.sendMessage(RED + message)
    def sendUsage(cmd:Command) = sendError(cmd.getUsage)
    def findPlayer(name:String)(f: Player => Unit) = Option(player.getServer.getPlayer(name)) match {
      case Some(p) => f(p)
      case None => sendError("kill could not find player: " + name)
    }
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
  def p2p(p2pc:PlayerToPlayerCommand): CommandHandler = (sender: Player, cmd: Command, args: Array[String]) =>
    sender.findPlayer(args(0)) { receiver => p2pc(sender, receiver, cmd, args) }
}

import ScalaPlugin._

class ScalaPlugin extends org.bukkit.plugin.java.JavaPlugin {
  def name = this.getDescription.getName

  // setup stuff
  override def onEnable(){ super.onEnable(); setupDatabase(); logInfo("enabled!") }
  override def onDisable(){ super.onDisable(); logInfo("disabled!") }
  def registerListener(listener:Listener): Unit =
    getServer().getPluginManager().registerEvents(listener, this);
    //this.getServer.getPluginManager.registerEvent(eventType, listener, Event.Priority.Normal, this)

  // logging
  def logInfo(message:String) { log.info("["+name+"] - " + message) }
  def logInfoAround[T](beforeMessage:String, afterMessage:String)(f: => T): T = {
    logInfo(beforeMessage); val t = f; logInfo(afterMessage); t
  }
  def logError(e:Throwable){ log.log(java.util.logging.Level.SEVERE, "["+name+"] - " + e.getMessage, e) }

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
case class VanillaListenerPlugin(listener:Listener) extends ListenerPlugin

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

trait EntityDamageByEntityListener extends Listener {
  @EventHandler def onEntityDamage(event:EntityDamageEvent) =
    if(event.isInstanceOf[EntityDamageByEntityEvent])
      onEntityDamageByEntity(event.asInstanceOf[EntityDamageByEntityEvent])
  def onEntityDamageByEntity(e:EntityDamageByEntityEvent)
}

trait PlayerDamageByEntityListener extends Listener {
  @EventHandler def onEntityDamage(event:EntityDamageEvent): Unit =
    if(event.isInstanceOf[EntityDamageByEntityEvent] && event.getEntity.isInstanceOf[Player])
      onPlayerDamageByEntity(event.getEntity.asInstanceOf[Player], event.asInstanceOf[EntityDamageByEntityEvent])
  def onPlayerDamageByEntity(p:Player, e:EntityDamageByEntityEvent)
}

trait PlayerDamageListener extends Listener {
  @EventHandler def onEntityDamage(e:EntityDamageEvent): Unit =
    if(e.getEntity.isInstanceOf[Player]) onPlayerDamage(e.getEntity.asInstanceOf[Player], e)
  def onPlayerDamage(p:Player, e:EntityDamageEvent)
}

trait PlayerDeathListener extends Listener {
  @EventHandler def onEntityDamage(e:EntityDeathEvent): Unit =
    if(e.getEntity.isInstanceOf[Player]) onPlayerDeath(e.getEntity.asInstanceOf[Player], e)
  def onPlayerDeath(p:Player, e:EntityDeathEvent)
}