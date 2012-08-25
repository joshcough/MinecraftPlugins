package jcdc.pluginfactory.examples

import org.bukkit.Material
import Material._
import jcdc.pluginfactory.{CubePlugin, CommandsPlugin, ListenersPlugin}

class GetOffMyLawn extends ListenersPlugin with CommandsPlugin with CubePlugin {

  val listeners = List(
    OnPlayerMove((p, e) => cubes.foreach{ case (owner, lawn) =>
      if (p != owner and lawn.contains(e.getTo) and ! lawn.contains(e.getFrom))
        owner ! (p.name + " is on your lawn!")
    })
  )

  val commands = List(
    Command(
      name = "/pos1",
      desc = "Set the first corner of your lawn.",
      body = noArgs(p => setFirstPosition(p, p.loc))
    ),
    Command(
      name = "/pos2",
      desc = "Set the second corner of your lawn.",
      body = noArgs(p => setSecondPosition(p, p.loc))
    ),
    Command(
      name = "/GetOffMyLawn",
      desc = "Kick everyone off your lawn, and strikes them with lightning",
      body = noArgs(owner => run(owner)(c => c.players.filter(_ != owner).foreach{ p =>
        p.teleportTo(p.world.getHighestBlockAt(p.world(c.maxX + 5, 0, c.maxZ + 5)))
        p.strike
        p ! (s"'Get off my lawn!!!', said ${owner.name}.")
      }))
    )
  )
}
