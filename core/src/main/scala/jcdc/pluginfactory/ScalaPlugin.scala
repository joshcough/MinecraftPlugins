package jcdc.pluginfactory

import java.util.logging.Logger
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.event.{Event, Listener}

object ScalaPlugin extends Pimps

abstract class ScalaPlugin extends org.bukkit.plugin.java.JavaPlugin with Pimps {

  val log = Logger.getLogger("Minecraft")

  def name = this.getDescription.getName
  def server = getServer
  def craftServer = org.bukkit.Bukkit.getServer.asInstanceOf[CraftServer]

  def fire(e:Event) = server.getPluginManager.callEvent(e)

  // setup stuff
  override def onEnable(){ super.onEnable(); setupDatabase; logInfo(name + " enabled!") }
  override def onDisable(){ super.onDisable(); logInfo(name + " disabled!") }
  def registerListener(listener:Listener): Unit =
    server.getPluginManager.registerEvents(listener, this)

  // db setup stuff.
  def dbClasses: List[Class[_]] = Nil
  override def getDatabaseClasses = new java.util.ArrayList[Class[_]](){ dbClasses.foreach(add) }
  def setupDatabase: Unit = if(dbClasses.nonEmpty)
    try getDatabase.find(dbClasses.head).findRowCount
    catch{
      case e: javax.persistence.PersistenceException => logTask("Installing DB"){ installDDL() }
    }

  def yml(author:String, version: String) = List(
      "name: "     + this.getClass.getSimpleName,
      "main: "     + this.getClass.getName,
      "author: "   + author,
      "version: "  + version,
      "database: " + (this.dbClasses.size > 0)
    ).mkString("\n")

  // logging
  def logInfo(message:String) { log.info("["+name+"] - " + message) }
  def logTask[T](message:String)(f: => T): T = {
    logInfo("Starting: " + message); val t = f; logInfo("Finished: " + message); t
  }
  def logError(e:Throwable){ log.log(java.util.logging.Level.SEVERE, "["+name+"] - " + e.getMessage, e) }
  def broadcast(message:String) = server.broadcastMessage("["+name+"] - " + message)
}
