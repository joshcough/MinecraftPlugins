package jcdc.pluginfactory.examples

import org.bukkit.craftbukkit.entity.CraftPlayer
import jcdc.pluginfactory.{NPCPlugin, CommandsPlugin}

class NPC extends CommandsPlugin with NPCPlugin {

  val npcs = collection.mutable.Map[String, CraftPlayer]()
  val npcTok = token("npc"){ (_, s) => npcs.get(s) }

  val commands = List(
    Command("npc:spawn", "Spawn an NPC", args(anyString){ case p ~ s =>
      npcs += (s -> NPCHuman(p.world, p.loc, s))
    }),
    Command("npc:fd", "Move an NPC forward", args(npcTok ~ num){ case p ~ (npc ~ distance) =>
      npc.teleport(p.world(npc.x + distance, npc.y, npc.z))
    }),
    Command("npc:tp", "Teleport an NPC", args(npcTok ~ num ~ num){ case p ~ (npc ~ x ~ z) =>
      npc.teleport(p.world(x, npc.y, z))
    }),
    Command("ncp:kill-npc", "Kill an NPC", args(npcTok){ case p ~ npc =>
      npcs -= npc.name
      p.kill(npc)
    })
  )
}
