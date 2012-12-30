package jcdc.pluginfactory

import org.bukkit.Server
import org.bukkit.event.Event
import org.bukkit.plugin.java.JavaPlugin
import util.Try
import java.util.logging.Logger
import javax.persistence.PersistenceException

/**
 * The base class that helps make writing Bukkit plugins vastly easier.
 * However, it's unlikely that you'll subclass ScalaPlugin directly. It's
 * far more likely that you'll subclass jcdc.pluginfactory.CommandsPlugin,
 * jcdc.pluginfactory.ListenersPlugin, or both.
 */
abstract class ScalaPlugin extends JavaPlugin with BukkitEnrichment {

  lazy val log = Logger.getLogger("Minecraft")

  // setup stuff
  override def onEnable() { super.onEnable() ; setupDatabase; logInfo(s"$name enabled!" ) }
  override def onDisable(){ super.onDisable();                logInfo(s"$name disabled!") }

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

  /**
   * Classes that want to use a database should override this def, providing
   * all of the Entity classes. See WarpPlugin in examples.
   */
  def dbClasses: List[Class[_]]    = Nil
  // this is here just so subclasses dont have to use java.util.ArrayList.
  override def getDatabaseClasses  = new java.util.ArrayList[Class[_]](){ dbClasses.foreach(add) }
  // this is horrible bukkit nonsense that every plugin must do if it wants to use the database.
  private  def setupDatabase: Unit =
    if(dbClasses.nonEmpty)
      // this somehow forces attempting to initialize the database
      try getDatabase.find(dbClasses.head).findRowCount
      // and if it throws... that means you haven't yet initialized the db,
      // and you need to call installDLL...
      // really, this is just crap. happy to hide it from any users.
      catch{ case e: PersistenceException => logTask("Installing DB"){ installDDL() } }

  /**
   * Generates the plugin.yml contents for this plugin.
   * See http://wiki.bukkit.org/Plugin_YAML for more info
   * @param author  the author  of the plugin
   * @param version the version of the plugin
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
   * @param author  the author  of the plugin
   * @param version the version of the plugin
   */
  def writeYML(author: String, version: String): Unit = {
    val ymlContents = this.yml(author, version)
    val resources = new java.io.File("./src/main/resources")
    resources.mkdir

    def write(filename:String): Unit = {
      val f = new java.io.FileWriter(new java.io.File(resources, filename))
      f.write(ymlContents)
      f.close
    }

    write(s"${this.name.toLowerCase}.yml")
    write("plugin.yml")
  }

  /**
   * Broadcast a message to the world.
   * The name of the plugin is prepended to the given message, like so:
   * [plugin-name] - message
   */
  def broadcast(message:String): Unit = server.broadcastMessage(s"[$name] - $message")

  /**
   * Log the given message at INFO level.
   */
  def logInfo(message:String): Unit = { log.info(s"[$name] - $message") }

  /**
   * Log around the given task like so:
   * 'Starting - message'
   *    f
   * 'Finished - message'
   */
  def logTask[T](message:String)(f: => T): T = {
    logInfo(s"Starting: $message"); val t = f; logInfo(s"Finished: $message"); t
  }

  /**
   * Log the given exception at SEVERE level.
   */
  def logError(e:Throwable): Unit = {
    log.log(java.util.logging.Level.SEVERE, s"[$name] - ${e.getMessage}", e)
  }

  // Various other little helper functions.
  def name = Try(this.getDescription.getName).getOrElse(this.getClass.getSimpleName)
  def server: Server      = getServer
  def pluginManager       = getServer.getPluginManager
  def fire(e:Event): Unit = server.getPluginManager.callEvent(e)


  // task stuff:
  def scheduleSyncTask(task: => Unit): Int =
    server.getScheduler.scheduleSyncDelayedTask(this, task)

  def scheduleSyncDelayedTask(initialDelay: Long)(task: => Unit): Int =
    server.getScheduler.scheduleSyncDelayedTask(this, task, initialDelay)

  def scheduleSyncRepeatingTask(period: Long)(task: => Unit): Int =
    server.getScheduler.scheduleSyncRepeatingTask(this, task, 0L, period)

  def scheduleSyncRepeatingTask(initialDelay: Long, period: Long)(task: => Unit): Int =
    server.getScheduler.scheduleSyncRepeatingTask(this, task, initialDelay, period)

  //  server.getScheduler.callSyncMethod()

}
