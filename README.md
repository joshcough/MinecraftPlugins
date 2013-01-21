# Overview

This project contains an API for building Minecraft Plugins in Scala.
It is currently made up of several sub-projects, but the most important ones to know about are:

  * core             - The core API
  * microexample     - A first example of how to create a plugin in Scala.
  * examples         - Several more example plugins using the core API
  * scala-lib-plugin - Provides a Bukkit plugin that provides the Scala API to other plugins at runtime.

# Why?

There are several reasons for building this API, but the three that pop to mind immediately are:

  * To show how easy it is to put better/cleaner APIs on top of existing Java APIs.
  * To make command and argument parsing vastly easier using parser combinators.
  * To hopefully spark some interest in Scala among the Minecraft community.

Note: The core API is built on Bukkit version 1.4.7-R0.1 (http://bukkit.org/).

## Examples

### Listener example: LightningArrows

The following example demonstrates how to use listeners in Scala.
This plugin strikes an entity (any player or mob) with lightning
if that entity is hit with an arrow:

```scala
import jcdc.pluginfactory.Listeners._
import org.bukkit.entity.EntityType.ARROW

class LightningArrows extends ListeningFor(OnEntityDamageByEntity { e =>
  if (e.getDamager isAn ARROW) e.world.strikeLightning(e.loc)
})
```

### Commands example: Shock

The following example demonstrates how to write commands in Scala.
It has one command called "shock" which takes a player as input,
and shocks that player with lightning.

```scala
import jcdc.pluginfactory.CommandPlugin

class Shock extends CommandPlugin {
  val command = Command("shock", "shock a player", player){ case (you, them) =>
    them.shock
  }
}
```

## Other Stuff


#### Other subprojects

There are a few other sub projects and I'll mention them just very briefly here.

  * netlogo      - A plugin that enables running NetLogo models in Minecraft servers.
                   More info here: [NetLogo Plugin](https://github.com/joshcough/MinecraftPlugins/wiki/NetLogo)
  * minelang     - An ongoing attempt at writing my own language
                  (very much like Clojure, but not as good) that allows users to write their own
                  minecraft plugins without having to know anything about IDE's and Jar files and
                  stuff like that. I don't have any other information to give on this just yet, though.

#### Links

Important links:

  * [Project Website on GitHub Pages)](http://joshcough.github.com/MinecraftPlugins/)
  * [Project Scaladoc on GitHub Pages](http://joshcough.github.com/MinecraftPlugins/scaladoc)
  * [Project site on Bukkit](http://dev.bukkit.org/server-mods/scala-plugin-api/)
