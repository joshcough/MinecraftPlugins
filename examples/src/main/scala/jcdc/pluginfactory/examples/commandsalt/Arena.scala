package jcdc.pluginfactory.examples.commandsalt

import org.bukkit.Material
import Material._
import org.bukkit.event.player.PlayerMoveEvent
import jcdc.pluginfactory.{Cube, Cubes, ListenersPlugin}
import jcdc.pluginfactory.commandsalt.CommandsPlugin

/**
 * The goal of this plugin was to have combatants enter an arena and
 * then fight to the death, but I never finished it.
 *
 * As of now, it does allow you to create arenas, and it notifies
 * players when they are entering and exiting an arena. This could
 * be a good start for a Trap plugin, or any other kind of thing
 * you might want to do when a player enters a particular area.
 */
class Arena extends ListenersPlugin with CommandsPlugin with Cubes {

  val listeners = List(
    // classic world-edit style corner setters
    OnLeftClickBlock((p, e) => if (p isHoldingA STONE_AXE) {
      setFirstPosition(p, e.loc)
      e.cancel
    }),
    OnRightClickBlock((p, e) =>
      if (p isHoldingA STONE_AXE) setSecondPosition(p, e.loc)),

    /**
     * The fun motion detection is here, and this is where you might plug in something awesome.
     */
    OnPlayerMove((p, e) => for((cp, cube) <- cubes)
      if (entering(cube, e))     p ! ("You are entering the battle arena of " + cp.name + "!")
      else if (leaving(cube, e)) p ! ("You are leaving the battle arena of "  + cp.name + "!")
    )
  )

  val commands = List(
    Command(name = "axe", desc = "Get an Arena wand.")(_.loc.dropItem(STONE_AXE)),
    // this isnt really needed, but it makes things easier to see in the game
    // try doing it, and then boucing in and out of the arena.
    Command(
      name = "make-arena",
      desc = "Set the walls and floor to the given material type.",
      args = material)(
      body = { case (p, m) => run(p){ cube =>
        for(b <- cube.blocks)
          if(cube.onWall(b) or cube.onFloor(b)) b changeTo m else b changeTo AIR
      }}
    )
  )

  def entering(c: Cube, e: PlayerMoveEvent) = c.contains(e.getTo)   and ! c.contains(e.getFrom)
  def leaving (c: Cube, e: PlayerMoveEvent) = c.contains(e.getFrom) and ! c.contains(e.getTo)
}
