package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{NPCPlugin, Command, CommandsPlugin}
import org.bukkit.entity.{EntityType, LivingEntity}

class NPCTest extends CommandsPlugin with NPCPlugin {

  val npcs = collection.mutable.Map[String, LivingEntity]()
  val ids = Iterator.from(0)
  // this parses an npc by its name.
  val npcTok = token("npc")(npcs.get)

  val commands = List(
    Command("npc", "Spawn an NPC", args(anyString) {
      case (p, s) =>
        npcs += (s -> human(s, p.loc))
    }),
    Command("npc:spawn", "Spawn an Entity", args(entity){ case (p, e) =>
      val id = ids.next.toString
      val npcEntity = e match {
        case EntityType.WOLF => wolf(id, p.loc)
      }
      npcs += (id -> npcEntity)
    }),
    Command("npc:fd", "Move an NPC forward", args(npcTok ~ num) {
      case (p, npc ~ distance) =>
        npc.teleport(p.world(npc.x + distance, npc.y, npc.z))
    }),
    Command("npc:tp", "Teleport an NPC", args(npcTok ~ num ~ num) {
      case (p, npc ~ x ~ z) =>
        npc.teleport(p.world(x, npc.y, z))
    }),
    Command("ncp:kill-npc", "Kill an NPC", args(npcTok) {
      case (p, npc) =>
        npcs -= npc.getEntityId.toString
        npc.die
    })
  )
}
