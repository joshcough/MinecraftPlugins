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
object TestServer { ts =>

  def getBlock(x:Int, y:Int, z:Int): Block = new Block {
    def getType: Material = Material.STONE
    def setTypeIdAndData(p1: Int, p2: Byte, p3: Boolean): Boolean = true
    def getLightFromSky: Byte = 0:Byte
    def setTypeId(p1: Int, p2: Boolean): Boolean = true
    def setTypeId(p1: Int): Boolean = true
    def getY: Int = y
    def getData: Byte = 0:Byte
    def getPistonMoveReaction: PistonMoveReaction = sys error "todo"
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
    def getBiome: Biome = sys error "todo"
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
    def getState: BlockState = sys error "todo"
    def getWorld: World = world
    def removeMetadata(p1: String, p2: Plugin) {}
    def getZ: Int = z
    def isEmpty: Boolean = true
    def getDrops(p1: ItemStack): util.Collection[ItemStack] = new util.LinkedList[ItemStack]()
    def getDrops: util.Collection[ItemStack] = new util.LinkedList[ItemStack]()
    def getLocation: Location = new Location(world, x, y, z)
    def isBlockFaceIndirectlyPowered(p1: BlockFace): Boolean = false
    def getChunk: Chunk = chunk
    def breakNaturally(p1: ItemStack): Boolean = false
    def breakNaturally(): Boolean = false
    def getTemperature: Double = 0d
    def getFace(p1: Block): BlockFace = sys error "todo"
    def isBlockPowered: Boolean = false
  }

  val chunk: Chunk = new Chunk {
    def getEntities: Array[Entity] = Array()
    def unload(): Boolean = true
    def unload(p1: Boolean): Boolean = true
    def unload(p1: Boolean, p2: Boolean): Boolean = true
    def getTileEntities: Array[BlockState] = Array()
    def isLoaded: Boolean = true
    def getChunkSnapshot(p1: Boolean, p2: Boolean, p3: Boolean): ChunkSnapshot = sys error "todo"
    def getChunkSnapshot: ChunkSnapshot = sys error "todo"
    def getX: Int = sys error "todo"
    def load(): Boolean = true
    def load(p1: Boolean): Boolean = p1
    def getBlock(x: Int, y: Int, z: Int): Block = ts.getBlock(x,y,z)
    def getWorld: World = world
    def getZ: Int = sys error "todo"
  }

  val world: World = new JavaTestWorld {
    def getMetadata(x$1: String): java.util.List[org.bukkit.metadata.MetadataValue] = sys error "todo"
    def hasMetadata(x$1: String): Boolean = sys error "todo"
    def removeMetadata(x$1: String,x$2: org.bukkit.plugin.Plugin): Unit = sys error "todo"
    def setMetadata(x$1: String,x$2: org.bukkit.metadata.MetadataValue): Unit = sys error "todo"
    // Members declared in org.bukkit.plugin.messaging.PluginMessageRecipient
    def getListeningPluginChannels(): java.util.Set[String] = sys error "todo"
    def sendPluginMessage(x$1: org.bukkit.plugin.Plugin,x$2: String,x$3: Array[Byte]): Unit = sys error "todo"
    // Members declared in org.bukkit.World
    def canGenerateStructures(): Boolean = sys error "todo"
    def createExplosion(x$1: org.bukkit.Location,x$2: Float,x$3: Boolean): Boolean = sys error "todo"
    def createExplosion(x$1: org.bukkit.Location,x$2: Float): Boolean = sys error "todo"
    def createExplosion(x$1: Double,x$2: Double,x$3: Double,x$4: Float,x$5: Boolean): Boolean = sys error "todo"
    def createExplosion(x$1: Double,x$2: Double,x$3: Double,x$4: Float): Boolean = sys error "todo"
    def dropItem(x$1: org.bukkit.Location,x$2: org.bukkit.inventory.ItemStack): org.bukkit.entity.Item = sys error "todo"
    def dropItemNaturally(x$1: org.bukkit.Location,x$2: org.bukkit.inventory.ItemStack): org.bukkit.entity.Item = sys error "todo"
    def generateTree(x$1: org.bukkit.Location,x$2: org.bukkit.TreeType,x$3: org.bukkit.BlockChangeDelegate): Boolean = sys error "todo"
    def generateTree(x$1: org.bukkit.Location,x$2: org.bukkit.TreeType): Boolean = sys error "todo"
    def getAllowAnimals(): Boolean = sys error "todo"
    def getAllowMonsters(): Boolean = sys error "todo"
    def getAnimalSpawnLimit(): Int = sys error "todo"
    def getBiome(x$1: Int,x$2: Int): org.bukkit.block.Biome = sys error "todo"
    def getBlockAt(loc: org.bukkit.Location): org.bukkit.block.Block = getBlock(loc.x, loc.y, loc.z)
    def getBlockAt(x: Int,y: Int,z: Int): org.bukkit.block.Block = getBlock(x, y, z)
    def getBlockTypeIdAt(x$1: org.bukkit.Location): Int = sys error "todo"
    def getBlockTypeIdAt(x$1: Int,x$2: Int,x$3: Int): Int = sys error "todo"
    def getChunkAt(x$1: org.bukkit.block.Block): org.bukkit.Chunk = chunk
    def getChunkAt(x$1: org.bukkit.Location): org.bukkit.Chunk = chunk
    def getChunkAt(x$1: Int,x$2: Int): org.bukkit.Chunk = chunk
    def getDifficulty(): org.bukkit.Difficulty = sys error "todo"
    def getEmptyChunkSnapshot(x$1: Int,x$2: Int,x$3: Boolean,x$4: Boolean): org.bukkit.ChunkSnapshot = sys error "todo"
    def getEntities(): java.util.List[org.bukkit.entity.Entity] = sys error "todo"
    def getEntitiesByClass[T <: Entity](p1: Class[T]): util.Collection[T] = sys error "todo"
    def getEnvironment(): org.bukkit.World.Environment = sys error "todo"
    def getFullTime(): Long = sys error "todo"
    def getGenerator(): org.bukkit.generator.ChunkGenerator = sys error "todo"
    def getHighestBlockAt(loc: org.bukkit.Location): org.bukkit.block.Block = getBlock(loc.x, 4, loc.z)
    def getHighestBlockAt(x: Int,z: Int): org.bukkit.block.Block = getBlock(x,4,z)
    def getHighestBlockYAt(x$1: org.bukkit.Location): Int = 4
    def getHighestBlockYAt(x$1: Int,x$2: Int): Int = 4
    def getHumidity(x$1: Int,x$2: Int): Double = sys error "todo"
    def getKeepSpawnInMemory(): Boolean = sys error "todo"
    def getLivingEntities(): java.util.List[org.bukkit.entity.LivingEntity] = sys error "todo"
    def getLoadedChunks(): Array[org.bukkit.Chunk] = Array(chunk)
    def getMaxHeight(): Int = sys error "todo"
    def getMonsterSpawnLimit(): Int = sys error "todo"
    def getName(): String = "test-world"
    def getPVP(): Boolean = sys error "todo"
    def getPlayers(): java.util.List[org.bukkit.entity.Player] = sys error "todo"
    def getPopulators(): java.util.List[org.bukkit.generator.BlockPopulator] = sys error "todo"
    def getSeaLevel(): Int = sys error "todo"
    def getSeed(): Long = sys error "todo"
    def getSpawnLocation(): org.bukkit.Location = sys error "todo"
    def getTemperature(x$1: Int,x$2: Int): Double = sys error "todo"
    def getThunderDuration(): Int = sys error "todo"
    def getTicksPerAnimalSpawns(): Long = sys error "todo"
    def getTicksPerMonsterSpawns(): Long = sys error "todo"
    def getTime(): Long = sys error "todo"
    def getUID(): java.util.UUID = sys error "todo"
    def getWaterAnimalSpawnLimit(): Int = sys error "todo"
    def getWeatherDuration(): Int = sys error "todo"
    def getWorldFolder(): java.io.File = sys error "todo"
    def getWorldType(): org.bukkit.WorldType = sys error "todo"
    def hasStorm(): Boolean = sys error "todo"
    def isAutoSave(): Boolean = sys error "todo"
    def isChunkInUse(x$1: Int,x$2: Int): Boolean = true
    def isChunkLoaded(x$1: Int,x$2: Int): Boolean = true
    def isChunkLoaded(x$1: org.bukkit.Chunk): Boolean = true
    def isThundering(): Boolean = sys error "todo"
    def loadChunk(x$1: Int,x$2: Int,x$3: Boolean): Boolean = true
    def loadChunk(x$1: Int,x$2: Int): Unit = {}
    def loadChunk(x$1: org.bukkit.Chunk): Unit = {}
    def playEffect[T](x$1: org.bukkit.Location,x$2: org.bukkit.Effect,x$3: T,x$4: Int): Unit = sys error "todo"
    def playEffect[T](x$1: org.bukkit.Location,x$2: org.bukkit.Effect,x$3: T): Unit = sys error "todo"
    def playEffect(x$1: org.bukkit.Location,x$2: org.bukkit.Effect,x$3: Int,x$4: Int): Unit = sys error "todo"
    def playEffect(x$1: org.bukkit.Location,x$2: org.bukkit.Effect,x$3: Int): Unit = sys error "todo"
    def playSound(x$1: org.bukkit.Location,x$2: org.bukkit.Sound,x$3: Float,x$4: Float): Unit = sys error "todo"
    def refreshChunk(x$1: Int,x$2: Int): Boolean = true
    def regenerateChunk(x$1: Int,x$2: Int): Boolean = true
    def save(): Unit = sys error "todo"
    def setAnimalSpawnLimit(x$1: Int): Unit = sys error "todo"
    def setAutoSave(x$1: Boolean): Unit = sys error "todo"
    def setBiome(x$1: Int,x$2: Int,x$3: org.bukkit.block.Biome): Unit = sys error "todo"
    def setDifficulty(x$1: org.bukkit.Difficulty): Unit = sys error "todo"
    def setFullTime(x$1: Long): Unit = sys error "todo"
    def setKeepSpawnInMemory(x$1: Boolean): Unit = sys error "todo"
    def setMonsterSpawnLimit(x$1: Int): Unit = sys error "todo"
    def setPVP(x$1: Boolean): Unit = sys error "todo"
    def setSpawnFlags(x$1: Boolean,x$2: Boolean): Unit = sys error "todo"
    def setSpawnLocation(x$1: Int,x$2: Int,x$3: Int): Boolean = sys error "todo"
    def setStorm(x$1: Boolean): Unit = sys error "todo"
    def setThunderDuration(x$1: Int): Unit = sys error "todo"
    def setThundering(x$1: Boolean): Unit = sys error "todo"
    def setTicksPerAnimalSpawns(x$1: Int): Unit = sys error "todo"
    def setTicksPerMonsterSpawns(x$1: Int): Unit = sys error "todo"
    def setTime(x$1: Long): Unit = sys error "todo"
    def setWaterAnimalSpawnLimit(x$1: Int): Unit = sys error "todo"
    def setWeatherDuration(x$1: Int): Unit = sys error "todo"
    def spawn[T <: org.bukkit.entity.Entity](x$1: org.bukkit.Location,x$2: Class[T]): T = sys error "todo"
    def spawnArrow(x$1: org.bukkit.Location,x$2: org.bukkit.util.Vector,x$3: Float,x$4: Float): org.bukkit.entity.Arrow = sys error "todo"
    def spawnCreature(x$1: org.bukkit.Location,x$2: org.bukkit.entity.CreatureType): org.bukkit.entity.LivingEntity = sys error "todo"
    def spawnCreature(x$1: org.bukkit.Location,x$2: org.bukkit.entity.EntityType): org.bukkit.entity.LivingEntity = sys error "todo"
    def spawnEntity(x$1: org.bukkit.Location,x$2: org.bukkit.entity.EntityType): org.bukkit.entity.Entity = sys error "todo"
    def spawnFallingBlock(x$1: org.bukkit.Location,x$2: Int,x$3: Byte): org.bukkit.entity.FallingBlock = sys error "todo"
    def spawnFallingBlock(x$1: org.bukkit.Location,x$2: org.bukkit.Material,x$3: Byte): org.bukkit.entity.FallingBlock = sys error "todo"
    def strikeLightning(x$1: org.bukkit.Location): org.bukkit.entity.LightningStrike = sys error "todo"
    def strikeLightningEffect(x$1: org.bukkit.Location): org.bukkit.entity.LightningStrike = sys error "todo"
    def unloadChunk(x$1: Int,x$2: Int,x$3: Boolean,x$4: Boolean): Boolean = sys error "todo"
    def unloadChunk(x$1: Int,x$2: Int,x$3: Boolean): Boolean = true
    def unloadChunk(x$1: Int,x$2: Int): Boolean = true
    def unloadChunk(x$1: org.bukkit.Chunk): Boolean = true
    def unloadChunkRequest(x$1: Int,x$2: Int,x$3: Boolean): Boolean = true
    def unloadChunkRequest(x$1: Int,x$2: Int): Boolean = true
  }

  val server: Server = new Server {
    def getListeningPluginChannels(): java.util.Set[String] = sys error "todo"
    def sendPluginMessage(x$1: org.bukkit.plugin.Plugin,x$2: String,x$3: Array[Byte]): Unit = sys error "todo"
    // Members declared in org.bukkit.Server
    def addRecipe(x$1: org.bukkit.inventory.Recipe): Boolean = sys error "todo"
    def banIP(x$1: String): Unit = sys error "todo"
    def broadcast(x$1: String,x$2: String): Int = sys error "todo"
    def broadcastMessage(x$1: String): Int = sys error "todo"
    def clearRecipes(): Unit = sys error "todo"
    def configureDbConfig(x$1: com.avaje.ebean.config.ServerConfig): Unit = sys error "todo"
    def createInventory(x$1: org.bukkit.inventory.InventoryHolder,x$2: Int,x$3: String): org.bukkit.inventory.Inventory = sys error "todo"
    def createInventory(x$1: org.bukkit.inventory.InventoryHolder,x$2: Int): org.bukkit.inventory.Inventory = sys error "todo"
    def createInventory(x$1: org.bukkit.inventory.InventoryHolder,x$2: org.bukkit.event.inventory.InventoryType): org.bukkit.inventory.Inventory = sys error "todo"
    def createMap(x$1: org.bukkit.World): org.bukkit.map.MapView = sys error "todo"
    def createWorld(x$1: org.bukkit.WorldCreator): org.bukkit.World = sys error "todo"
    def dispatchCommand(x$1: org.bukkit.command.CommandSender,x$2: String): Boolean = sys error "todo"
    def getAllowEnd(): Boolean = sys error "todo"
    def getAllowFlight(): Boolean = sys error "todo"
    def getAllowNether(): Boolean = sys error "todo"
    def getAnimalSpawnLimit(): Int = sys error "todo"
    def getBannedPlayers(): java.util.Set[org.bukkit.OfflinePlayer] = sys error "todo"
    def getBukkitVersion(): String = sys error "todo"
    def getCommandAliases(): java.util.Map[String,Array[String]] = sys error "todo"
    def getConnectionThrottle(): Long = sys error "todo"
    def getConsoleSender(): org.bukkit.command.ConsoleCommandSender = sys error "todo"
    def getDefaultGameMode(): org.bukkit.GameMode = sys error "todo"
    def getGenerateStructures(): Boolean = sys error "todo"
    def getHelpMap(): org.bukkit.help.HelpMap = sys error "todo"
    def getIPBans(): java.util.Set[String] = sys error "todo"
    def getIp(): String = sys error "todo"
    def getLogger(): java.util.logging.Logger = sys error "todo"
    def getMap(x$1: Short): org.bukkit.map.MapView = sys error "todo"
    def getMaxPlayers(): Int = sys error "todo"
    def getMessenger(): org.bukkit.plugin.messaging.Messenger = sys error "todo"
    def getMonsterSpawnLimit(): Int = sys error "todo"
    def getMotd(): String = "yo dawgs"
    def getName(): String = "test-server"
    def getOfflinePlayer(x$1: String): org.bukkit.OfflinePlayer = sys error "todo"
    def getOfflinePlayers(): Array[org.bukkit.OfflinePlayer] = sys error "todo"
    def getOnlineMode(): Boolean = false
    def getOnlinePlayers(): Array[org.bukkit.entity.Player] = Array(player)
    def getOperators(): java.util.Set[org.bukkit.OfflinePlayer] = new java.util.HashSet[org.bukkit.OfflinePlayer](){ add(player) }
    def getPlayer(name: String): org.bukkit.entity.Player = if (name==player.name) player else sys.error("no player: " + name)
    def getPlayerExact(x$1: String): org.bukkit.entity.Player = getPlayer(x$1)
    def getPluginCommand(x$1: String): org.bukkit.command.PluginCommand = sys error "todo"
    def getPluginManager(): org.bukkit.plugin.PluginManager = sys error "todo"
    def getPort(): Int = sys error "todo"
    def getRecipesFor(x$1: org.bukkit.inventory.ItemStack): java.util.List[org.bukkit.inventory.Recipe] = sys error "todo"
    def getScheduler(): org.bukkit.scheduler.BukkitScheduler = sys error "todo"
    def getServerId(): String = sys error "todo"
    def getServerName(): String = sys error "todo"
    def getServicesManager(): org.bukkit.plugin.ServicesManager = sys error "todo"
    def getSpawnRadius(): Int = sys error "todo"
    def getTicksPerAnimalSpawns(): Int = sys error "todo"
    def getTicksPerMonsterSpawns(): Int = sys error "todo"
    def getUpdateFolder(): String = sys error "todo"
    def getUpdateFolderFile(): java.io.File = sys error "todo"
    def getVersion(): String = sys error "todo"
    def getViewDistance(): Int = sys error "todo"
    def getWarningState(): org.bukkit.Warning.WarningState = sys error "todo"
    def getWaterAnimalSpawnLimit(): Int = sys error "todo"
    def getWhitelistedPlayers(): java.util.Set[org.bukkit.OfflinePlayer] = sys error "todo"
    def getWorld(x$1: java.util.UUID): org.bukkit.World = world
    def getWorld(x$1: String): org.bukkit.World = world
    def getWorldContainer(): java.io.File = sys error "todo"
    def getWorldType(): String = sys error "todo"
    def getWorlds(): java.util.List[org.bukkit.World] = new util.LinkedList[World](){ add(world) }
    def hasWhitelist(): Boolean = sys error "todo"
    def isPrimaryThread(): Boolean = sys error "todo"
    def matchPlayer(x$1: String): java.util.List[org.bukkit.entity.Player] = sys error "todo"
    def recipeIterator(): java.util.Iterator[org.bukkit.inventory.Recipe] = sys error "todo"
    def reload(): Unit = sys error "todo"
    def reloadWhitelist(): Unit = sys error "todo"
    def resetRecipes(): Unit = sys error "todo"
    def savePlayers(): Unit = sys error "todo"
    def setDefaultGameMode(x$1: org.bukkit.GameMode): Unit = sys error "todo"
    def setSpawnRadius(x$1: Int): Unit = sys error "todo"
    def setWhitelist(x$1: Boolean): Unit = sys error "todo"
    def shutdown(): Unit = sys error "todo"
    def unbanIP(x$1: String): Unit = sys error "todo"
    def unloadWorld(x$1: org.bukkit.World,x$2: Boolean): Boolean = sys error "todo"
    def unloadWorld(x$1: String,x$2: Boolean): Boolean = sys error "todo"
    def useExactLoginLocation(): Boolean = sys error "todo"
  }
 
  val origin: Location = new Location(world, 0, 4, 0)

  val player = new Player {
    var x = 0d
    var y = 4d
    var z = 0d
    def setWhitelisted(p1: Boolean) {}
    def setPassenger(p1: Entity): Boolean = true
    def getType: EntityType = EntityType.UNKNOWN
    def throwSnowball: Snowball = sys error "todo"
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
    def launchProjectile[T <: Projectile](p1: Class[_ <: T]): T = sys error "todo"
    def hasMetadata(p1: String): Boolean = false
    def setFlySpeed(p1: Float) {}
    def getName: String = getServer.getName
    def getMaxFireTicks: Int = 0
    def getBedSpawnLocation: Location = origin
    def isInsideVehicle: Boolean = false
    def getInventory: PlayerInventory = sys error "todo"
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
    def openEnchanting(p1: Location, p2: Boolean): InventoryView = sys error "todo"
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
    def openInventory(p1: Inventory): InventoryView = sys error "todo"
    def playNote(p1: Location, p2: Instrument, p3: Note) {}
    def playNote(p1: Location, p2: Byte, p3: Byte) {}
    def setItemInHand(p1: ItemStack) {}
    def getWalkSpeed: Float = 0
    def addAttachment(p1: Plugin, p2: Int): PermissionAttachment = sys error "todo"
    def addAttachment(p1: Plugin, p2: String, p3: Boolean, p4: Int): PermissionAttachment = sys error "todo"
    def addAttachment(p1: Plugin): PermissionAttachment = sys error "todo"
    def addAttachment(p1: Plugin, p2: String, p3: Boolean): PermissionAttachment = sys error "todo"
    def getUniqueId: java.util.UUID = sys error "todo"
    def hasLineOfSight(p1: Entity): Boolean = false
    def openWorkbench(p1: Location, p2: Boolean): InventoryView = sys error "todo"
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
    def shootArrow: Arrow = sys error "todo"
    def sendChunkChange(p1: Location, p2: Int, p3: Int, p4: Int, p5: Array[Byte]): Boolean = true
    def hasPlayedBefore: Boolean = true
    def setFoodLevel(p1: Int) {}
    def getWorld: World = getServer.getWorlds.get(0) // TODO: Important
    def sendBlockChange(p1: Location, p2: Int, p3: Byte) {}
    def sendBlockChange(p1: Location, p2: Material, p3: Byte) {}
    def setFallDistance(p1: Float) {}
    def getTicksLived: Int = 0
    def getPlayerTime: Long = 0
    def throwEgg: Egg = sys error "todo"
    def setDisplayName(p1: String) {}
    def resetPlayerTime {}
    def getActivePotionEffects: java.util.Collection[PotionEffect] = new java.util.LinkedList()
    def teleport(p1: Entity, p2: TeleportCause): Boolean = teleport(p1.loc)
    def teleport(p1: Entity): Boolean = teleport(p1.loc)
    def teleport(l: Location, c: TeleportCause): Boolean = teleport(l)
    def teleport(l: Location): Boolean = { x = l.xd; y = l.yd; z = l.zd; true }
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
    def getOpenInventory: InventoryView = sys error "todo"
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
    def getAddress: InetSocketAddress = sys error "todo"
    def getExhaustion: Float = 0
    def getLocation: Location = new Location(world, x, y, z)
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
    def getVelocity: Vector = sys error "todo"
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
    def getEnderChest(): org.bukkit.inventory.Inventory = sys error "todo"
    def playSound(x$1: org.bukkit.Location,x$2: org.bukkit.Sound,x$3: Float,x$4: Float): Unit = {}
  }
}


