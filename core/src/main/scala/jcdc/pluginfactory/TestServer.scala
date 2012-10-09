package jcdc.pluginfactory

import betterjava.JavaTestWorld
import org.bukkit._
import org.bukkit.inventory._
import org.bukkit.inventory.InventoryView.Property
import org.bukkit.entity._
import org.bukkit.map.MapView
import org.bukkit.metadata.MetadataValue
import org.bukkit.permissions.{PermissionAttachmentInfo, Permission, PermissionAttachment}
import org.bukkit.plugin.Plugin
import org.bukkit.potion.{PotionEffectType, PotionEffect}
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.util.Vector
import block._
import conversations.{ConversationAbandonedEvent, Conversation}
import EnrichmentClasses._
import java.util
import java.net.InetSocketAddress

// TODO: rename this
object TestServer {

  def originBlock(x:Int, y:Int, z:Int): Block = new Block {
    def getType: Material = Material.STONE
    def setTypeIdAndData(p1: Int, p2: Byte, p3: Boolean): Boolean = true
    def getLightFromSky: Byte = 0:Byte
    def setTypeId(p1: Int, p2: Boolean): Boolean = true
    def setTypeId(p1: Int): Boolean = true
    def getY: Int = y
    def getData: Byte = 0:Byte
    def getPistonMoveReaction: PistonMoveReaction = ???
    def isBlockFacePowered(p1: BlockFace): Boolean = false
    def hasMetadata(p1: String): Boolean = false
    def getMetadata(p1: String): util.List[MetadataValue] = new util.LinkedList[MetadataValue]()
    def setMetadata(p1: String, p2: MetadataValue) {}
    def isBlockIndirectlyPowered: Boolean = false
    def getBlockPower: Int = 0
    def getBlockPower(p1: BlockFace): Int =  0
    def getLightFromBlocks: Byte = 0:Byte
    def getLightLevel: Byte = 0:Byte
    def getTypeId: Int = getType.getId
    def getBiome: Biome = ???
    def setType(p1: Material) {}
    def isLiquid: Boolean = false
    def getX: Int = x
    def setData(p1: Byte, p2: Boolean) {}
    def setData(p1: Byte) {}
    def getHumidity: Double = 0
    def setBiome(p1: Biome) {}
    def getRelative(p1: BlockFace, p2: Int): Block = this
    def getRelative(p1: BlockFace): Block = this
    def getRelative(p1: Int, p2: Int, p3: Int): Block = this
    def getState: BlockState = ???
    def getWorld: World = world
    def removeMetadata(p1: String, p2: Plugin) {}
    def getZ: Int = z
    def isEmpty: Boolean = true
    def getDrops(p1: ItemStack): util.Collection[ItemStack] = new util.LinkedList[ItemStack]()
    def getDrops: util.Collection[ItemStack] = new util.LinkedList[ItemStack]()
    def getLocation: Location = origin
    def isBlockFaceIndirectlyPowered(p1: BlockFace): Boolean = false
    def getChunk: Chunk = ???
    def breakNaturally(p1: ItemStack): Boolean = false
    def breakNaturally(): Boolean = false
    def getTemperature: Double = 0d
    def getFace(p1: Block): BlockFace = ???
    def isBlockPowered: Boolean = false
  }

  val world: World = new JavaTestWorld {
    def getMetadata(x$1: String): java.util.List[org.bukkit.metadata.MetadataValue] = ???
    def hasMetadata(x$1: String): Boolean = ???
    def removeMetadata(x$1: String,x$2: org.bukkit.plugin.Plugin): Unit = ???
    def setMetadata(x$1: String,x$2: org.bukkit.metadata.MetadataValue): Unit = ???
    // Members declared in org.bukkit.plugin.messaging.PluginMessageRecipient
    def getListeningPluginChannels(): java.util.Set[String] = ???
    def sendPluginMessage(x$1: org.bukkit.plugin.Plugin,x$2: String,x$3: Array[Byte]): Unit = ???
    // Members declared in org.bukkit.World
    def canGenerateStructures(): Boolean = ???
    def createExplosion(x$1: org.bukkit.Location,x$2: Float,x$3: Boolean): Boolean = ???
    def createExplosion(x$1: org.bukkit.Location,x$2: Float): Boolean = ???
    def createExplosion(x$1: Double,x$2: Double,x$3: Double,x$4: Float,x$5: Boolean): Boolean = ???
    def createExplosion(x$1: Double,x$2: Double,x$3: Double,x$4: Float): Boolean = ???
    def dropItem(x$1: org.bukkit.Location,x$2: org.bukkit.inventory.ItemStack): org.bukkit.entity.Item = ???
    def dropItemNaturally(x$1: org.bukkit.Location,x$2: org.bukkit.inventory.ItemStack): org.bukkit.entity.Item = ???
    def generateTree(x$1: org.bukkit.Location,x$2: org.bukkit.TreeType,x$3: org.bukkit.BlockChangeDelegate): Boolean = ???
    def generateTree(x$1: org.bukkit.Location,x$2: org.bukkit.TreeType): Boolean = ???
    def getAllowAnimals(): Boolean = ???
    def getAllowMonsters(): Boolean = ???
    def getAnimalSpawnLimit(): Int = ???
    def getBiome(x$1: Int,x$2: Int): org.bukkit.block.Biome = ???
    def getBlockAt(loc: org.bukkit.Location): org.bukkit.block.Block = originBlock(loc.x, loc.y, loc.z)
    def getBlockAt(x: Int,y: Int,z: Int): org.bukkit.block.Block = originBlock(x, y, z)
    def getBlockTypeIdAt(x$1: org.bukkit.Location): Int = ???
    def getBlockTypeIdAt(x$1: Int,x$2: Int,x$3: Int): Int = ???
    def getChunkAt(x$1: org.bukkit.block.Block): org.bukkit.Chunk = ???
    def getChunkAt(x$1: org.bukkit.Location): org.bukkit.Chunk = ???
    def getChunkAt(x$1: Int,x$2: Int): org.bukkit.Chunk = ???
    def getDifficulty(): org.bukkit.Difficulty = ???
    def getEmptyChunkSnapshot(x$1: Int,x$2: Int,x$3: Boolean,x$4: Boolean): org.bukkit.ChunkSnapshot = ???
    def getEntities(): java.util.List[org.bukkit.entity.Entity] = ???
    def getEntitiesByClass[T <: Entity](p1: Class[T]): util.Collection[T] = ???
    def getEnvironment(): org.bukkit.World.Environment = ???
    def getFullTime(): Long = ???
    def getGenerator(): org.bukkit.generator.ChunkGenerator = ???
    def getHighestBlockAt(loc: org.bukkit.Location): org.bukkit.block.Block = originBlock(loc.x, 4, loc.z)
    def getHighestBlockAt(x: Int,z: Int): org.bukkit.block.Block = originBlock(x,4,z)
    def getHighestBlockYAt(x$1: org.bukkit.Location): Int = 4
    def getHighestBlockYAt(x$1: Int,x$2: Int): Int = 4
    def getHumidity(x$1: Int,x$2: Int): Double = ???
    def getKeepSpawnInMemory(): Boolean = ???
    def getLivingEntities(): java.util.List[org.bukkit.entity.LivingEntity] = ???
    def getLoadedChunks(): Array[org.bukkit.Chunk] = ???
    def getMaxHeight(): Int = ???
    def getMonsterSpawnLimit(): Int = ???
    def getName(): String = ???
    def getPVP(): Boolean = ???
    def getPlayers(): java.util.List[org.bukkit.entity.Player] = ???
    def getPopulators(): java.util.List[org.bukkit.generator.BlockPopulator] = ???
    def getSeaLevel(): Int = ???
    def getSeed(): Long = ???
    def getSpawnLocation(): org.bukkit.Location = ???
    def getTemperature(x$1: Int,x$2: Int): Double = ???
    def getThunderDuration(): Int = ???
    def getTicksPerAnimalSpawns(): Long = ???
    def getTicksPerMonsterSpawns(): Long = ???
    def getTime(): Long = ???
    def getUID(): java.util.UUID = ???
    def getWaterAnimalSpawnLimit(): Int = ???
    def getWeatherDuration(): Int = ???
    def getWorldFolder(): java.io.File = ???
    def getWorldType(): org.bukkit.WorldType = ???
    def hasStorm(): Boolean = ???
    def isAutoSave(): Boolean = ???
    def isChunkInUse(x$1: Int,x$2: Int): Boolean = ???
    def isChunkLoaded(x$1: Int,x$2: Int): Boolean = ???
    def isChunkLoaded(x$1: org.bukkit.Chunk): Boolean = ???
    def isThundering(): Boolean = ???
    def loadChunk(x$1: Int,x$2: Int,x$3: Boolean): Boolean = ???
    def loadChunk(x$1: Int,x$2: Int): Unit = ???
    def loadChunk(x$1: org.bukkit.Chunk): Unit = ???
    def playEffect[T](x$1: org.bukkit.Location,x$2: org.bukkit.Effect,x$3: T,x$4: Int): Unit = ???
    def playEffect[T](x$1: org.bukkit.Location,x$2: org.bukkit.Effect,x$3: T): Unit = ???
    def playEffect(x$1: org.bukkit.Location,x$2: org.bukkit.Effect,x$3: Int,x$4: Int): Unit = ???
    def playEffect(x$1: org.bukkit.Location,x$2: org.bukkit.Effect,x$3: Int): Unit = ???
    def playSound(x$1: org.bukkit.Location,x$2: org.bukkit.Sound,x$3: Float,x$4: Float): Unit = ???
    def refreshChunk(x$1: Int,x$2: Int): Boolean = ???
    def regenerateChunk(x$1: Int,x$2: Int): Boolean = ???
    def save(): Unit = ???
    def setAnimalSpawnLimit(x$1: Int): Unit = ???
    def setAutoSave(x$1: Boolean): Unit = ???
    def setBiome(x$1: Int,x$2: Int,x$3: org.bukkit.block.Biome): Unit = ???
    def setDifficulty(x$1: org.bukkit.Difficulty): Unit = ???
    def setFullTime(x$1: Long): Unit = ???
    def setKeepSpawnInMemory(x$1: Boolean): Unit = ???
    def setMonsterSpawnLimit(x$1: Int): Unit = ???
    def setPVP(x$1: Boolean): Unit = ???
    def setSpawnFlags(x$1: Boolean,x$2: Boolean): Unit = ???
    def setSpawnLocation(x$1: Int,x$2: Int,x$3: Int): Boolean = ???
    def setStorm(x$1: Boolean): Unit = ???
    def setThunderDuration(x$1: Int): Unit = ???
    def setThundering(x$1: Boolean): Unit = ???
    def setTicksPerAnimalSpawns(x$1: Int): Unit = ???
    def setTicksPerMonsterSpawns(x$1: Int): Unit = ???
    def setTime(x$1: Long): Unit = ???
    def setWaterAnimalSpawnLimit(x$1: Int): Unit = ???
    def setWeatherDuration(x$1: Int): Unit = ???
    def spawn[T <: org.bukkit.entity.Entity](x$1: org.bukkit.Location,x$2: Class[T]): T = ???
    def spawnArrow(x$1: org.bukkit.Location,x$2: org.bukkit.util.Vector,x$3: Float,x$4: Float): org.bukkit.entity.Arrow = ???
    def spawnCreature(x$1: org.bukkit.Location,x$2: org.bukkit.entity.CreatureType): org.bukkit.entity.LivingEntity = ???
    def spawnCreature(x$1: org.bukkit.Location,x$2: org.bukkit.entity.EntityType): org.bukkit.entity.LivingEntity = ???
    def spawnEntity(x$1: org.bukkit.Location,x$2: org.bukkit.entity.EntityType): org.bukkit.entity.Entity = ???
    def spawnFallingBlock(x$1: org.bukkit.Location,x$2: Int,x$3: Byte): org.bukkit.entity.FallingBlock = ???
    def spawnFallingBlock(x$1: org.bukkit.Location,x$2: org.bukkit.Material,x$3: Byte): org.bukkit.entity.FallingBlock = ???
    def strikeLightning(x$1: org.bukkit.Location): org.bukkit.entity.LightningStrike = ???
    def strikeLightningEffect(x$1: org.bukkit.Location): org.bukkit.entity.LightningStrike = ???
    def unloadChunk(x$1: Int,x$2: Int,x$3: Boolean,x$4: Boolean): Boolean = ???
    def unloadChunk(x$1: Int,x$2: Int,x$3: Boolean): Boolean = ???
    def unloadChunk(x$1: Int,x$2: Int): Boolean = ???
    def unloadChunk(x$1: org.bukkit.Chunk): Boolean = ???
    def unloadChunkRequest(x$1: Int,x$2: Int,x$3: Boolean): Boolean = ???
    def unloadChunkRequest(x$1: Int,x$2: Int): Boolean = ???
  }

  val server: Server = new Server {
    def getListeningPluginChannels(): java.util.Set[String] = ???
    def sendPluginMessage(x$1: org.bukkit.plugin.Plugin,x$2: String,x$3: Array[Byte]): Unit = ???
    // Members declared in org.bukkit.Server
    def addRecipe(x$1: org.bukkit.inventory.Recipe): Boolean = ???
    def banIP(x$1: String): Unit = ???
    def broadcast(x$1: String,x$2: String): Int = ???
    def broadcastMessage(x$1: String): Int = ???
    def clearRecipes(): Unit = ???
    def configureDbConfig(x$1: com.avaje.ebean.config.ServerConfig): Unit = ???
    def createInventory(x$1: org.bukkit.inventory.InventoryHolder,x$2: Int,x$3: String): org.bukkit.inventory.Inventory = ???
    def createInventory(x$1: org.bukkit.inventory.InventoryHolder,x$2: Int): org.bukkit.inventory.Inventory = ???
    def createInventory(x$1: org.bukkit.inventory.InventoryHolder,x$2: org.bukkit.event.inventory.InventoryType): org.bukkit.inventory.Inventory = ???
    def createMap(x$1: org.bukkit.World): org.bukkit.map.MapView = ???
    def createWorld(x$1: org.bukkit.WorldCreator): org.bukkit.World = ???
    def dispatchCommand(x$1: org.bukkit.command.CommandSender,x$2: String): Boolean = ???
    def getAllowEnd(): Boolean = ???
    def getAllowFlight(): Boolean = ???
    def getAllowNether(): Boolean = ???
    def getAnimalSpawnLimit(): Int = ???
    def getBannedPlayers(): java.util.Set[org.bukkit.OfflinePlayer] = ???
    def getBukkitVersion(): String = ???
    def getCommandAliases(): java.util.Map[String,Array[String]] = ???
    def getConnectionThrottle(): Long = ???
    def getConsoleSender(): org.bukkit.command.ConsoleCommandSender = ???
    def getDefaultGameMode(): org.bukkit.GameMode = ???
    def getGenerateStructures(): Boolean = ???
    def getHelpMap(): org.bukkit.help.HelpMap = ???
    def getIPBans(): java.util.Set[String] = ???
    def getIp(): String = ???
    def getLogger(): java.util.logging.Logger = ???
    def getMap(x$1: Short): org.bukkit.map.MapView = ???
    def getMaxPlayers(): Int = ???
    def getMessenger(): org.bukkit.plugin.messaging.Messenger = ???
    def getMonsterSpawnLimit(): Int = ???
    def getMotd(): String = ???
    def getName(): String = ???
    def getOfflinePlayer(x$1: String): org.bukkit.OfflinePlayer = ???
    def getOfflinePlayers(): Array[org.bukkit.OfflinePlayer] = ???
    def getOnlineMode(): Boolean = ???
    def getOnlinePlayers(): Array[org.bukkit.entity.Player] = ???
    def getOperators(): java.util.Set[org.bukkit.OfflinePlayer] = ???
    def getPlayer(x$1: String): org.bukkit.entity.Player = ???
    def getPlayerExact(x$1: String): org.bukkit.entity.Player = ???
    def getPluginCommand(x$1: String): org.bukkit.command.PluginCommand = ???
    def getPluginManager(): org.bukkit.plugin.PluginManager = ???
    def getPort(): Int = ???
    def getRecipesFor(x$1: org.bukkit.inventory.ItemStack): java.util.List[org.bukkit.inventory.Recipe] = ???
    def getScheduler(): org.bukkit.scheduler.BukkitScheduler = ???
    def getServerId(): String = ???
    def getServerName(): String = ???
    def getServicesManager(): org.bukkit.plugin.ServicesManager = ???
    def getSpawnRadius(): Int = ???
    def getTicksPerAnimalSpawns(): Int = ???
    def getTicksPerMonsterSpawns(): Int = ???
    def getUpdateFolder(): String = ???
    def getUpdateFolderFile(): java.io.File = ???
    def getVersion(): String = ???
    def getViewDistance(): Int = ???
    def getWarningState(): org.bukkit.Warning.WarningState = ???
    def getWaterAnimalSpawnLimit(): Int = ???
    def getWhitelistedPlayers(): java.util.Set[org.bukkit.OfflinePlayer] = ???
    def getWorld(x$1: java.util.UUID): org.bukkit.World = world
    def getWorld(x$1: String): org.bukkit.World = world
    def getWorldContainer(): java.io.File = ???
    def getWorldType(): String = ???
    def getWorlds(): java.util.List[org.bukkit.World] = new util.LinkedList[World](){ add(world) }
    def hasWhitelist(): Boolean = ???
    def isPrimaryThread(): Boolean = ???
    def matchPlayer(x$1: String): java.util.List[org.bukkit.entity.Player] = ???
    def recipeIterator(): java.util.Iterator[org.bukkit.inventory.Recipe] = ???
    def reload(): Unit = ???
    def reloadWhitelist(): Unit = ???
    def resetRecipes(): Unit = ???
    def savePlayers(): Unit = ???
    def setDefaultGameMode(x$1: org.bukkit.GameMode): Unit = ???
    def setSpawnRadius(x$1: Int): Unit = ???
    def setWhitelist(x$1: Boolean): Unit = ???
    def shutdown(): Unit = ???
    def unbanIP(x$1: String): Unit = ???
    def unloadWorld(x$1: org.bukkit.World,x$2: Boolean): Boolean = ???
    def unloadWorld(x$1: String,x$2: Boolean): Boolean = ???
    def useExactLoginLocation(): Boolean = ???
  }
 
  val origin: Location = new Location(world, 0, 4, 0)

  val player = new Player {
    def setWhitelisted(p1: Boolean) {}
    def setPassenger(p1: Entity): Boolean = true
    def getType: EntityType = EntityType.UNKNOWN
    def throwSnowball: Snowball = ???
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
    def sendRawMessage(p1: String) { println(s"raw message: $p1") }
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
    def getTargetBlock(p1: java.util.HashSet[java.lang.Byte], p2: Int): Block = origin.getBlock
    def removePotionEffect(p1: PotionEffectType) {}
    def setLevel(p1: Int) {}
    def setFlying(p1: Boolean) {}
    def setLastDamageCause(p1: EntityDamageEvent) {}
    def hasPermission(p1: Permission): Boolean = true
    def hasPermission(p1: String): Boolean = true
    def getFallDistance: Float = 0
    def showPlayer(p1: Player) {}
    def shootArrow: Arrow = ???
    def sendChunkChange(p1: Location, p2: Int, p3: Int, p4: Int, p5: Array[Byte]): Boolean = true
    def hasPlayedBefore: Boolean = true
    def setFoodLevel(p1: Int) {}
    def getWorld: World = getServer.getWorlds.get(0) // TODO: Important
    def sendBlockChange(p1: Location, p2: Int, p3: Byte) {}
    def sendBlockChange(p1: Location, p2: Material, p3: Byte) {}
    def setFallDistance(p1: Float) {}
    def getTicksLived: Int = 0
    def getPlayerTime: Long = 0
    def throwEgg: Egg = ???
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
    def sendPluginMessage(p1: Plugin, p2: String, p3: Array[Byte]) {
      println(s"plugin message: $p1, $p2, ${p3.mkString("")}")
    }
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
    def sendMessage(p1: Array[String]) { p1 foreach sendMessage }
    def sendMessage(p1: String) { println(p1) }
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
    def getEnderChest(): org.bukkit.inventory.Inventory = ???
    def playSound(x$1: org.bukkit.Location,x$2: org.bukkit.Sound,x$3: Float,x$4: Float): Unit = {}
  }
}


