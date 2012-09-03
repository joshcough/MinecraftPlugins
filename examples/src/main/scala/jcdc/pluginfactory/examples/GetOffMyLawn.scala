package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{Cubes, Command, CommandsPlugin, CommonCommands, ListenerPlugin}
import CommonCommands._

class GetOffMyLawn extends ListenerPlugin with CommandsPlugin with Cubes {

  val listener = OnPlayerMove((p, e) => cubes.foreach{ case (owner, lawn) =>
    if (p != owner and lawn.contains(e.getTo) and ! lawn.contains(e.getFrom))
      owner ! (p.name + " is on your lawn!")
  })

  val commands = List(pos1, pos2, Command(
    name = "/GetOffMyLawn",
    desc = "Kick everyone off your lawn, and strikes them with lightning",
    body = noArgs(owner => run(owner)(lawn => lawn.players.filter(_ != owner).foreach { p =>
      p.teleportTo(p.world.getHighestBlockAt(p.world(lawn.maxX + 5, 0, lawn.maxZ + 5)))
      p.strike
      p ! (s"'Get off my lawn!!!', said ${owner.name}.")
    }))
  ))
}
