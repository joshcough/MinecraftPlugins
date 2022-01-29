package com.joshcough.minecraft

import org.bukkit.ChatColor._
import org.bukkit.GameMode._
import org.bukkit.command.{CommandSender, Command => BukkitCommand}
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.{Location, World, Material, GameMode}
import org.bukkit.plugin.Plugin


object MinecraftParsers extends MinecraftParsers
/**
 * Parsers for all sorts of useful things in Minecraft.
 */
trait MinecraftParsers extends ScalaPlugin with ParserCombinators {
  val gamemode: Parser[GameMode] =
    ("c" | "creative" | "1") ^^^ CREATIVE |
    ("s" | "survival" | "0") ^^^ SURVIVAL
  val entity  : Parser[EntityType] = maybe("entity-type")  (findEntity)
  val material: Parser[Material]   = maybe("material-type")(findMaterial)
  val player  : Parser[Player]     = maybe("player-name")  (server.findPlayer)
  val plugin  : Parser[Plugin]     = maybe("plugin")       (pluginManager.findPlugin(_))
  val coordinates = int ~ int ~ int.?
  val location: Parser[World => Location] = coordinates ^^ {
    case x ~ z ~ Some(y) => (w:World) => w(x, y, z).loc
    case x ~ z ~ None    => (w:World) => w.getHighestBlockAt(x, z).loc
  }
  val time    : Parser[Int] = int.named("time").
    filterWith(i => i >= 0 && i <= 24000)(_ => "time must be between 0 and 24000")
  val length  : Parser[Int] = int.named("length")
  val height  : Parser[Int] = int.named("height")
  val depth   : Parser[Int] = int.named("depth")
}

/**
 * A trait that allows a plugin to have one command, very easily.
 */
trait
CommandPlugin extends CommandsPlugin {
  val command: Command
  def commands = List(command)
}

object CommandsPlugin extends CommandsPlugin{ val commands = Nil }

/**
 * A trait that greatly simplifies creating Bukkit commands.
 * There are many examples in the examples folder that
 * demonstrate how to use this trait. But here is a simple example:
 *
 * trait GotoPlugin extends CommandsPlugin {
 *   val commands = List(
 *     Command("goto", "Teleport!", args(location){ case (you, loc) =>
 *       you teleport loc(you.world)
 *     })
 *   )
 * }
 *
 * Users simply provide a list of commands they want to use in their plugin.
 *
 * A Command is:
 *   - A name
 *   - A description
 *   - and a 'CommandBody'
 *
 * A CommandBody consists of
 *   - a parser for the arguments of the command
 *   - and a function that takes
 *      - the player that entered the command
 *      - and result of parsing
 *
 * If the command is entered, the parser runs on the arguments.
 * If the parser succeeds, the function is ran.
 * If it fails, the player is given an error message.
 *
 * In the example above, args(location){ you teleport loc(you.world) }
 * TODO keep writing.
 *
 */
trait CommandsPlugin extends ScalaPlugin with MinecraftParsers {

  case class Command(
    name: String,
    description: String,
    argsDescription: Option[String],
    body: (Player, List[String]) => Unit){
    def filter(f: Player => Boolean, err: => String) = this.copy(
      body = (p, args) => if(f(p)) body(p, args) else p.sendError(err)
    )
  }

  def commands: List[Command]

  def ArgsCommand[T](name: String, desc: String, p: Parser[T])
                    (body: ((Player, T)) => Unit): Command = Command(name, desc, p)(body)

  def NoArgsCommand(name: String, desc: String)
                   (body: Player => Unit): Command = Command(name, desc)(body)

  /**
   * Create commands that takes arguments.
   *
   * Takes an Parser[T] and a function from Player and T to Unit
   * If the command is executed, then the parser is run with the command arguments.
   *
   * If those arguments successfully parse, the parser will produce a T.
   * The function (the body of the command) will then be invoked with the player
   * that entered the command, and the T produced by parsing.
   *
   * If the arguments do not parse, then the player who entered the command
   * is sent an error message, explaining what went wrong, and how to use the command.
   *
   * @param name
   * @param desc
   * @param args
   * @param body
   * @tparam T
   * @return
   */
  def Command[T](name: String, desc: String, args: Parser[T])
                (body: ((Player, T)) => Unit): Command = new Command(
    name = name, description = desc, argsDescription = Some(args.describe),
    body = (p: Player, argsList: List[String]) =>
      (args <~ eof)(argsList) match {
        case Success(t,_) => body(p -> t)
        case Failure(msg) => p !* (RED(msg), RED(desc), RED(s"/$name ${args.describe}"))
      }
  )

  /**
   * Create a command that take no arguments.
   *
   * Takes a function from Player to unit,
   * and if the command is entered by a player
   * the function is executed with that player.
   * @param name
   * @param desc
   * @param body
   * @return
   */
  def Command(name: String, desc: String)(body: Player => Unit): Command =
    Command(name, desc, eof){ case (p, _) => body(p) }

  private lazy val commandsMap = commands.map(c => (c.name.toLowerCase, c)).toMap

  /**
   * This is Bukkit's main entry point for plugins handling commands.
   * When a command is entered by a player, the plugin will look through
   * all of its commands. If one matches the command entered, it will be executed.
   */
  override def onCommand(sender: CommandSender, cmd: BukkitCommand,
                         commandName: String, args: Array[String]) = {
    println(s"$name handling $commandName [${args.mkString(",")}]")
    val p = sender match { case p: Player => p; case _ => ConsolePlayer.player }
    (for (c <- commandsMap.get(commandName.toLowerCase)) yield
      try { c.body(p, args.toList); true }
      catch { case e: Exception =>
        p ! RED(e.getMessage)
        logError(e)
        false
      }).getOrElse(true)
  }

  override def onEnable {
    super.onEnable
    commandsMap.foreach { case (name, _) => logInfo("command: " + name) }
  }

  /**
   * TODO: rewrite comment
   *
   * This should probably be done better, using bukkit permissions, but later.
   *
   * Takes a CommandBody and wraps it in another CommandBody that first checks
   * if the user is an op. If the user is an op, the inner CommandBody is executed.
   * If not, then the user is given an error message.
   */
  def OpOnly(c: Command): Command = c.filter(_.isOp, s"You must be an op to run /${c.name}")

  /**
   * Simple combinator for creating commands that take a single Player argument (only).
   */
  def P2P(name: String, desc: String)(f: (Player, Player) => Unit): Command =
    Command(name, desc, player){case (p1,p2) => f(p1, p2)}

  /**
   * Overriding to add commands into the plugin.yml.
   */
  override def yml(author:String, version: String) = {
    def yml(c: Command) =
      s"  ${c.name}:\n" +
      s"    description: ${c.description}\n" +
      s"    usage: /${c.name} ${c.argsDescription.getOrElse("")}"
    val commandsYml = s"commands:\n${commands.map(yml).mkString("\n")}"
    List(super.yml(author, version), commandsYml).mkString("\n")
  }

  /**
   * This is used so that any plugin commands can be entered at the console.
   * it 'converts' a the console to a player, that way not all plugins have to
   * check if(console) then ... else ...
   * this might be less type safe, and allows commands to be entered at the console
   * that really shouldnt be entered, however, it keeps the api very clean
   * and the console owner will generally be aware of what should and shouldnt work
   * at the console anyway.
   *
   * Here's one trivial example of a command that wouldn't work:
   *
   * If there was a command that opened the users inventory (eg: /inv), it wouldn't
   * work because the console has no inventory, and the open inventory function is
   * implemented like this:
   *
   *  def openInventory(p1: Inventory): InventoryView = ???
   */
  object ConsolePlayer {
    import org.bukkit._
    import org.bukkit.inventory._
    import org.bukkit.inventory.InventoryView.Property
    import conversations.{ConversationAbandonedEvent, Conversation}
    import java.net.InetSocketAddress
    import org.bukkit.entity._
    import org.bukkit.map.MapView
    import org.bukkit.metadata.MetadataValue
    import org.bukkit.permissions.{PermissionAttachmentInfo, Permission, PermissionAttachment}
    import org.bukkit.plugin.Plugin
    import org.bukkit.potion.{PotionEffectType, PotionEffect}
    import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
    import org.bukkit.event.entity.EntityDamageEvent
    import org.bukkit.block.Block
    import org.bukkit.util.Vector

    def origin = server.getWorlds.get(0).blockAt(0,0,0).loc

    val player = new Player {
      def setWhitelisted(p1: Boolean): Unit = {}
      def setPassenger(p1: Entity): Boolean = true
      def getType: EntityType = EntityType.UNKNOWN
      def throwSnowball: Snowball = getWorld.spawn(origin, classOf[Snowball])
      def getLastDamageCause: EntityDamageEvent = new EntityDamageEvent(this, EntityDamageEvent.DamageCause.FALL, 0)
      def closeInventory: Unit = {}
      def getGameMode: GameMode = GameMode.CREATIVE
      def setMaximumAir(p1: Int): Unit = {}
      def kickPlayer(p1: String): Unit = {}
      def setTicksLived(p1: Int): Unit = {}
      def setExp(p1: Float): Unit = {}
      def addPotionEffect(p1: PotionEffect, p2: Boolean): Boolean = false
      def addPotionEffect(p1: PotionEffect): Boolean = false
      def chat(p1: String): Unit = {}
      def setWindowProperty(p1: Property, p2: Int): Boolean = false
      def getFireTicks: Int = 0
      def getMaximumNoDamageTicks: Int = 0
      def getTotalExperience: Int = 0
      def getPlayerTimeOffset: Long = 0
      def sendRawMessage(p1: String) { println(s"raw message: $p1") }
      def setGameMode(p1: GameMode): Unit = {}
      def setBedSpawnLocation(p1: Location): Unit = {}
      def setBedSpawnLocation(x$1: org.bukkit.Location,x$2: Boolean): Unit = {}
      def getHealth: Double = 0
      def getItemInHand: ItemStack = new ItemStack(Material.AIR)
      override def getDisplayName: String = getServer.getName
      def damage(p1: Int, p2: Entity): Unit = {}
      def damage(p1: Int): Unit = {}
      def getNearbyEntities(p1: Double, p2: Double, p3: Double): java.util.List[Entity] = new java.util.LinkedList[Entity]()
      def isSleepingIgnored: Boolean = true
      def setVelocity(p1: Vector): Unit = {}
      def setExhaustion(p1: Float): Unit = {}
      def setCompassTarget(p1: Location): Unit = {}
      def setSaturation(p1: Float): Unit = {}
      def playEffect[T](p1: Location, p2: Effect, p3: T): Unit = {}
      def playEffect(p1: Location, p2: Effect, p3: Int): Unit = {}
      def playEffect(p1: EntityEffect): Unit = {}
      def getListeningPluginChannels: java.util.Set[String] = new java.util.HashSet()
      def getPlayer: Player = this
      def getMaxHealth: Double = 0
      def getVehicle: Entity = this // TODO: might not be ok
      def isSneaking: Boolean = false
      def loadData: Unit = {}
      def setPlayerTime(p1: Long, p2: Boolean): Unit = {}
      def isBlocking: Boolean = false
      def isOp: Boolean = true
      def getMetadata(p1: String): java.util.List[MetadataValue] = new java.util.LinkedList()
      def launchProjectile[T <: Projectile](p1: Class[_ <: T]): T = ???
      def launchProjectile[T <: org.bukkit.entity.Projectile](x$1: Class[_ <: T], x$2: org.bukkit.util.Vector): T = ???
      def hasMetadata(p1: String): Boolean = false
      def setFlySpeed(p1: Float): Unit = {}
      def getName: String = getServer.getName
      def getMaxFireTicks: Int = 0
      def getBedSpawnLocation: Location = origin
      def isInsideVehicle: Boolean = false
      def getInventory: PlayerInventory = ???
      def setMetadata(p1: String, p2: MetadataValue): Unit = {}
      def setBanned(p1: Boolean): Unit = {}
      def incrementStatistic(p1: Statistic, p2: Material, p3: Int): Unit = {}
      def incrementStatistic(p1: Statistic, p2: Material): Unit = {}
      def incrementStatistic(p1: Statistic, p2: Int): Unit = {}
      def incrementStatistic(p1: Statistic): Unit = {}
      def isWhitelisted: Boolean = true
      def getLastDamage: Double  = 0
      def getKiller: Player = this
      def getEffectivePermissions: java.util.Set[PermissionAttachmentInfo] = new java.util.HashSet()
      def openEnchanting(p1: Location, p2: Boolean): InventoryView = ???
      def getFoodLevel: Int = 0
      def updateInventory: Unit = {}
      def getLastPlayed: Long = 0
      def getNoDamageTicks: Int = 0
      def setPlayerListName(p1: String): Unit = {}
      def getServer: Server = server
      def isSleeping: Boolean = false
      def acceptConversationInput(p1: String): Unit = {}
      def hasPotionEffect(p1: PotionEffectType): Boolean = false
      def getAllowFlight: Boolean = false
      def setOp(p1: Boolean): Unit = {}
      def getPlayerListName: String = getServer.getName
      def setMaximumNoDamageTicks(p1: Int): Unit = {}
      def setFireTicks(p1: Int): Unit = {}
      def isPermissionSet(p1: Permission): Boolean = true
      def isPermissionSet(p1: String): Boolean = true
      def getLevel: Int = 0
      def setSneaking(p1: Boolean): Unit = {}
      def getRemainingAir: Int = 0
      def isFlying: Boolean = false
      def performCommand(p1: String): Boolean = false
      def eject: Boolean = true
      def setRemainingAir(p1: Int): Unit = {}
      def getLastTwoTargetBlocks(p1: java.util.HashSet[java.lang.Byte], p2: Int): java.util.List[org.bukkit.block.Block] = new java.util.LinkedList()
      def sendMap(p1: MapView): Unit = {}
      def openInventory(p1: InventoryView): Unit = {}
      def openInventory(p1: Inventory): InventoryView = ???
      def playNote(p1: Location, p2: Instrument, p3: Note): Unit = {}
      def playNote(p1: Location, p2: Byte, p3: Byte): Unit = {}
      def setItemInHand(p1: ItemStack): Unit = {}
      def getWalkSpeed: Float = 0
      def addAttachment(p1: Plugin, p2: Int): PermissionAttachment = ???
      def addAttachment(p1: Plugin, p2: String, p3: Boolean, p4: Int): PermissionAttachment = ???
      def addAttachment(p1: Plugin): PermissionAttachment = ???
      def addAttachment(p1: Plugin, p2: String, p3: Boolean): PermissionAttachment = ???
      def getUniqueId: java.util.UUID = ???
      def hasLineOfSight(p1: Entity): Boolean = false
      def openWorkbench(p1: Location, p2: Boolean): InventoryView = ???
      def awardAchievement(p1: Achievement): Unit = {}
      def abandonConversation(p1: Conversation, p2: ConversationAbandonedEvent): Unit = {}
      def abandonConversation(p1: Conversation): Unit = {}
      def leaveVehicle: Boolean = false
      def getCompassTarget: Location = origin
      def remove: Unit = {}
      def serialize: java.util.Map[String, AnyRef] = new java.util.HashMap[String, AnyRef]
      def recalculatePermissions: Unit = {}
      def getLineOfSight(p1: java.util.HashSet[java.lang.Byte], p2: Int): java.util.List[Block] = new java.util.LinkedList()
      def setLastDamage(p1: Int): Unit = {}
      def getTargetBlock(p1: java.util.HashSet[java.lang.Byte], p2: Int): Block = origin.block
      def removePotionEffect(p1: PotionEffectType): Unit = {}
      def setLevel(p1: Int): Unit = {}
      def setFlying(p1: Boolean): Unit = {}
      def setLastDamageCause(p1: EntityDamageEvent): Unit = {}
      def hasPermission(p1: Permission): Boolean = true
      def hasPermission(p1: String): Boolean = true
      def getFallDistance: Float = 0
      def showPlayer(p1: Player): Unit = {}
      def shootArrow: Arrow = getWorld.spawn(origin, classOf[Arrow])
      def sendChunkChange(p1: Location, p2: Int, p3: Int, p4: Int, p5: Array[Byte]): Boolean = true
      def hasPlayedBefore: Boolean = true
      def setFoodLevel(p1: Int): Unit = {}
      def getWorld: World = getServer.getWorlds.get(0) // TODO: Important
      def sendBlockChange(p1: Location, p2: Int, p3: Byte): Unit = {}
      def sendBlockChange(p1: Location, p2: Material, p3: Byte): Unit = {}
      def setFallDistance(p1: Float): Unit = {}
      def getTicksLived: Int = 0
      def getPlayerTime: Long = 0
      def throwEgg: Egg = getWorld.spawn(origin, classOf[Egg])
      def setDisplayName(p1: String): Unit = {}
      def resetPlayerTime: Unit = {}
      def getActivePotionEffects: java.util.Collection[PotionEffect] = new java.util.LinkedList()
      def teleport(p1: Entity, p2: TeleportCause): Boolean = false
      def teleport(p1: Entity): Boolean = false
      def teleport(p1: Location, p2: TeleportCause): Boolean = false
      def teleport(p1: Location): Boolean = false
      def getFirstPlayed: Long = 0
      def setTotalExperience(p1: Int): Unit = {}
      def saveData: Unit = {}
      def giveExp(p1: Int): Unit = {}
      def removeMetadata(p1: String, p2: Plugin): Unit = {}
      def getMaximumAir: Int = 0
      def getPassenger: Entity = this // TODO: might now be ok
      def setHealth(p1: Int): Unit = {}
      def setNoDamageTicks(p1: Int): Unit = {}
      def isValid: Boolean = true
      def getOpenInventory: InventoryView = ???
      def addPotionEffects(p1: java.util.Collection[PotionEffect]): Boolean = false
      def sendPluginMessage(p1: Plugin, p2: String, p3: Array[Byte]) {
        println(s"plugin message: $p1, $p2, ${p3.mkString("")}")
      }
      def setSleepingIgnored(p1: Boolean): Unit = {}
      def isEmpty: Boolean = false
      def isOnline: Boolean = true
      def isPlayerTimeRelative: Boolean = false
      def setWalkSpeed(p1: Float): Unit = {}
      def removeAttachment(p1: PermissionAttachment): Unit = {}
      def getAddress: InetSocketAddress = ???
      def getExhaustion: Float = 0
      def getLocation: Location = origin
      def getLocation(l: org.bukkit.Location): org.bukkit.Location = getLocation
      def getItemOnCursor: ItemStack = new ItemStack(Material.AIR)
      def getEyeLocation: Location = origin
      def isSprinting: Boolean = false
      def getSaturation: Float = 0
      def getExp: Float = 0
      def hidePlayer(p1: Player): Unit = {}
      def setAllowFlight(p1: Boolean): Unit = {}
      def sendMessage(p1: Array[String]) { p1 foreach sendMessage }
      def sendMessage(p1: String) { println(p1) }
      def isBanned: Boolean = false
      def setSprinting(p1: Boolean): Unit = {}
      def getVelocity: Vector = ???
      def getEntityId: Int = 0
      def setItemOnCursor(p1: ItemStack): Unit = {}
      def getExpToLevel: Int = 0
      def isConversing: Boolean = false
      def getSleepTicks: Int = 0
      def isDead: Boolean = false
      def getFlySpeed: Float = 0
      def beginConversation(p1: Conversation): Boolean = false
      def getEyeHeight(p1: Boolean): Double = 0
      def getEyeHeight: Double = 0
      def canSee(p1: Player): Boolean = true
      def getEnderChest(): org.bukkit.inventory.Inventory = ???
      def playSound(x$1: org.bukkit.Location,x$2: org.bukkit.Sound,x$3: Float,x$4: Float): Unit = {}
      def giveExpLevels(x$1: Int): Unit = {}
      def getCanPickupItems(): Boolean = false
      def getEquipment(): org.bukkit.inventory.EntityEquipment = ???
      def getRemoveWhenFarAway(): Boolean = false
      override def setCanPickupItems(x$1: Boolean): Unit = {}
      def setRemoveWhenFarAway(x$1: Boolean): Unit = {}
      def setTexturePack(x$1: String): Unit = {}
      def resetMaxHealth(): Unit = {}
      def setMaxHealth(x: Int): Unit = {}
      def getCustomName(): String = "Server"
      def isCustomNameVisible(): Boolean = true
      def setCustomName(x$1: String): Unit = {}
      def setCustomNameVisible(x$1: Boolean): Unit = {}
      def getPlayerWeather(): org.bukkit.WeatherType = ???
      def getScoreboard(): org.bukkit.scoreboard.Scoreboard = ???
      def isOnGround(): Boolean = false
      def resetPlayerWeather(): Unit = {}
      def setPlayerWeather(x$1: org.bukkit.WeatherType): Unit = {}
      def setScoreboard(x$1: org.bukkit.scoreboard.Scoreboard): Unit = {}

      // 1.7 update
      def _INVALID_damage(x$1: Int,x$2: org.bukkit.entity.Entity): Unit = ???
      def _INVALID_damage(x$1: Int): Unit = ???
      def _INVALID_getHealth(): Int = ???
      def _INVALID_getMaxHealth(): Int = ???
      def _INVALID_setHealth(x$1: Int): Unit = ???
      def _INVALID_setMaxHealth(x$1: Int): Unit = ???
      def damage(x$1: Double,x$2: org.bukkit.entity.Entity): Unit = ???
      def damage(x$1: Double): Unit = ???
      def setHealth(x$1: Double): Unit = ???
      def setMaxHealth(x$1: Double): Unit = ???

      // Members declared in org.bukkit.entity.LivingEntity
      def _INVALID_getLastDamage(): Int = ???
      def _INVALID_setLastDamage(x$1: Int): Unit = ???
      def getLeashHolder(): org.bukkit.entity.Entity = ???
      def isLeashed(): Boolean = ???
      def setLastDamage(x$1: Double): Unit = ???
      def setLeashHolder(x$1: org.bukkit.entity.Entity): Boolean = ???

      // Members declared in org.bukkit.entity.Player
      def getHealthScale(): Double = ???
      def isHealthScaled(): Boolean = ???
      def playSound(x$1: org.bukkit.Location,x$2: String,x$3: Float,x$4: Float): Unit = ???
      def setHealthScale(x$1: Double): Unit = ???
      def setHealthScaled(x$1: Boolean): Unit = ???
      def setResourcePack(x$1: String): Unit = ???

      def decrementStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.entity.EntityType,x$3: Int): Unit = ???
      def decrementStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.entity.EntityType): Unit = ???
      def decrementStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.Material,x$3: Int): Unit = ???
      def decrementStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.Material): Unit = ???
      def decrementStatistic(x$1: org.bukkit.Statistic,x$2: Int): Unit = ???
      def decrementStatistic(x$1: org.bukkit.Statistic): Unit = ???
      def getStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.entity.EntityType): Int = ???
      def getStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.Material): Int = ???
      def getStatistic(x$1: org.bukkit.Statistic): Int = ???
      def hasAchievement(x$1: org.bukkit.Achievement): Boolean = ???
      def incrementStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.entity.EntityType,x$3: Int): Unit = ???
      def incrementStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.entity.EntityType): Unit = ???
      def removeAchievement(x$1: org.bukkit.Achievement): Unit = ???
      def setStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.entity.EntityType,x$3: Int): Unit = ???
      def setStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.Material,x$3: Int): Unit = ???
      def setStatistic(x$1: org.bukkit.Statistic,x$2: Int): Unit = ???
    }
  }
}
