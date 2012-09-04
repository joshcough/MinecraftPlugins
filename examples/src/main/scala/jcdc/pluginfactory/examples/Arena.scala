package jcdc.pluginfactory.examples

import org.bukkit.Material
import Material._
import jcdc.pluginfactory.{Cubes, Command, CommandsPlugin, ListenersPlugin}

class Arena extends ListenersPlugin with CommandsPlugin with Cubes {

  val listeners = List(
    OnLeftClickBlock((p, e) => if (p isHoldingA STONE_AXE) {
      setFirstPosition(p, e.loc)
      e.cancel
    }),
    OnRightClickBlock((p, e) =>
      if (p isHoldingA STONE_AXE) setSecondPosition(p, e.loc)),
    OnPlayerMove((p, e) => for((cp, cube) <- cubes)
      if (cube.contains(e.getTo) and ! cube.contains(e.getFrom))
        p ! ("You are entering the battle arena of " + cp.name + "!")
      else if (cube.contains(e.getFrom) and ! cube.contains(e.getTo))
        p ! ("You are leaving the battle arena of " + cp.name + "!")
    )
  )

  val commands = List(
    Command(
      name = "axe",
      desc = "Get an Arena wand.",
      body = noArgs(_.loc.dropItem(STONE_AXE))
    ),
    Command(
      name = "arena",
      desc = "Set all the selected blocks to the given material type.",
      body = args(material) { case p ~ m => run(p){ cube =>
        for(b <- cube.blocks)
          if(cube.onWall(b) or cube.onFloor(b)) b changeTo m else b changeTo AIR
        () // shut up intellij
      }}
    )
  )
}
