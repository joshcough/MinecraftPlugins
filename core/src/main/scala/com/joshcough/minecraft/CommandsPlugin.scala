package com.joshcough.minecraft

import org.bukkit.ChatColor._
import org.bukkit.GameMode._
import org.bukkit.command.{CommandSender, Command => BukkitCommand}
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.{Location, World, Material, GameMode}
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.bukkit.Server

object MinecraftParsers extends MinecraftParsers
/**
 * Parsers for all sorts of useful things in Minecraft.
 */
trait MinecraftParsers extends ParserCombinators with BukkitEnrichment {
  val gamemode: Parser[GameMode] =
    ("c" | "creative" | "1") ^^^ CREATIVE |
    ("s" | "survival" | "0") ^^^ SURVIVAL
  val entity  : Parser[EntityType] = maybe("entity-type")  (findEntity)
  val material: Parser[Material]   = maybe("material-type")(findMaterial)
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

  def player(implicit server: Server)  : Parser[Player]
    = maybe("player-name")  (server.findPlayer)
  def plugin(implicit pluginManager: PluginManager)  : Parser[Plugin]
    = maybe("plugin")       (pluginManager.findPlugin(_))

}

/**
 * A trait that allows a plugin to have one command, very easily.
 */
trait
CommandPlugin extends CommandsPlugin {
  import CommandsPlugin._
  val command: Command
  def commands = List(command)
}


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

  import CommandsPlugin._

  def commands: List[Command]

  private lazy val commandsMap = commands.map(c => (c.name.toLowerCase, c)).toMap

  private val consolePlayer = new ConsolePlayer(server)

  /**
    * This is Bukkit's main entry point for plugins handling commands.
    * When a command is entered by a player, the plugin will look through
    * all of its commands. If one matches the command entered, it will be executed.
    */
  override def onCommand(sender: CommandSender, cmd: BukkitCommand,
                         commandName: String, args: Array[String]) = {
    println(s"$name handling $commandName [${args.mkString(",")}]")
    val p = sender match { case p: Player => p; case _ => consolePlayer.player }
    (for (c <- commandsMap.get(commandName.toLowerCase)) yield
      try { c.body(p, args.toList); true }
      catch { case e: Exception =>
        p ! RED(e.getMessage)
        logError(e)
        false
      }).getOrElse(true)
  }

  override def onEnable: Unit = {
    super.onEnable
    commandsMap.foreach { case (name, _) => logInfo("command: " + name) }
  }

  /**
    * Overriding to add commands into the plugin.yml.
    */
  override def yml(author:String, version: String): String =
    List(super.yml(author, version), CommandsPlugin.yml(commands)).mkString("\n")
}

object CommandsPlugin extends MinecraftParsers {
  case class Command(
                      name: String,
                      description: String,
                      argsDescription: Option[String],
                      body: (Player, List[String]) => Unit){
    def filter(f: Player => Boolean, err: => String) = this.copy(
      body = (p, args) => if(f(p)) body(p, args) else p.sendError(err)
    )
  }

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
  def P2P(name: String, desc: String)(f: (Player, Player) => Unit)(implicit server: Server): Command =
    Command(name, desc, player){case (p1,p2) => f(p1, p2)}

  def yml(commands: List[Command]): String = {
    def yml(c: Command) =
      s"  ${c.name}:\n" +
        s"    description: ${c.description}\n" +
        s"    usage: /${c.name} ${c.argsDescription.getOrElse("")}"
    s"commands:\n${commands.map(yml).mkString("\n")}"
  }

  def fullPluginYml(pluginName: String,
                    pluginClassName: String,
                    author:String,
                    version: String,
                    dependencies: List[String],
                    mandatoryDependencies: List[String],
                    softDependencies: List[String],
                    commands: List[Command]): String = {
    List(
      ScalaPlugin.yml(pluginName, pluginClassName, author, version, dependencies, mandatoryDependencies, softDependencies),
      CommandsPlugin.yml(commands)
    ).mkString("\n")
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
  class ConsolePlayer(server: Server) {
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
      override def setWhitelisted(p1: Boolean): Unit = {}
      override def setPassenger(p1: Entity): Boolean = true
      override def getType: EntityType = EntityType.UNKNOWN
      //      override def throwSnowball: Snowball = getWorld.spawn(origin, classOf[Snowball])
      override def getLastDamageCause: EntityDamageEvent = new EntityDamageEvent(this, EntityDamageEvent.DamageCause.FALL, 0)
      override def closeInventory: Unit = {}
      override def getGameMode: GameMode = GameMode.CREATIVE
      override def setMaximumAir(p1: Int): Unit = {}
      override def kickPlayer(p1: String): Unit = {}
      override def setTicksLived(p1: Int): Unit = {}
      override def setExp(p1: Float): Unit = {}
      override def addPotionEffect(p1: PotionEffect, p2: Boolean): Boolean = false
      override def addPotionEffect(p1: PotionEffect): Boolean = false
      override def chat(p1: String): Unit = {}
      override def setWindowProperty(p1: Property, p2: Int): Boolean = false
      override def getFireTicks: Int = 0
      override def getMaximumNoDamageTicks: Int = 0
      override def getTotalExperience: Int = 0
      override def getPlayerTimeOffset: Long = 0
      override def sendRawMessage(p1: String) { println(s"raw message: $p1") }
      override def setGameMode(p1: GameMode): Unit = {}
      override def setBedSpawnLocation(p1: Location): Unit = {}
      override def setBedSpawnLocation(x$1: org.bukkit.Location,x$2: Boolean): Unit = {}
      override def getHealth: Double = 0
      override def getItemInHand: ItemStack = new ItemStack(Material.AIR)
      override def getDisplayName: String = getServer.getName
      //      override def damage(p1: Int, p2: Entity): Unit = {}
      //      override def damage(p1: Int): Unit = {}
      override def getNearbyEntities(p1: Double, p2: Double, p3: Double): java.util.List[Entity] = new java.util.LinkedList[Entity]()
      override def isSleepingIgnored: Boolean = true
      override def setVelocity(p1: Vector): Unit = {}
      override def setExhaustion(p1: Float): Unit = {}
      override def setCompassTarget(p1: Location): Unit = {}
      override def setSaturation(p1: Float): Unit = {}
      override def playEffect[T](p1: Location, p2: Effect, p3: T): Unit = {}
      override def playEffect(p1: Location, p2: Effect, p3: Int): Unit = {}
      override def playEffect(p1: EntityEffect): Unit = {}
      override def getListeningPluginChannels: java.util.Set[String] = new java.util.HashSet()
      override def getPlayer: Player = this
      override def getMaxHealth: Double = 0
      override def getVehicle: Entity = this // TODO: might not be ok
      override def isSneaking: Boolean = false
      override def loadData: Unit = {}
      override def setPlayerTime(p1: Long, p2: Boolean): Unit = {}
      override def isBlocking: Boolean = false
      override def isOp: Boolean = true
      override def getMetadata(p1: String): java.util.List[MetadataValue] = new java.util.LinkedList()
      override def launchProjectile[T <: Projectile](p1: Class[_ <: T]): T = ???
      override def launchProjectile[T <: Projectile](p1: Class[_ <: T], x$2: org.bukkit.util.Vector) : T = ???
      override def hasMetadata(p1: String): Boolean = false
      override def setFlySpeed(p1: Float): Unit = {}
      override def getName: String = getServer.getName
      override def getMaxFireTicks: Int = 0
      override def getBedSpawnLocation: Location = origin
      override def isInsideVehicle: Boolean = false
      override def getInventory: PlayerInventory = ???
      override def setMetadata(p1: String, p2: MetadataValue): Unit = {}
      //      override def setBanned(p1: Boolean): Unit = {}
      override def incrementStatistic(p1: Statistic, p2: Material, p3: Int): Unit = {}
      override def incrementStatistic(p1: Statistic, p2: Material): Unit = {}
      override def incrementStatistic(p1: Statistic, p2: Int): Unit = {}
      override def incrementStatistic(p1: Statistic): Unit = {}
      override def isWhitelisted: Boolean = true
      override def getLastDamage: Double  = 0
      override def getKiller: Player = this
      override def getEffectivePermissions: java.util.Set[PermissionAttachmentInfo] = new java.util.HashSet()
      override def openEnchanting(p1: Location, p2: Boolean): InventoryView = ???
      override def getFoodLevel: Int = 0
      override def updateInventory: Unit = {}
      override def getLastPlayed: Long = 0
      override def getNoDamageTicks: Int = 0
      override def setPlayerListName(p1: String): Unit = {}
      override def getServer: Server = server
      override def isSleeping: Boolean = false
      override def acceptConversationInput(p1: String): Unit = {}
      override def hasPotionEffect(p1: PotionEffectType): Boolean = false
      override def getAllowFlight: Boolean = false
      override def setOp(p1: Boolean): Unit = {}
      override def getPlayerListName: String = getServer.getName
      override def setMaximumNoDamageTicks(p1: Int): Unit = {}
      override def setFireTicks(p1: Int): Unit = {}
      override def isPermissionSet(p1: Permission): Boolean = true
      override def isPermissionSet(p1: String): Boolean = true
      override def getLevel: Int = 0
      override def setSneaking(p1: Boolean): Unit = {}
      override def getRemainingAir: Int = 0
      override def isFlying: Boolean = false
      override def performCommand(p1: String): Boolean = false
      override def eject: Boolean = true
      override def setRemainingAir(p1: Int): Unit = {}
      //      override def getLastTwoTargetBlocks(p1: java.util.HashSet[java.lang.Byte], p2: Int): java.util.List[org.bukkit.block.Block] = new java.util.LinkedList()
      override def sendMap(p1: MapView): Unit = {}
      override def openInventory(p1: InventoryView): Unit = {}
      override def openInventory(p1: Inventory): InventoryView = ???
      override def playNote(p1: Location, p2: Instrument, p3: Note): Unit = {}
      override def playNote(p1: Location, p2: Byte, p3: Byte): Unit = {}
      override def setItemInHand(p1: ItemStack): Unit = {}
      override def getWalkSpeed: Float = 0
      override def addAttachment(p1: Plugin, p2: Int): PermissionAttachment = ???
      override def addAttachment(p1: Plugin, p2: String, p3: Boolean, p4: Int): PermissionAttachment = ???
      override def addAttachment(p1: Plugin): PermissionAttachment = ???
      override def addAttachment(p1: Plugin, p2: String, p3: Boolean): PermissionAttachment = ???
      override def getUniqueId: java.util.UUID = ???
      override def hasLineOfSight(p1: Entity): Boolean = false
      override def openWorkbench(p1: Location, p2: Boolean): InventoryView = ???
      override def awardAchievement(p1: Achievement): Unit = {}
      override def abandonConversation(p1: Conversation, p2: ConversationAbandonedEvent): Unit = {}
      override def abandonConversation(p1: Conversation): Unit = {}
      override def leaveVehicle: Boolean = false
      override def getCompassTarget: Location = origin
      override def remove: Unit = {}
      override def serialize: java.util.Map[String, AnyRef] = new java.util.HashMap[String, AnyRef]
      override def recalculatePermissions: Unit = {}
      //      override def getLineOfSight(p1: java.util.HashSet[java.lang.Byte], p2: Int): java.util.List[Block] = new java.util.LinkedList()
      //      override def setLastDamage(p1: Int): Unit = {}
      //      override def getTargetBlock(p1: java.util.HashSet[java.lang.Byte], p2: Int): Block = origin.block
      override def removePotionEffect(p1: PotionEffectType): Unit = {}
      override def setLevel(p1: Int): Unit = {}
      override def setFlying(p1: Boolean): Unit = {}
      override def setLastDamageCause(p1: EntityDamageEvent): Unit = {}
      override def hasPermission(p1: Permission): Boolean = true
      override def hasPermission(p1: String): Boolean = true
      override def getFallDistance: Float = 0
      override def showPlayer(p1: Player): Unit = {}
      //      override def shootArrow: Arrow = getWorld.spawn(origin, classOf[Arrow])
      override def sendChunkChange(p1: Location, p2: Int, p3: Int, p4: Int, p5: Array[Byte]): Boolean = true
      override def hasPlayedBefore: Boolean = true
      override def setFoodLevel(p1: Int): Unit = {}
      override def getWorld: World = getServer.getWorlds.get(0) // TODO: Important
      //      override def sendBlockChange(p1: Location, p2: Int, p3: Byte): Unit = {}
      override def sendBlockChange(p1: Location, p2: Material, p3: Byte): Unit = {}
      override def setFallDistance(p1: Float): Unit = {}
      override def getTicksLived: Int = 0
      override def getPlayerTime: Long = 0
      //      override def throwEgg: Egg = getWorld.spawn(origin, classOf[Egg])
      override def setDisplayName(p1: String): Unit = {}
      override def resetPlayerTime: Unit = {}
      override def getActivePotionEffects: java.util.Collection[PotionEffect] = new java.util.LinkedList()
      override def teleport(p1: Entity, p2: TeleportCause): Boolean = false
      override def teleport(p1: Entity): Boolean = false
      override def teleport(p1: Location, p2: TeleportCause): Boolean = false
      override def teleport(p1: Location): Boolean = false
      override def getFirstPlayed: Long = 0
      override def setTotalExperience(p1: Int): Unit = {}
      override def saveData: Unit = {}
      override def giveExp(p1: Int): Unit = {}
      override def removeMetadata(p1: String, p2: Plugin): Unit = {}
      override def getMaximumAir: Int = 0
      override def getPassenger: Entity = this // TODO: might now be ok
      //      override def setHealth(p1: Int): Unit = {}
      override def setNoDamageTicks(p1: Int): Unit = {}
      override def isValid: Boolean = true
      override def getOpenInventory: InventoryView = ???
      override def addPotionEffects(p1: java.util.Collection[PotionEffect]): Boolean = false
      override def sendPluginMessage(p1: Plugin, p2: String, p3: Array[Byte]) {
        println(s"plugin message: $p1, $p2, ${p3.mkString("")}")
      }
      override def setSleepingIgnored(p1: Boolean): Unit = {}
      override def isEmpty: Boolean = false
      override def isOnline: Boolean = true
      override def isPlayerTimeRelative: Boolean = false
      override def setWalkSpeed(p1: Float): Unit = {}
      override def removeAttachment(p1: PermissionAttachment): Unit = {}
      override def getAddress: InetSocketAddress = ???
      override def getExhaustion: Float = 0
      override def getLocation: Location = origin
      override def getLocation(l: org.bukkit.Location): org.bukkit.Location = getLocation
      override def getItemOnCursor: ItemStack = new ItemStack(Material.AIR)
      override def getEyeLocation: Location = origin
      override def isSprinting: Boolean = false
      override def getSaturation: Float = 0
      override def getExp: Float = 0
      override def hidePlayer(p1: Player): Unit = {}
      override def setAllowFlight(p1: Boolean): Unit = {}
      override def sendMessage(p1: Array[String]) { p1 foreach sendMessage }
      override def sendMessage(p1: String) { println(p1) }
      override def isBanned: Boolean = false
      override def setSprinting(p1: Boolean): Unit = {}
      override def getVelocity: Vector = ???
      override def getEntityId: Int = 0
      override def setItemOnCursor(p1: ItemStack): Unit = {}
      override def getExpToLevel: Int = 0
      override def isConversing: Boolean = false
      override def getSleepTicks: Int = 0
      override def isDead: Boolean = false
      override def getFlySpeed: Float = 0
      override def beginConversation(p1: Conversation): Boolean = false
      override def getEyeHeight(p1: Boolean): Double = 0
      override def getEyeHeight: Double = 0
      override def canSee(p1: Player): Boolean = true
      override def getEnderChest(): org.bukkit.inventory.Inventory = ???
      override def playSound(x$1: org.bukkit.Location,x$2: org.bukkit.Sound,x$3: Float,x$4: Float): Unit = {}
      override def giveExpLevels(x$1: Int): Unit = {}
      override def getCanPickupItems(): Boolean = false
      override def getEquipment(): org.bukkit.inventory.EntityEquipment = ???
      override def getRemoveWhenFarAway(): Boolean = false
      override def setCanPickupItems(x$1: Boolean): Unit = {}
      override def setRemoveWhenFarAway(x$1: Boolean): Unit = {}
      override def setTexturePack(x$1: String): Unit = {}
      override def resetMaxHealth(): Unit = {}
      //      override def setMaxHealth(x: Int): Unit = {}
      override def getCustomName(): String = "Server"
      override def isCustomNameVisible(): Boolean = true
      override def setCustomName(x$1: String): Unit = {}
      override def setCustomNameVisible(x$1: Boolean): Unit = {}
      override def getPlayerWeather(): org.bukkit.WeatherType = ???
      override def getScoreboard(): org.bukkit.scoreboard.Scoreboard = ???
      override def isOnGround(): Boolean = false
      override def resetPlayerWeather(): Unit = {}
      override def setPlayerWeather(x$1: org.bukkit.WeatherType): Unit = {}
      override def setScoreboard(x$1: org.bukkit.scoreboard.Scoreboard): Unit = {}

      override def damage(x$1: Double,x$2: org.bukkit.entity.Entity): Unit = ???
      override def damage(x$1: Double): Unit = ???
      override def setHealth(x$1: Double): Unit = ???
      override def setMaxHealth(x$1: Double): Unit = ???

      override def getLeashHolder(): org.bukkit.entity.Entity = ???
      override def isLeashed(): Boolean = ???
      override def setLastDamage(x$1: Double): Unit = ???
      override def setLeashHolder(x$1: org.bukkit.entity.Entity): Boolean = ???

      // Members declared in org.bukkit.entity.Player
      override def getHealthScale(): Double = ???
      override def isHealthScaled(): Boolean = ???
      override def playSound(x$1: org.bukkit.Location,x$2: String,x$3: Float,x$4: Float): Unit = ???
      override def setHealthScale(x$1: Double): Unit = ???
      override def setHealthScaled(x$1: Boolean): Unit = ???
      override def setResourcePack(x$1: String): Unit = ???

      def getAttribute(x$1: org.bukkit.attribute.Attribute): org.bukkit.attribute.AttributeInstance = ???
      // Members declared in org.bukkit.entity.Entity
      def addPassenger(x$1: org.bukkit.entity.Entity): Boolean = ???
      def addScoreboardTag(x$1: String): Boolean = ???
      def getBoundingBox(): org.bukkit.util.BoundingBox = ???
      def getFacing(): org.bukkit.block.BlockFace = ???
      def getHeight(): Double = ???
      def getPassengers(): java.util.List[org.bukkit.entity.Entity] = ???
      def getPistonMoveReaction(): org.bukkit.block.PistonMoveReaction = ???
      def getPortalCooldown(): Int = ???
      def getScoreboardTags(): java.util.Set[String] = ???
      def getWidth(): Double = ???
      def hasGravity(): Boolean = ???
      def isGlowing(): Boolean = ???
      def isInvulnerable(): Boolean = ???
      def isPersistent(): Boolean = ???
      def isSilent(): Boolean = ???
      def removePassenger(x$1: org.bukkit.entity.Entity): Boolean = ???
      def removeScoreboardTag(x$1: String): Boolean = ???
      def setGlowing(x$1: Boolean): Unit = ???
      def setGravity(x$1: Boolean): Unit = ???
      def setInvulnerable(x$1: Boolean): Unit = ???
      def setPersistent(x$1: Boolean): Unit = ???
      def setPortalCooldown(x$1: Int): Unit = ???
      def setSilent(x$1: Boolean): Unit = ???
      // Members declared in org.bukkit.entity.HumanEntity
      def discoverRecipe(x$1: org.bukkit.NamespacedKey): Boolean = ???
      def discoverRecipes(x$1: java.util.Collection[org.bukkit.NamespacedKey]): Int = ???
      def getCooldown(x$1: org.bukkit.Material): Int = ???
      def getMainHand(): org.bukkit.inventory.MainHand = ???
      def getShoulderEntityLeft(): org.bukkit.entity.Entity = ???
      def getShoulderEntityRight(): org.bukkit.entity.Entity = ???
      def hasCooldown(x$1: org.bukkit.Material): Boolean = ???
      def isHandRaised(): Boolean = ???
      def openMerchant(x$1: org.bukkit.inventory.Merchant,x$2: Boolean): org.bukkit.inventory.InventoryView = ???
      def openMerchant(x$1: org.bukkit.entity.Villager,x$2: Boolean): org.bukkit.inventory.InventoryView = ???
      def setCooldown(x$1: org.bukkit.Material,x$2: Int): Unit = ???
      def setShoulderEntityLeft(x$1: org.bukkit.entity.Entity): Unit = ???
      def setShoulderEntityRight(x$1: org.bukkit.entity.Entity): Unit = ???
      def undiscoverRecipe(x$1: org.bukkit.NamespacedKey): Boolean = ???
      def undiscoverRecipes(x$1: java.util.Collection[org.bukkit.NamespacedKey]): Int = ???
      // Members declared in org.bukkit.entity.LivingEntity
      def getLastTwoTargetBlocks(x$1: java.util.Set[org.bukkit.Material],x$2: Int): java.util.List[org.bukkit.block.Block] = ???
      def getLineOfSight(x$1: java.util.Set[org.bukkit.Material],x$2: Int): java.util.List[org.bukkit.block.Block] = ???
      def getPotionEffect(x$1: org.bukkit.potion.PotionEffectType): org.bukkit.potion.PotionEffect = ???
      def getTargetBlock(x$1: java.util.Set[org.bukkit.Material],x$2: Int): org.bukkit.block.Block = ???
      def getTargetBlockExact(x$1: Int,x$2: org.bukkit.FluidCollisionMode): org.bukkit.block.Block = ???
      def getTargetBlockExact(x$1: Int): org.bukkit.block.Block = ???
      def hasAI(): Boolean = ???
      def isCollidable(): Boolean = ???
      def isGliding(): Boolean = ???
      def isRiptiding(): Boolean = ???
      def isSwimming(): Boolean = ???
      def rayTraceBlocks(x$1: Double,x$2: org.bukkit.FluidCollisionMode): org.bukkit.util.RayTraceResult = ???
      def rayTraceBlocks(x$1: Double): org.bukkit.util.RayTraceResult = ???
      def setAI(x$1: Boolean): Unit = ???
      def setCollidable(x$1: Boolean): Unit = ???
      def setGliding(x$1: Boolean): Unit = ???
      def setSwimming(x$1: Boolean): Unit = ???
      // Members declared in org.bukkit.entity.Player
      def decrementStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.entity.EntityType,x$3: Int): Unit = ???
      def decrementStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.entity.EntityType): Unit = ???
      def decrementStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.Material,x$3: Int): Unit = ???
      def decrementStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.Material): Unit = ???
      def decrementStatistic(x$1: org.bukkit.Statistic,x$2: Int): Unit = ???
      def decrementStatistic(x$1: org.bukkit.Statistic): Unit = ???
      def getAdvancementProgress(x$1: org.bukkit.advancement.Advancement): org.bukkit.advancement.AdvancementProgress = ???
      def getClientViewDistance(): Int = ???
      def getLocale(): String = ???
      def getPlayerListFooter(): String = ???
      def getPlayerListHeader(): String = ???
      def getSpectatorTarget(): org.bukkit.entity.Entity = ???
      def getStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.entity.EntityType): Int = ???
      def getStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.Material): Int = ???
      def getStatistic(x$1: org.bukkit.Statistic): Int = ???
      def hasAchievement(x$1: org.bukkit.Achievement): Boolean = ???
      def hidePlayer(x$1: org.bukkit.plugin.Plugin,x$2: org.bukkit.entity.Player): Unit = ???
      def incrementStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.entity.EntityType,x$3: Int): Unit = ???
      def incrementStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.entity.EntityType): Unit = ???
      def playSound(x$1: org.bukkit.Location,x$2: String,x$3: org.bukkit.SoundCategory,x$4: Float,x$5: Float): Unit = ???
      def playSound(x$1: org.bukkit.Location,x$2: org.bukkit.Sound,x$3: org.bukkit.SoundCategory,x$4: Float,x$5: Float): Unit = ???
      def removeAchievement(x$1: org.bukkit.Achievement): Unit = ???
      def resetTitle(): Unit = ???
      def sendBlockChange(x$1: org.bukkit.Location,x$2: org.bukkit.block.data.BlockData): Unit = ???
      def sendSignChange(x$1: org.bukkit.Location,x$2: Array[String]): Unit = ???
      def sendTitle(x$1: String,x$2: String,x$3: Int,x$4: Int,x$5: Int): Unit = ???
      def sendTitle(x$1: String,x$2: String): Unit = ???
      def setPlayerListFooter(x$1: String): Unit = ???
      def setPlayerListHeader(x$1: String): Unit = ???
      def setPlayerListHeaderFooter(x$1: String,x$2: String): Unit = ???
      def setResourcePack(x$1: String,x$2: Array[Byte]): Unit = ???
      def setSpectatorTarget(x$1: org.bukkit.entity.Entity): Unit = ???
      def setStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.entity.EntityType,x$3: Int): Unit = ???
      def setStatistic(x$1: org.bukkit.Statistic,x$2: org.bukkit.Material,x$3: Int): Unit = ???
      def setStatistic(x$1: org.bukkit.Statistic,x$2: Int): Unit = ???
      def showPlayer(x$1: org.bukkit.plugin.Plugin,x$2: org.bukkit.entity.Player): Unit = ???
      def spawnParticle[T](x$1: org.bukkit.Particle,x$2: Double,x$3: Double,x$4: Double,x$5: Int,x$6: Double,x$7: Double,x$8: Double,x$9: Double,x$10: T): Unit = ???
      def spawnParticle[T](x$1: org.bukkit.Particle,x$2: org.bukkit.Location,x$3: Int,x$4: Double,x$5: Double,x$6: Double,x$7: Double,x$8: T): Unit = ???
      def spawnParticle(x$1: org.bukkit.Particle,x$2: Double,x$3: Double,x$4: Double,x$5: Int,x$6: Double,x$7: Double,x$8: Double,x$9: Double): Unit = ???
      def spawnParticle(x$1: org.bukkit.Particle,x$2: org.bukkit.Location,x$3: Int,x$4: Double,x$5: Double,x$6: Double,x$7: Double): Unit = ???
      def spawnParticle[T](x$1: org.bukkit.Particle,x$2: Double,x$3: Double,x$4: Double,x$5: Int,x$6: Double,x$7: Double,x$8: Double,x$9: T): Unit = ???
      def spawnParticle[T](x$1: org.bukkit.Particle,x$2: org.bukkit.Location,x$3: Int,x$4: Double,x$5: Double,x$6: Double,x$7: T): Unit = ???
      def spawnParticle(x$1: org.bukkit.Particle,x$2: Double,x$3: Double,x$4: Double,x$5: Int,x$6: Double,x$7: Double,x$8: Double): Unit = ???
      def spawnParticle(x$1: org.bukkit.Particle,x$2: org.bukkit.Location,x$3: Int,x$4: Double,x$5: Double,x$6: Double): Unit = ???
      def spawnParticle[T](x$1: org.bukkit.Particle,x$2: Double,x$3: Double,x$4: Double,x$5: Int,x$6: T): Unit = ???
      def spawnParticle[T](x$1: org.bukkit.Particle,x$2: org.bukkit.Location,x$3: Int,x$4: T): Unit = ???
      def spawnParticle(x$1: org.bukkit.Particle,x$2: Double,x$3: Double,x$4: Double,x$5: Int): Unit = ???
      def spawnParticle(x$1: org.bukkit.Particle,x$2: org.bukkit.Location,x$3: Int): Unit = ???
      def stopSound(x$1: String,x$2: org.bukkit.SoundCategory): Unit = ???
      def stopSound(x$1: org.bukkit.Sound,x$2: org.bukkit.SoundCategory): Unit = ???
      def stopSound(x$1: String): Unit = ???
      def stopSound(x$1: org.bukkit.Sound): Unit = ???
      def updateCommands(): Unit = ???
    }
  }

}
