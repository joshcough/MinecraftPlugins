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
