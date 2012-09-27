package jcdc.pluginfactory

import org.bukkit.ChatColor._
import org.bukkit.command.{CommandSender, Command => BukkitCommand}
import org.bukkit.GameMode._
import org.bukkit.entity.Player
import EnrichmentClasses._
import org.bukkit.{Location, World}

case class CommandBody(argDesc: String, f:(Player, BukkitCommand, List[String]) => Unit)

object Command {
  def apply(name: String, desc: String, body: CommandBody) = new Command(name, Some(desc), body)
  def apply(name: String, body: CommandBody) = new Command(name, None, body)
}

case class Command(name: String, description: Option[String], body: CommandBody)

object BasicMinecraftParsers extends BasicMinecraftParsers

trait BasicMinecraftParsers extends ScalaPlugin with ParserCombinators {
  val gamemode =
    ("c" | "creative" | "1") ^^^ CREATIVE |
    ("s" | "survival" | "0") ^^^ SURVIVAL
  def entity   = token("entity-type")  (findEntity)
  def material = token("material-type")(findMaterial)
  def player   = token("player-name")  (server.findPlayer)
  def location: Parser[World => Location] = (int ~ int ~ int.?) ^^ {
    case x ~ y ~ Some(z) => (w:World) => w(x, y, z)
    case x ~ z ~ None    => (w:World) => w.getHighestBlockAt(x, z)
  }
}

trait CommandPlugin extends CommandsPlugin {
  val command: Command
  def commands = List(command)
}

trait CommandsPlugin extends ScalaPlugin with BasicMinecraftParsers {

  def commands: List[Command]

  def opOnly(ch: CommandBody): CommandBody = CommandBody(
    s"${ch.argDesc} [Op Only]", (player: Player, c: BukkitCommand, args: List[String]) =>
      if (player.isOp) ch.f(player, c, args)
      else player ! (RED + s"You must be an op to run /${c.getName}")
  )

  type PlayerToPlayer = (Player, Player) => Unit
  def p2p(p2pc: PlayerToPlayer): CommandBody = args(player) { case (p1, p2) => p2pc(p1, p2) }

  def noArgs(f: Player => Unit): CommandBody =
    CommandBody("", (p: Player, c: BukkitCommand, args: List[String]) => f(p))

  def args[T](argsParser: Parser[T])(f: ((Player, T)) => Unit): CommandBody =
    CommandBody(
      argsParser.describe, (p: Player, c: BukkitCommand, args: List[String]) => {
        argsParser(args) match {
          case Failure(msg) => p ! (RED + " " + msg)
          case Success(t, _)   => f(p -> t)
        }
      }
    )

  private def lowers = commands.map(c => (c.name.toLowerCase, c)).toMap

  override def onEnable() {
    super.onEnable()
    lowers.foreach { case (name, _) => logInfo("command: " + name) }
  }

  def toPlayer(cs:CommandSender): Player = cs match {
    case p: Player => p
    case _ => ConsolePlayer.player
  }

  override def onCommand(sender: CommandSender, cmd: BukkitCommand,
                         commandLabel: String, args: Array[String]) = {
    println(s"$name handling $commandLabel [${args.mkString(",")}]")
    val p = toPlayer(sender)
    (for (ch <- lowers.get(cmd.getName.toLowerCase)) yield
      try {
        ch.body.f(p, cmd, args.toList)
        true
      }
      catch { case e: Exception =>
        p ! (e.getMessage + "\n" + e.getStackTraceString)
        e.printStackTrace
        false
      }).getOrElse(true)
  }

  override def yml(author:String, version: String) = {
    def commandYml(c: Command) = "  " +
      c.name + ":\n" +
      "    description: " + c.description.getOrElse(c.name) + "\n" +
      "    usage: /<command> " + c.body.argDesc
    val commandsYml = "commands:\n" + commands.map(commandYml).mkString("\n")
    List(super.yml(author, version), commandsYml).mkString("\n")
  }

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

    def origin = server.getWorlds.get(0).blockAt(0,0,0)

    val player = new Player {
      def setWhitelisted(p1: Boolean) {}
      def setPassenger(p1: Entity): Boolean = true
      def getType: EntityType = EntityType.UNKNOWN
      def throwSnowball: Snowball = getWorld.spawn(origin, classOf[Snowball])
      def getLastDamageCause: EntityDamageEvent = new EntityDamageEvent(this, EntityDamageEvent.DamageCause.FALL, 0)
      def closeInventory {}
      def getGameMode: GameMode = GameMode.CREATIVE
      def setMaximumAir(p1: Int) {}
      def kickPlayer(p1: String) {}
      def setTicksLived(p1: Int) {}
      def setExp(p1: Float) {}
      def addPotionEffect(p1: PotionEffect, p2: Boolean): Boolean = false
      def addPotionEffect(p1: PotionEffect): Boolean = false
      def chat(p1: String) {}
      def setWindowProperty(p1: Property, p2: Int): Boolean = false
      def getFireTicks: Int = 0
      def getMaximumNoDamageTicks: Int = 0
      def getTotalExperience: Int = 0
      def getPlayerTimeOffset: Long = 0
      def sendRawMessage(p1: String) {}
      def setGameMode(p1: GameMode) {}
      def setBedSpawnLocation(p1: Location) {}
      def getHealth: Int = 0
      def getItemInHand: ItemStack = new ItemStack(Material.AIR)
      override def getDisplayName: String = getServer.getName
      def damage(p1: Int, p2: Entity) {}
      def damage(p1: Int) {}
      def getNearbyEntities(p1: Double, p2: Double, p3: Double): java.util.List[Entity] = new java.util.LinkedList[Entity]()
      def isSleepingIgnored: Boolean = true
      def setVelocity(p1: Vector) {}
      def setExhaustion(p1: Float) {}
      def setCompassTarget(p1: Location) {}
      def setSaturation(p1: Float) {}
      def playEffect[T](p1: Location, p2: Effect, p3: T) {}
      def playEffect(p1: Location, p2: Effect, p3: Int) {}
      def playEffect(p1: EntityEffect) {}
      def getListeningPluginChannels: java.util.Set[String] = new java.util.HashSet()
      def getPlayer: Player = this
      def getMaxHealth: Int = 0
      def getVehicle: Entity = this // TODO: might not be ok
      def isSneaking: Boolean = false
      def loadData {}
      def setPlayerTime(p1: Long, p2: Boolean) {}
      def isBlocking: Boolean = false
      def isOp: Boolean = true
      def getMetadata(p1: String): java.util.List[MetadataValue] = new java.util.LinkedList()
      def launchProjectile[T <: Projectile](p1: Class[_ <: T]): T = ???
      def hasMetadata(p1: String): Boolean = false
      def setFlySpeed(p1: Float) {}
      def getName: String = getServer.getName
      def getMaxFireTicks: Int = 0
      def getBedSpawnLocation: Location = origin
      def isInsideVehicle: Boolean = false
      def getInventory: PlayerInventory = ???
      def setMetadata(p1: String, p2: MetadataValue) {}
      def setBanned(p1: Boolean) {}
      def incrementStatistic(p1: Statistic, p2: Material, p3: Int) {}
      def incrementStatistic(p1: Statistic, p2: Material) {}
      def incrementStatistic(p1: Statistic, p2: Int) {}
      def incrementStatistic(p1: Statistic) {}
      def isWhitelisted: Boolean = true
      def getLastDamage: Int = 0
      def getKiller: Player = this
      def getEffectivePermissions: java.util.Set[PermissionAttachmentInfo] = new java.util.HashSet()
      def openEnchanting(p1: Location, p2: Boolean): InventoryView = ???
      def getFoodLevel: Int = 0
      def updateInventory {}
      def getLastPlayed: Long = 0
      def getNoDamageTicks: Int = 0
      def setPlayerListName(p1: String) {}
      def getServer: Server = server
      def isSleeping: Boolean = false
      def acceptConversationInput(p1: String) {}
      def hasPotionEffect(p1: PotionEffectType): Boolean = false
      def getAllowFlight: Boolean = false
      def setOp(p1: Boolean) {}
      def getPlayerListName: String = getServer.getName
      def setMaximumNoDamageTicks(p1: Int) {}
      def setFireTicks(p1: Int) {}
      def isPermissionSet(p1: Permission): Boolean = true
      def isPermissionSet(p1: String): Boolean = true
      def getLevel: Int = 0
      def setSneaking(p1: Boolean) {}
      def getRemainingAir: Int = 0
      def isFlying: Boolean = false
      def performCommand(p1: String): Boolean = false
      def eject: Boolean = true
      def setRemainingAir(p1: Int) {}
      def getLastTwoTargetBlocks(p1: java.util.HashSet[java.lang.Byte], p2: Int): java.util.List[org.bukkit.block.Block] = new java.util.LinkedList()
      def sendMap(p1: MapView) {}
      def openInventory(p1: InventoryView) {}
      def openInventory(p1: Inventory): InventoryView = ???
      def playNote(p1: Location, p2: Instrument, p3: Note) {}
      def playNote(p1: Location, p2: Byte, p3: Byte) {}
      def setItemInHand(p1: ItemStack) {}
      def getWalkSpeed: Float = 0
      def addAttachment(p1: Plugin, p2: Int): PermissionAttachment = ???
      def addAttachment(p1: Plugin, p2: String, p3: Boolean, p4: Int): PermissionAttachment = ???
      def addAttachment(p1: Plugin): PermissionAttachment = ???
      def addAttachment(p1: Plugin, p2: String, p3: Boolean): PermissionAttachment = ???
      def getUniqueId: java.util.UUID = ???
      def hasLineOfSight(p1: Entity): Boolean = false
      def openWorkbench(p1: Location, p2: Boolean): InventoryView = ???
      def awardAchievement(p1: Achievement) {}
      def abandonConversation(p1: Conversation, p2: ConversationAbandonedEvent) {}
      def abandonConversation(p1: Conversation) {}
      def leaveVehicle: Boolean = false
      def getCompassTarget: Location = origin
      def remove {}
      def serialize: java.util.Map[String, AnyRef] = new java.util.HashMap[String, AnyRef]
      def recalculatePermissions {}
      def getLineOfSight(p1: java.util.HashSet[java.lang.Byte], p2: Int): java.util.List[Block] = new java.util.LinkedList()
      def setLastDamage(p1: Int) {}
      def getTargetBlock(p1: java.util.HashSet[java.lang.Byte], p2: Int): Block = origin
      def removePotionEffect(p1: PotionEffectType) {}
      def setLevel(p1: Int) {}
      def setFlying(p1: Boolean) {}
      def setLastDamageCause(p1: EntityDamageEvent) {}
      def hasPermission(p1: Permission): Boolean = true
      def hasPermission(p1: String): Boolean = true
      def getFallDistance: Float = 0
      def showPlayer(p1: Player) {}
      def shootArrow: Arrow = getWorld.spawn(origin, classOf[Arrow])
      def sendChunkChange(p1: Location, p2: Int, p3: Int, p4: Int, p5: Array[Byte]): Boolean = true
      def hasPlayedBefore: Boolean = true
      def setFoodLevel(p1: Int) {}
      def getWorld: World = getServer.getWorlds.get(0) // TODO: Important
      def sendBlockChange(p1: Location, p2: Int, p3: Byte) {}
      def sendBlockChange(p1: Location, p2: Material, p3: Byte) {}
      def setFallDistance(p1: Float) {}
      def getTicksLived: Int = 0
      def getPlayerTime: Long = 0
      def throwEgg: Egg = getWorld.spawn(origin, classOf[Egg])
      def setDisplayName(p1: String) {}
      def resetPlayerTime {}
      def getActivePotionEffects: java.util.Collection[PotionEffect] = new java.util.LinkedList()
      def teleport(p1: Entity, p2: TeleportCause): Boolean = false
      def teleport(p1: Entity): Boolean = false
      def teleport(p1: Location, p2: TeleportCause): Boolean = false
      def teleport(p1: Location): Boolean = false
      def getFirstPlayed: Long = 0
      def setTotalExperience(p1: Int) {}
      def saveData {}
      def giveExp(p1: Int) {}
      def removeMetadata(p1: String, p2: Plugin) {}
      def getMaximumAir: Int = 0
      def getPassenger: Entity = this // TODO: might now be ok
      def setHealth(p1: Int) {}
      def setNoDamageTicks(p1: Int) {}
      def isValid: Boolean = true
      def getOpenInventory: InventoryView = ???
      def addPotionEffects(p1: java.util.Collection[PotionEffect]): Boolean = false
      def sendPluginMessage(p1: Plugin, p2: String, p3: Array[Byte]) {}
      def setSleepingIgnored(p1: Boolean) {}
      def isEmpty: Boolean = false
      def isOnline: Boolean = true
      def isPlayerTimeRelative: Boolean = false
      def setWalkSpeed(p1: Float) {}
      def removeAttachment(p1: PermissionAttachment) {}
      def getAddress: InetSocketAddress = ???
      def getExhaustion: Float = 0
      def getLocation: Location = origin
      def getItemOnCursor: ItemStack = new ItemStack(Material.AIR)
      def getEyeLocation: Location = origin
      def isSprinting: Boolean = false
      def getSaturation: Float = 0
      def getExp: Float = 0
      def hidePlayer(p1: Player) {}
      def setAllowFlight(p1: Boolean) {}
      def sendMessage(p1: Array[String]) {}
      def sendMessage(p1: String) {}
      def isBanned: Boolean = false
      def setSprinting(p1: Boolean) {}
      def getVelocity: Vector = ???
      def getEntityId: Int = 0
      def setItemOnCursor(p1: ItemStack) {}
      def getExpToLevel: Int = 0
      def isConversing: Boolean = false
      def getSleepTicks: Int = 0
      def isDead: Boolean = false
      def getFlySpeed: Float = 0
      def beginConversation(p1: Conversation): Boolean = false
      def getEyeHeight(p1: Boolean): Double = 0
      def getEyeHeight: Double = 0
      def canSee(p1: Player): Boolean = true
    }
  }
}