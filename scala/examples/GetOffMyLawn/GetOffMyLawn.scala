package com.joshcough.minecraft.examples

import org.bukkit.event.player.PlayerMoveEvent
import com.joshcough.minecraft.{Cube, CubeState, CommandsPlugin, ListenerPlugin}
import org.bukkit.block.Block

/**
 * Notifies you any time someone comes onto your lawn,
 * so that you can yell at them, kick them off, and shock them with lightning.
 *
 * Very similar in functionality to the Arena plugin in this same directory.
 */
class GetOffMyLawn extends ListenerPlugin with CommandsPlugin with CubeState {

  def movingOntoLawn(e:PlayerMoveEvent, lawn: Cube[Block]) =
    lawn.contains(e.getTo.point) && ! lawn.contains(e.getFrom.point)

  val listener = OnPlayerMove((p, e) =>
    for ((owner, lawn) <- cubes; if p != owner && movingOntoLawn(e, lawn))
      owner ! s"${p.name} is on your lawn!"
  )

  val commands = List(
    Command("/pos1",  "Set the first position") (p => setFirstPosition(p, p.loc)),
    Command("/pos2",  "Set the second position")(p => setSecondPosition(p, p.loc)),
    Command(
      name = "GetOffMyLawn",
      desc = "Kicks everyone off your lawn, and shocks them with lightning")(
      body = owner => {
        val lawn = cube(owner)
        for(p <- lawn.players; if(p != owner)) {
          p.teleportTo(p.world.getHighestBlockAt(p.world(lawn.maxX + 5, 0, lawn.maxZ + 5).loc))
          p shockWith s"'Get off my lawn!!!', said ${owner.name}."
        }
      }
    )
  )
}
