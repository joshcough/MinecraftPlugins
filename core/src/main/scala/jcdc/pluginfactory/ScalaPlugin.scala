package jcdc.pluginfactory

import java.util.logging.Logger
import javax.persistence.PersistenceException
import org.bukkit.event.{Event, Listener}
import util.Try
import org.bukkit.entity.Player
import org.bukkit.ChatColor._

object ScalaPlugin extends EnrichmentClasses

abstract class ScalaPlugin extends org.bukkit.plugin.java.JavaPlugin with EnrichmentClasses {

  val log  = Logger.getLogger("Minecraft")
  def name = Try(this.getDescription.getName).getOrElse(this.getClass.getSimpleName)
  def server        = getServer
  def pluginManager = getServer.getPluginManager
  def fire(e:Event) = server.getPluginManager.callEvent(e)

  // setup stuff
  override def onEnable(){ super.onEnable(); setupDatabase; logInfo(s"$name enabled!") }
  override def onDisable(){ super.onDisable(); logInfo(s"$name disabled!") }
  def registerListener(listener:Listener): Unit =
    server.getPluginManager.registerEvents(listener, this)

  /**
   * A list of dependencies that this plugin depends on.
   * JcdcPluginFactory is automatically included, which contains Scala, Clojure, and
   * all of the classes in jcdc.pluginfactory.
   * See http://wiki.bukkit.org/Plugin_YAML for more info
   */
  def dependencies: List[String] = Nil

  /**
   * A list of all the soft dependencies for this plugin.
   * See http://wiki.bukkit.org/Plugin_YAML for more info
   */
  def softDependencies: List[String] = Nil

  // db setup stuff.
  def dbClasses: List[Class[_]] = Nil
  override def getDatabaseClasses = new java.util.ArrayList[Class[_]](){ dbClasses.foreach(add) }
  def setupDatabase: Unit =
    if(dbClasses.nonEmpty)
      try getDatabase.find(dbClasses.head).findRowCount
      catch{ case e: PersistenceException => logTask("Installing DB"){ installDDL() } }

  /**
   * Generates the plugin.yml contents for this plugin.
   * See http://wiki.bukkit.org/Plugin_YAML for more info
   **/
  def yml(author:String, version: String) = List(
      "name: "        + this.name,
      "main: "        + this.getClass.getName,
      "author: "      + author,
      "version: "     + version,
      "database: "    + (this.dbClasses.size > 0),
      // the JcdcPluginFactory dependency makes sure Scala, Clojure, and
      // all of the classes in jcdc.pluginfactory are on the classpath at runtime
      "depend: ["     + ("JcdcPluginFactory" :: this.dependencies).mkString(", ") + "]",
      "softdepend: [" + this.softDependencies.mkString(", ") + "]"
    ).mkString("\n")

  /**
   * Writes out the yml file. This is used to build the plugin.
   * @param author  the author of the plugin
   * @param version the version of the plugin
   */
  def writeYML(author: String, version: String): Unit = {
    val resources = new java.io.File("./src/main/resources")
    resources.mkdir
    val ymlFile = new java.io.File(resources, s"${this.name.toLowerCase}.yml")
    val ymlContents = this.yml(author, version)
    val f = new java.io.FileWriter(ymlFile)
    f.write(ymlContents)
    f.close
  }

  // logging
  def logInfo(message:String) { log.info(s"[$name] - $message") }
  def logTask[T](message:String)(f: => T): T = {
    logInfo(s"Starting: $message"); val t = f; logInfo(s"Finished: $message"); t
  }
  def logError(e:Throwable){
    log.log(java.util.logging.Level.SEVERE, s"[$name] - ${e.getMessage}", e)
  }
  def broadcast(message:String) = server.broadcastMessage(s"[$name] - $message")
}
