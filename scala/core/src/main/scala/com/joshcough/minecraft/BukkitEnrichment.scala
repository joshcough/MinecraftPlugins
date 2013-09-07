package com.joshcough.minecraft

import org.bukkit.{ChatColor, Effect, Location, Material, OfflinePlayer, Server, World}
import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.{Plugin, PluginManager}
import ChatColor._
import Effect._
import Material._
import org.bukkit.entity.{LivingEntity, Entity, EntityType, Player}
import org.bukkit.event.player.PlayerInteractEvent
import util.Try
import Cube._


object BukkitEnrichment extends BukkitEnrichment{
  object MaterialAndData {
    val AIR = new MaterialAndData(Material.AIR, None)
  }
  case class MaterialAndData(m: Material, data: Option[Byte]){
    def update(b: Block): Boolean = {
      val oldM    = b.getType
      val oldData = b.getData
      b setType m
      data foreach b.setData
      oldM != m || oldData != data.getOrElse(0)
    }
    def itemStack: ItemStack = data.fold(new ItemStack(m))(new ItemStack(m, 1, 0:Short, _))
  }
}

/**
 * Adds piles of missing functions to Bukkit classes.
 *
 * This is all done using Scala 2.10 enrichment classes, which work like this:
 *
 * implicit class RichThing(t: Thing) {
 *   def someNewFunction = ...
 * }
 *
 * Where Thing is the class getting functions added to it. Any functions inside
 * of the RichClass are added to Thing (or available at compile time, anyway).
 *
 * No implicit conversions are used here. Everything is explicit, in order to keep sanity.
 * It would be easy to convert back and forth from say, a Block and a Location,
 * but instead I provide functions like b.loc, and l.block. While I think this provides
 * a good amount of sanity for the price of a little extra verbosity. This is especially true
 * because this trait is mixed into ScalaPlugin, meaning that every ScalaPlugin
 * has access to everything here. If there were a number of implicit conversions, things
 * could potentially get ugly fast.
 */
trait BukkitEnrichment extends ScalaEnrichment {

  import BukkitEnrichment._

  /**
   * Add a whole pile of awesomeness to Block.
   */
  implicit class RichBlock(b:Block) {

    // a pile of useful functions.
    lazy val world        = b.getWorld
    lazy val loc          = b.getLocation
    lazy val (x, y, z)    = (b.getX, b.getY, b.getZ)
    lazy val (xd, yd, zd) = (b.getX.toDouble, b.getY.toDouble, b.getZ.toDouble)
    lazy val chunk        = world.getChunkAt(b)
    lazy val blockNorth: Block = world(xd, yd, zd - 1)
    lazy val blockSouth: Block = world(xd, yd, zd + 1)
    lazy val blockEast : Block = world(xd + 1, yd, zd)
    lazy val blockWest : Block = world(xd - 1, yd, zd)
    lazy val blockNorthEast: Block = blockNorth.blockEast
    lazy val blockSouthEast: Block = blockSouth.blockEast
    lazy val blockNorthWest: Block = blockNorth.blockWest
    lazy val blockSouthWest: Block = blockSouth.blockWest

    def copy(x: Double = xd, y: Double = yd, z: Double = zd) = world(x, y, z)

    // the block directly above b
    lazy val blockAbove      = world(xd, yd + 1, zd)
    // the block directly below b
    lazy val blockBelow      = world(xd, yd - 1, zd)
    // the nth block above b
    def nthBlockAbove(n:Int) = world(xd, yd + n, zd)
    // the nth block below b
    def nthBlockBelow(n:Int) = world(xd, yd - n, zd)
    // a Stream of all the Blocks above b
    def blocksAbove   : Stream[Block] = blockAbove #:: blockAbove.blocksAbove
    // b, and all the blocks above b
    def andBlocksAbove: Stream[Block] = b #:: blocksAbove
    // a Stream of all the Blocks below b
    def blocksBelow   : Stream[Block] = blockBelow #:: blockBelow.blocksBelow
    // b, and all the blocks below b
    def andBlocksBelow: Stream[Block] = b #:: blocksBelow
    // the four blocks north, south, east and west of b
    def neighbors4: Stream[Block] =
      blockNorth #:: blockSouth #:: blockEast #:: blockWest #:: Stream.empty
    // b, and the four blocks north, south, east and west of b
    def andNeighbors4: Stream[Block] = b #:: neighbors4
    // the four blocks north, south, east and west of b
    // and the four blocks northeast, southeast, northwest, and southwest of b
    def neighbors8   : Stream[Block] = neighbors4 ++ (
      blockNorthEast #:: blockSouthEast #:: blockNorthWest #:: blockSouthWest #:: Stream.empty
    )
    // b and all of b's neighbors8
    def andNeighbors8: Stream[Block] = b #:: neighbors8

    /**
     * @return all of b's 26 neighbors in 3D space
     */
    def neighbors    : Stream[Block] =
      neighbors8 ++ b.blockBelow.andNeighbors8 #::: b.blockAbove.andNeighbors8

    /**
     * @return b, and all of b's 26 neighbors in 3D space
     */
    def andNeighbors : Stream[Block] = b #:: neighbors

    def is(m:Material)    = b.getType == m
    def isA(m:Material)   = b.getType == m
    def isNot(m:Material) = b.getType != m

    /**
     * drop the item for the current material of this block, and then set this block to AIR
     */
    def erase: Boolean = if(! (b is AIR)) {
      b.world.dropItem  (b.loc, b.itemStack)
      b.world.playEffect(b.loc, SMOKE, 1)
      changeTo(AIR)
    } else false

    /**
     * Change this block to the given material.
     */
    def changeTo(m: Material): Boolean = {
      try if(! chunk.isLoaded) chunk.load
      catch { case e: Exception => println("unable to load chunk.") }
      MaterialAndData(m, None) update b
    }

    def itemStack = new ItemStack(b.getType, 1, b.getData)
    def materialAndData = MaterialAndData(b.getType, Some(b.getData))

    def point: Point = Point(b.x, b.y, b.z)

    /**
     * Returns a Cube of all of the blocks between two locations of the world.
     */
    def cubeTo(b2: Block): Cube[Block] = b.loc.cubeTo(b2.loc)
  }

  /**
   * Add some awesomeness to Material.
   */
  implicit class RichMaterial(m: Material){
    def itemStack = new ItemStack(m, 1)
    def andData = MaterialAndData(m, None)
  }

  /**
   * Add some awesomeness to Cancellable.
   */
  implicit class RichCancellable(c:Cancellable){
    def cancel: Unit = c.setCancelled(true)
    def cancelIf(b: => Boolean, runBeforeCancelling: => Unit = () => ()): Unit =
      if(b) { runBeforeCancelling; c.setCancelled(true) }
  }

  /**
   * Add a bunch of awesomeness to Entity.
   */
  implicit class RichEntity(e:Entity){
    lazy val loc       = e.getLocation
    lazy val (x, y, z) = (loc.x, loc.y, loc.z)
    def server   = e.getServer
    def world    = e.getWorld
    def isAn(et:EntityType) = e.getType == et
    def isA (et:EntityType) = isAn(et)
    /**
     * Run f on e, if e is a Player
     */
    def whenPlayer(f: Player => Unit): Unit = if(e.isInstanceOf[Player]) f(e.asInstanceOf[Player])
    def shock = world strikeLightning loc
  }

  /**
   * Add some awesomeness to LivingEntity.
   */
  implicit class RichLivingEntity(e: LivingEntity){
    def die: Unit = e setHealth 0
  }

  /**
   * Add some awesomeness to ItemStack.
   */
  implicit class RichItemStack(i:ItemStack){
    def isA (m:Material) = i.getType == m
    def isAn(m:Material) = i.getType == m
  }

  /**
   * Add a whole pile of awesomeness to World.
   */
  implicit class RichWorld(w:World){
    def name = w.getName
    def entities = w.getEntities
    def apply(x: Int,    y: Int,    z: Int)   : Block = blockAt(x.toDouble, y.toDouble, z.toDouble)
    def apply(x: Double, y: Double, z: Double): Block = new Location(w, x, y, z).getBlock
    def apply(p: Point): Block = this(p.x, p.y, p.z)
    def blockAt(x: Int, y: Int, z: Int): Block = blockAt(x.toDouble, y.toDouble, z.toDouble)
    def blockAt(x: Double, y: Double, z: Double): Block = new Location(w, x, y, z).getBlock

    /**
     * Returns an infinite Stream[Block] that increases positively in X (or EAST)
     * starting at the given Location.
     */
    def fromX(loc:Location): Stream[Block] = {
      lazy val nats:Stream[Int] = 0 #:: 1 #:: nats.tail.map(_+1)
      for (x<-nats) yield w(loc.x + x, loc.y, loc.z)
    }
  }

  /**
   * Add a whole pile of awesomeness to Location.
   */
  implicit class RichLocation(loc: Location){
    lazy val (x,y,z)    = (loc.getX.toInt, loc.getY.toInt, loc.getZ.toInt)
    lazy val xyz        = (x, y, z)
    lazy val (xd,yd,zd) = (loc.getX, loc.getY, loc.getZ)
    lazy val xyzd       = (xd, yd, zd)
    def world           = loc.getWorld
    def block           = loc.getBlock
    def spawn(entityType:  EntityType): Unit = world.spawnCreature(loc, entityType)
    def spawnN(entityType: EntityType, n: Int): Unit = for (i <- 1 to n) spawn(entityType)
    def dropItem(stack: ItemStack): Unit = loc.world.dropItem(loc, stack)
    def dropItem(m: Material): Unit = dropItem(m.itemStack)
    def point: Point = Point(loc.x, loc.y, loc.z)
    /**
     * Returns a Cube of all of the blocks between two locations of the world.
     */
    def cubeTo(loc2: Location): Cube[Block] =
      Cube[Block](loc.point, loc2.point)((p: Point) => loc.world(p.x, p.y, p.z))
  }

  implicit class RichCubeOfBlocks(c: Cube[Block]) {
    import collection.JavaConversions.asScalaIterator
    def world = c(Point(0,0,0)).world
    def blocks = c.toStream
    def blocksAndMaterials = blocks.map(b => (b, b.materialAndData))
    def players: Iterator[Player] = world.getPlayers.iterator.filter(contains)
    def contains(p: Player)  : Boolean = c.contains(p.loc.point)
    def contains(l: Location): Boolean = c.contains(l.point)
  }

  /**
   * Add a whole pile of awesomeness to Server.
   */
  implicit class RichServer(s:Server){
    def findPlayer(name:String) = tryO(s.getPlayer(name))
    def findOnlinePlayer = findPlayer _
    def findOfflinePlayer(name:String) = Option(s.getOfflinePlayer(name))
    def findOnlinePlayers(names: List[String]): List[Player] = names.map(findOnlinePlayer).flatten
    def findOfflinePlayers(names: List[String]): List[OfflinePlayer] =
      names.map(findOfflinePlayer).flatten
  }

  /**
   * Add a whole pile of awesomeness to Player.
   */
  implicit class RichPlayer(player:Player){
    def loc    = player.getLocation
    def name   = player.getName
    def world  = player.getWorld
    def server = player.getServer

    def inventory = player.getInventory
    def is(pname: String) = name == pname

    def holding = player.getItemInHand
    def isHolding  (m: Material) = player.getItemInHand.getType == m
    def isHoldingA (m: Material) = isHolding(m)
    def isHoldingAn(m: Material) = isHolding(m)
    def isHoldingAnyOf(ms: Material*) = ms.exists(isHolding)

    def blockOn         = player.loc.block.blockBelow
    def blockAboveHead  = blockOn.nthBlockAbove(3)
    def blocksAboveHead = blockAboveHead.blocksAbove

    /**
     * If this player were in a box, this function would return all the blocks in that box
     * @return
     */
    def blocksAround: Stream[Block] =
      blockOn.nthBlockAbove(1).neighbors8 ++  // 8 blocks at the bottom half of the player
      blockOn.nthBlockAbove(2).neighbors8 ++  // 8 blocks at the top half of the player
      blockOn.andNeighbors8 #:::              // 9 blocks below the player
      blockOn.nthBlockAbove(3).andNeighbors8  // 9 blocks above the player.

    /**
     * Sends player a message.
     */
    def !  (s:  String): Unit  = if(s != null) player.sendMessage(s)

    /**
     * Sends player all of the given messages
     */
    def !* (ss: String*): Unit = ss.foreach(s => player ! s)

    /**
     * Sends the player the given message, but turns it red.
     */
    def sendError(message:String): Unit = player.sendMessage(RED(message))

    /**
     * Send the player an error message, and then throw an exception violently.
     */
    def bomb(message:String): Nothing = {
      player ! RED(message)
      throw new RuntimeException(message)
    }

    /**
     * Brings the player UP to the top of the world (but at the same NSEW coordinate).
     */
    def surface: Unit = teleportTo(world getHighestBlockAt loc)

    // just a ton of utility functions that i don't feel like documenting

    def findPlayer(name:String)(f: Player => Unit): Unit =
      server.findPlayer(name).fold(sendError("kill could not find player: " + name))(f)
    def findPlayers(names:List[String])(f: Player => Unit): Unit = names.foreach(n => findPlayer(n)(f))
    def ban(reason:String){ player.setBanned(true); player.kickPlayer("banned: $reason") }
    def kill(playerName:String): Unit   = findPlayer(playerName)(kill)
    def kill(p:Player): Unit            = doTo(p, p.setHealth(0), "killed")
    def teleportTo(otherPlayer: Player) = player.teleport(otherPlayer)
    def teleportTo(b: Block): Unit      = player.teleport(b.loc)
    def shockWith(message:String) {
      player.shock
      player ! message
    }
    def withMaterial[T](nameOrId:String)(f: Material => T) {
      attemptO(findMaterial(nameOrId))("No such material: $nameOrId", f)
    }
    def attemptO[T, U](ot: Option[T])(s: => String, f: T => U){
      ot.fold(player ! s)(t => f(t))
    }
    def attempt[T](f: => T): Unit = try f catch {
      case e: Exception => player ! RED(s"$e ${e.getMessage}\n${e.getStackTraceString}")
    }

    def doTo(otherPlayer: Player, f: => Unit, actionName: String){
      f
      otherPlayer  ! GREEN(s"you have been $actionName by ${player.name}")
      player       ! GREEN(s"you have $actionName ${otherPlayer.name}")
    }
  }

  /**
   * Add some awesomeness to EntityDamageByEntityEvent.
   */
  implicit class RichEntityDamageByEntityEvent(e: EntityDamageByEntityEvent) {
    def damager = e.getDamager
    def damagee = e.getEntity
  }

  /**
   * Add some awesomeness to PlayerInteractEvent.
   */
  implicit class RichPlayerInteractEvent(e: PlayerInteractEvent) {
    def block = e.getClickedBlock
    def loc = block.loc
  }

  /**
   * Add some awesomeness to WeatherChangeEvent.
   */
  implicit class RichWeatherChangeEvent(e:WeatherChangeEvent) {
    def rain = e.toWeatherState
    def sun  = ! rain
  }

  implicit class RichPluginManager(pm: PluginManager) {
    def findPlugin(name: String): Option[Plugin] = tryO(pm.getPlugin(name))
    def enable(plugin: String)      : Unit = findPlugin(plugin).foreach(pm.enablePlugin)
    def disable(plugin: String)     : Unit = findPlugin(plugin).foreach(pm.disablePlugin)
    def enableAll(plugins: String*) : Unit = plugins.foreach(enable)
    def disableAll(plugins: String*): Unit = plugins.foreach(disable)
  }

  // arguably, these functions should be someplace else...
  def tryO[T](f: => T): Option[T] = Try(Option(f)).getOrElse(None)

  def findEntity(name:String) = Option(EntityType.fromName(name.toUpperCase)).orElse(
    Option(EntityType.valueOf(name.toUpperCase))
  )

  def findMaterial(nameOrId: String) = Option(getMaterial(nameOrId.toUpperCase)).orElse(
    tryO(getMaterial(nameOrId.toInt))
  )

  implicit class RichColor(c: ChatColor) {
    def apply(s: String) = c + s
  }

  sealed case class Color(data:Byte){
    def wool = MaterialAndData(WOOL, Some(data))
  }

  object Color {
    val WHITE       = new Color(0)
    val ORANGE      = new Color(1)
    val MAGENTA     = new Color(2)
    val LIGHT_BLUE  = new Color(3)
    val YELLOW      = new Color(4)
    val LIGHT_GREEN = new Color(5)
    val PINK        = new Color(6)
    val GREY        = new Color(7)
    val LIGHT_GREY  = new Color(8)
    val CYAN        = new Color(9)
    val VIOLET      = new Color(10)
    val BLUE        = new Color(11)
    val BROWN       = new Color(12)
    val GREEN       = new Color(13)
    val RED         = new Color(14)
    val BLACK       = new Color(15)
  }
}

