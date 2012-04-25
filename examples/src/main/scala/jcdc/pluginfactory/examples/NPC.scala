package jcdc.pluginfactory.examples

import jcdc.pluginfactory.CommandsPlugin
import org.bukkit.entity.Player
import net.minecraft.server.{MinecraftServer, EntityPlayer, World => MCWorld, ItemInWorldManager}
import org.bukkit.craftbukkit.CraftServer

/**
 * helpful urls:
 * http://dev.bukkit.org/server-mods/npccreatures/
 * https://github.com/Steveice10/NPCCreatures/blob/master/src/main/java/ch/spacebase/npccreatures/npcs/NPCManager.java
 * https://github.com/Steveice10/NPCCreatures/blob/master/src/main/java/ch/spacebase/npccreatures/npcs/nms/NPCNetHandler.java
 * https://github.com/CitizensDev/Citizens/blob/master/src/core/net/citizensnpcs/resources/npclib/CraftNPC.java
 * https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/net/minecraft/server/Entity.java
 */
class NPC extends CommandsPlugin {

  val commands = List(
    Command("spawn", "Spawn a human", noArgs(spawnNPC))
  )

  def spawnNPC(p:Player): Unit = {
    val h = NPCHuman(p.world.worldServer, "joshcough")
    h.setLocation(p.x + 2, p.y, p.z + 2, 0, 0)
    p.world.worldServer.addEntity(h)
  }
}

object NPCHuman{
  def server = org.bukkit.Bukkit.getServer.asInstanceOf[CraftServer].getHandle.server
  def apply(world: MCWorld, name:String) = {
    new NPCHuman(server, world, name, new ItemInWorldManager(world))
  }
}

class NPCHuman(server: MinecraftServer, world: MCWorld, name:String, im: ItemInWorldManager)
  extends EntityPlayer(server, world, name, im) {
  im.b(1)
  // this was in the npccreatures code. looks like it might be needed
  //this.netServerHandler = new NPCNetHandler(((NPCCreatures) Bukkit.getServer().getPluginManager().getPlugin("NPCCreatures")).getNPCManager(), this)
  override def move(arg0: Double, arg1: Double, arg2: Double) { setPosition(arg0, arg1, arg2) }
}

//
//import net.minecraft.server.EntityPlayer
//import net.minecraft.server.NetServerHandler
//import net.minecraft.server.Packet
//import net.minecraft.server.Packet101CloseWindow
//import net.minecraft.server.Packet102WindowClick
//import net.minecraft.server.Packet106Transaction
//import net.minecraft.server.Packet10Flying
//import net.minecraft.server.Packet130UpdateSign
//import net.minecraft.server.Packet14BlockDig
//import net.minecraft.server.Packet15Place
//import net.minecraft.server.Packet16BlockItemSwitch
//import net.minecraft.server.Packet18ArmAnimation
//import net.minecraft.server.Packet19EntityAction
//import net.minecraft.server.Packet255KickDisconnect
//import net.minecraft.server.Packet3Chat
//import net.minecraft.server.Packet7UseEntity
//import net.minecraft.server.Packet9Respawn
//import org.bukkit.Bukkit
//import org.bukkit.craftbukkit.CraftServer
//import org.bukkit.craftbukkit.entity.CraftPlayer
//
//import ch.spacebase.npccreatures.npcs.NPCManager
//
///**
// *
// * @author martin
// */
//public class NPCNetHandler extends NetServerHandler {
//
//  public NPCNetHandler(NPCManager npcManager, EntityPlayer entityplayer) {
//    super(npcManager.getServer().getMCServer(), npcManager.getNPCNetworkManager(), entityplayer)
//  }
//
//  @Override
//  public CraftPlayer getPlayer() {
//    return new CraftPlayer((CraftServer) Bukkit.getServer(), player) // Fake
//    // player
//    // prevents
//    // spout
//    // NPEs
//  }
//
//  override def a() {
//  }
//
//  override def a(Packet10Flying packet10flying) {
//  }
//
//  override def sendMessage(String s) {
//  }
//
//  override def a(double d0, double d1, double d2, float f, float f1) {
//  }
//
//  override def a(Packet14BlockDig packet14blockdig) {
//  }
//
//  override def a(Packet15Place packet15place) {
//  }
//
//  override def a(String s, Object[] aobject) {
//  }
//
//  override def onUnhandledPacket(Packet packet) {
//  }
//
//  override def a(Packet16BlockItemSwitch packet16blockitemswitch) {
//  }
//
//  override def a(Packet3Chat packet3chat) {
//  }
//
//  override def a(Packet18ArmAnimation packet18armanimation) {
//  }
//
//  override def a(Packet19EntityAction packet19entityaction) {
//  }
//
//  override def a(Packet255KickDisconnect packet255kickdisconnect) {
//  }
//
//  override def sendPacket(Packet packet) {
//  }
//
//  override def a(Packet7UseEntity packet7useentity) {
//  }
//
//  override def a(Packet9Respawn packet9respawn) {
//  }
//
//  override def handleContainerClose(Packet101CloseWindow packet101closewindow) {
//  }
//
//  override def a(Packet102WindowClick packet102windowclick) {
//  }
//
//  override def a(Packet106Transaction packet106transaction) {
//  }
//
//  override def a(Packet130UpdateSign packet130updatesign) {
//  }
//
//}