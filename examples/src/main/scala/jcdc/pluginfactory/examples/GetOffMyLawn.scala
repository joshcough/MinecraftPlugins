package jcdc.pluginfactory.examples

import org.bukkit.event.player.PlayerMoveEvent
import jcdc.pluginfactory.{WorldEditCommands, Cube, Cubes, ListenerPlugin}

/**
 * Notifies you any time someone comes onto your lawn,
 * so that you can yell at them, kick them off, and shock them with lightning.
 *
 * Very similar in functionality to the Arena plugin in this same directory.
 */
class GetOffMyLawn extends ListenerPlugin with WorldEditCommands with Cubes {

  def movingOntoLawn(e:PlayerMoveEvent, lawn: Cube) =
    lawn.contains(e.getTo) && ! lawn.contains(e.getFrom)

  val listener = OnPlayerMove((p, e) =>
    for ((owner, lawn) <- cubes; if (p != owner && movingOntoLawn(e, lawn)))
      owner ! (s"${p.name} is on your lawn!")
  )

  val commands = List(
    pos1, // to set the first  corner of your lawn
    pos2, // to set the second corner of your lawn
    Command(
      name = "GetOffMyLawn",
      desc = "Kicks everyone off your lawn, and shocks them with lightning")(
      body = owner => run(owner)(lawn =>
        for(p <- lawn.players; if(p != owner)) {
          p.teleportTo(p.world.getHighestBlockAt(p.world(lawn.maxX + 5, 0, lawn.maxZ + 5).loc))
          p.shockWith(s"'Get off my lawn!!!', said ${owner.name}.")
        }
      )
    )
  )
}
