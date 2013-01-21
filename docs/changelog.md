# 0.3.0

## Summary

 * Upgraded to Bukkit 1.4.7-R0.1
 * Upgraded to Scala 2.10.0

## Core Code Changes

 * Extensive Cube API additions
   * Changed Cube to be polymorphic
   * Got rid of Coor, and now just have a type alias Point = (Int, Int, Int)
   * Added Applicative Functor for Cube
   * Made shift operations be transformations on cubes.
   * Removed MinecraftCube, and placed its functions elsewhere.
    renamed Cube to CubeState,
   * Added some operators to PlayerState

## Bug Fixes/Example/Other Code Changes

 * World Edit changes
   * Implemented undo and redo, and made all commands work with it.
   * Properly reporting the number of blocks changed.
   * Implemented paste. It is monumental.
   * Renamed the /change command to /replace
   * Implemented flip and flipxy, which are both really fun.
   * Added ability to turn undo on and off.
   * Fixed the walls and move commands, they were broken
   * Fixed minor bug in #blocks-changed reporting
 * Fixed bugs in cube size code
 * Added scalacheck generators for cubes, and added lots of nice commutativity and other tests.

# 0.2.2

## Summary

 * split out the scala-library plugin from my own code. that might seem a little funny,
   but the scala code will change far less than mine, and users don't need to be downloading
   a 10 meg file every time i make changes.
 * upgraded to Bukkit 1.4.6-R0.3

## Core Code Changes

 * added corners function to Cube
 * added shell function to Cube, which gives back walls, floor, and ceiling.
 * added randomBoolen function to ScalaEnrichment
 * added length, height and depth parsers (all ints)
 * removed WorldEditCommands, because it's really not needed. i should figure out how to call commands from another plugin anyway, and just depend on worldedit instead.
 * added OnPlayerQuit Listener
 * renamed noArguments to eof, and added aliases noArguments, nothing, empty. not sure which of them i like or if i will keep them.
 * added byNameToRunnable implicit conversion in ScalaEnrichment
 * added new Task api
   * added functional wrappers on scheduleSyncTask, scheduleSyncDelayedTask, scheduleSyncRepeatingTask
   * added PlayerTasks class that:
     * also provides wrappers for creating tasks
     * keeps track of running tasks for players.
     * cancels them when the player leaves the game, if desired
     * examples in WorldEdits

## Bug Fixes

 * fixed error reporting in CommandsPlugin, it was broken.
 * fixed ~ (the parser) description, because it was completely broken
 * fixed a horrible bug in Option flipFold, and added tests for it.

## Example/Other Code Changes

 * refactored WorldEdit to use the Cubes trait, removed WorldEditCubes, and refactored the Cubes trait a bit. Refactored Arena, and GetOffMyLawn to be current with the new Cubes changes.
 * added random-house command, which changes all the blocks of the house to random materials (selected from materials the user typed in)
 * added awesome wave command which creates a wave with length ~ height ~ material (no depth)
 * added crazy-walls command to worldedit, to show off the task stuff. it has your walls change every second.
 * updated WorldEdit with the cycle-walls command to use the new task api. the command changes walls to different materials, repeatedly until the user logs out.
 * fixed up build scripts by adding publish-local.sh
 * implemented ParserMonad in Java. puke. renamed ArgParser to just Parser in Java
 * simplified WorldEditDemo a bit

# 0.2.1

## Core Code Changes

 * renamed BasicMinecraftParsers to MinecraftParsers
 * renamed/refactored EnrichmentClassses into BukkitEnrichment and ScalaEnrichment.
 * Changed the way commands work, they now take four arguments: name, description, args, body
   where before, args and body were merged together.
 * made Parser into a legit monad.
 * added filterWith to Parser, changed time parser to use it.

## Example/Other Code Changes

 * moved block changer to its own file and documented it.
 * changed BlockChanger parser to (material or noArguments), which has a
   little bit better error semantics than material.?
 * added Shock command example
 * added release script

## Documentation:

 * added scaladoc link to docs
 * added tiny shock command example (commands example) to main page
 * added bukkit.md, which is the main md for the plugin on bukkit.
 * documented parser combinator library, and cleaned it up a little.
 * added piles of other scaladoc to make sure it looks good in the generated html.
 * created http://joshcough.github.com/MinecraftPlugins/
 * created http://joshcough.github.com/MinecraftPlugins/scaladoc
