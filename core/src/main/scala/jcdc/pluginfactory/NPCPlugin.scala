package jcdc.pluginfactory

import org.bukkit.Location
import ch.spacebase.npccreatures.npcs.NPCType
import ch.spacebase.npccreatures.npcs.entity.NPC
import ch.spacebase.npccreatures.NPCCreatures

/**
* helpful urls:
* http://dev.bukkit.org/server-mods/npccreatures/
* https://github.com/Steveice10/NPCCreatures/blob/master/src/main/java/ch/spacebase/npccreatures/npcs/NPCManager.java
* https://github.com/Steveice10/NPCCreatures/blob/master/src/main/java/ch/spacebase/npccreatures/npcs/nms/NPCNetHandler.java
* https://github.com/CitizensDev/Citizens/blob/master/src/core/net/citizensnpcs/resources/npclib/CraftNPC.java
* https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/net/minecraft/server/Entity.java
*/
trait NPCPlugin extends CommandsPlugin {
  private lazy val npcManager =
    server.getPluginManager.getPlugin("NPCCreatures").asInstanceOf[NPCCreatures].getNPCManager
  def npcType = token("npc-type"){ s => Option(NPCType.getByName(s)) }

  def despawn(npc:NPC){ npcManager.despawnById(npc.getNPCId) }
  private def spawn(name:String, loc: Location, npcType:NPCType): NPC =
    this.npcManager.spawnNPC(name, loc,npcType)

  def human(name: String, loc:Location)       = spawn(name, loc, NPCType.HUMAN)
  def wolf (name: String, loc:Location)       = spawn(name, loc, NPCType.WOLF)
  def sheep(name: String, loc:Location)       = spawn(name, loc, NPCType.SHEEP)
  def villager(name: String, loc:Location)    = spawn(name, loc, NPCType.VILLAGER)
  def zombie(name: String, loc:Location)      = spawn(name, loc, NPCType.ZOMBIE)
  def spider(name: String, loc:Location)      = spawn(name, loc, NPCType.SPIDER)
  def skeleton(name: String, loc:Location)    = spawn(name, loc, NPCType.SKELETON)
  def creeper(name: String, loc:Location)     = spawn(name, loc, NPCType.CREEPER)
  def enderman(name: String, loc:Location)    = spawn(name, loc, NPCType.ENDERMAN)
  def blaze(name: String, loc:Location)       = spawn(name, loc, NPCType.BLAZE)
  def magmacube(name: String, loc:Location)   = spawn(name, loc, NPCType.MAGMACUBE)
  def pigzombie(name: String, loc:Location)   = spawn(name, loc, NPCType.PIGZOMBIE)
  def cavespider(name: String, loc:Location)  = spawn(name, loc, NPCType.CAVESPIDER)
  def slime(name: String, loc:Location)       = spawn(name, loc, NPCType.SLIME)
  def silverfish(name: String, loc:Location)  = spawn(name, loc, NPCType.SILVERFISH)
  def snowman(name: String, loc:Location)     = spawn(name, loc, NPCType.SNOWMAN)
  def pig(name: String, loc:Location)         = spawn(name, loc, NPCType.PIG)
  def chicken(name: String, loc:Location)     = spawn(name, loc, NPCType.CHICKEN)
  def cow(name: String, loc:Location)         = spawn(name, loc, NPCType.COW)
  def squid(name: String, loc:Location)       = spawn(name, loc, NPCType.SQUID)
  def mooshroom(name: String, loc:Location)   = spawn(name, loc, NPCType.MOOSHROOM)
  def enderdragon(name: String, loc:Location) = spawn(name, loc, NPCType.ENDERDRAGON)
  def ghast(name: String, loc:Location)       = spawn(name, loc, NPCType.GHAST)
  def giant(name: String, loc:Location)       = spawn(name, loc, NPCType.GIANT)
  def ocelot(name: String, loc:Location)      = spawn(name, loc, NPCType.OCELOT)
  def irongolem(name: String, loc:Location)   = spawn(name, loc, NPCType.IRONGOLEM)

  def spawn(id:Long, loc: Location, breed: String): NPC = (breed.toLowerCase match {
    case "wolves"       | "wolf"       => wolf  _
    case "sheep"                       => sheep _
    case "villagers"    | "villager"   => villager _
    case "zombies"      | "zombie"     => zombie _
    case "spiders"      | "spider"     => spider _
    case "skeletons"    | "skeleton"   => skeleton _
    case "creepers"     | "creeper"    => creeper _
    case "endermen"     | "enderman"   => enderman _
    case "blazes"       | "blaze"      => blaze _
    case "magmacubes"   | "magmacube"  => magmacube _
    case "pigzombies"   | "pigzombie"  => pigzombie _
    case "cavespiders"  | "cavespider" => cavespider _
    case "slimes"       | "slime"      => slime _
    case "silverfish"                  => silverfish _
    case "snowmen"      | "snowman"    => snowman _
    case "pigs"         | "pig"        => pig _
    case "chickens"     | "chicken"    => chicken _
    case "cows"         | "cow"        => cow _
    case "squids"       | "squid"      => squid _
    case "mooshrooms"   | "mooshroom"  => mooshroom _
    case "ghasts"       | "ghast"      => ghast _
    case "giants"       | "giant"      => giant _
    case "ocelots"      | "ocelot"     => ocelot _
    case "irongolems"   | "irongolem" | "golems" | "golem"     => irongolem _
    case "enderdragons" | "enderdragon" | "dragons" | "dragon" => enderdragon _
    case _        => human _
  })(id.toString, loc)
}
