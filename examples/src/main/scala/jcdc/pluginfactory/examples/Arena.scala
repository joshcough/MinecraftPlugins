package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{CommandsPlugin, ListenersPlugin}
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import Material._

class Arena extends ListenersPlugin with CommandsPlugin with CubePlugin with JCDCPluginFactoryExample {

  val listeners = List(
    OnLeftClickBlock((p, e) => if (p isHoldingA STONE_AXE) {
      setFirstPosition(p, e.getClickedBlock)
      e.cancel
    }),
    OnRightClickBlock((p, e) =>
      if (p isHoldingA STONE_AXE) setSecondPosition(p, e.getClickedBlock)),
    OnPlayerMove((p, e) => cubes.foreach{ case (cp, cube) =>
      if (cube.contains(e.getTo) && ! cube.contains(e.getFrom))
        p ! ("You are entering the battle arena of " + cp.name + "!")
    })
  )

  val commands = List(
    Command(
      name = "/axe",
      desc = "Get an Arena wand.",
      body = noArgs(p => p.world.dropItem(p.loc, new ItemStack(STONE_AXE, 1)))
    ),
    Command(
      name = "/arena",
      desc = "Set all the selected blocks to the given material type.",
      body = args(material) { case p ~ m => run(p)(cube => cube.blocks.foreach(b =>
        if (cube.onWall(b) || cube.onFloor(b)) b changeTo m else b changeTo AIR
      ))}
    )
  )
}
