package com.joshcough.minecraft

import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.{Listener, Event}
import org.bukkit.plugin.java.JavaPlugin
import util.Try
import java.util.logging.{Level, Logger}
import javax.persistence.PersistenceException

/**
 * The base class that helps make writing Bukkit plugins vastly easier.
 * However, it's unlikely that you'll subclass ScalaPlugin directly. It's
 * far more likely that you'll subclass com.joshcough.minecraft.CommandsPlugin,
 * com.joshcough.minecraft.ListenersPlugin, or both.
 */
abstract class ScalaPlugin extends JavaPlugin with BukkitEnrichment { scalaPlugin =>

  lazy val log = Logger.getLogger("Minecraft")

  // setup stuff
  override def onEnable:  Unit = {
    super.onEnable
    this.saveDefaultConfig
    setupDatabase
    logInfo(s"$name enabled!")
  }

  override def onDisable: Unit = { super.onDisable; logInfo(s"$name disabled!") }

  /**
   * A list of dependencies that this plugin depends on.
   * JcdcPluginFactory is automatically included, which contains Scala, Clojure, and
   * all of the classes in com.joshcough.minecraft.
   * See http://wiki.bukkit.org/Plugin_YAML for more info
   */
  def dependencies: List[String] = Nil

  // the ScalaLibPlugin provides Scala at runtime.
  // the ScalaPluginAPI provides com.joshcough.minecraft.* classes at runtime
  private val mandatoryDependencies = List("ScalaLibPlugin", "ScalaPluginAPI")

  /**
   * A list of all the soft dependencies for this plugin.
   * See http://wiki.bukkit.org/Plugin_YAML for more info
   */
  def softDependencies: List[String] = Nil

  // TODO: is there a real yml data type i could use?
  def configs: Map[String, String] = Map()

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
    "depend: ["     + (mandatoryDependencies ++ this.dependencies).mkString(", ") + "]",
    "softdepend: [" + this.softDependencies.mkString(", ") + "]"
  ).mkString("\n")

  /**
   * Writes out the plugin.yml file, and config.yml.
   * @param author  the author  of the plugin
   * @param version the version of the plugin
   */
  def writeYML(author: String, version: String, outputDir: String = "."): Unit = {
    val resources = new java.io.File(outputDir)
    resources.mkdirs
    def write(contents: String, filename:String): Unit = {
      val f = new java.io.FileWriter(new java.io.File(resources, filename))
      f.write(contents)
      f.close
    }
    write(yml(author, version), "plugin.yml")
    write(configs.toList.map{ case (k, v) => s"$k: $v" }.mkString("\n"), "config.yml")
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
  def logInfo(message:String): Unit = logMessage(Level.INFO, message)

  /**
   * Log the given message at WARNING level.
   */
  def logWarning(message:String): Unit = logMessage(Level.WARNING, message)

  /**
   * Log the given exception at SEVERE level.
   */
  def logError(e:Throwable): Unit = logMessage(Level.SEVERE, e.getMessage + e.getStackTraceString)

  private def logMessage(level: Level, message: String): Unit =
    log.log(level, s"[$name] - $message")

  /**
   * Log around the given task like so:
   * 'Starting - message'
   *    f
   * 'Finished - message'
   */
  def logTask[T](message:String)(f: => T): T = {
    logInfo(s"Starting: $message"); val t = f; logInfo(s"Finished: $message"); t
  }


  // Various other little helper functions.
  def name = Try(this.getDescription.getName).getOrElse(this.getClass.getSimpleName)
  def server: Server      = getServer
  def pluginManager       = getServer.getPluginManager
  def fire(e:Event): Unit = server.getPluginManager.callEvent(e)
  def registerListener(listener:Listener): Unit = pluginManager.registerEvents(listener, this)

  // task stuff:
  private lazy val scheduler = server.getScheduler

  case class Task(id:Int)

  def scheduleSyncTask(task: => Unit): Task =
    Task(scheduler.scheduleSyncDelayedTask(this, task))

  def scheduleSyncDelayedTask(initialDelay: Long)(task: => Unit): Task =
    Task(scheduler.scheduleSyncDelayedTask(this, task, initialDelay))

  def scheduleSyncRepeatingTask(period: Long)(task: => Unit): Task =
    Task(scheduler.scheduleSyncRepeatingTask(this, task, 0L, period))

  def scheduleSyncRepeatingTask(initialDelay: Long, period: Long)(task: => Unit): Task =
    Task(scheduler.scheduleSyncRepeatingTask(this, task, initialDelay, period))

  def cancelTask(t: Task) = scheduler cancelTask t.id

  case class PlayerTasks(cancelOnExit: Boolean = true) extends PlayerState[Seq[Task]] { self =>
    override val default: Option[Seq[Task]] = Some(Nil)

    registerListener(Listeners.OnPlayerQuit((p, _) => if(cancelOnExit) p.cancelAll))

    implicit class PlayerWithTaskFunctions(p:Player){
      private def addTask(t: Task): Task = { self += (p -> (self(p) :+ t)); t }

      def scheduleSyncTask(task: => Unit): Task = addTask(scalaPlugin.scheduleSyncTask(task))

      def scheduleSyncRepeatingTask(initialDelay: Long, period: Long)(task: => Unit): Task =
        addTask(scalaPlugin.scheduleSyncRepeatingTask(initialDelay, period)(task))

      def cancelTask(t: Task): Unit = {
        scheduler cancelTask t.id
        self += (p -> self(p).filter(_ != t))
      }
      def cancelAll: Unit = {
        logInfo(s"canceling all tasks for: $p")
        (self -= p) foreach { t =>
          logInfo(s"canceling: $t")
          scheduler cancelTask t.id
        }
      }
    }
  }

  /**
   * Invokes a command programmatically.
   */
  def runCommand(p: Player, commandName: String, args: Seq[String]) = {
    p ! s"$name running: $commandName ${args.mkString(" ")}"
    onCommand(p, getCommand(commandName), commandName, args.toArray)
  }
}

/**
 * This plugin and this code is not intended for use.
 * It is just an empty plugin that is required for turning this library
 * into a plugin, so that the API and Scala can be on the classpath for
 * plugins that want to use this API.
 */
class ScalaPluginAPI extends org.bukkit.plugin.java.JavaPlugin {
  override def onEnable  : Unit = {}
  override def onDisable : Unit = {}
}
