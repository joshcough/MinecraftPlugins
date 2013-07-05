Provides a library for writing server plugins in Scala. The plugins are vastly simpler to write than using the original Bukkit API in Java.

**_Update_** - This project now provides two plugins:

  * scala-library-plugin-2.10.2.jar
  * scala-plugin-api_2.10-0.3.0.jar

The first plugin provides Scala 2.10.2, so that plugins that depend on it can have scala on the classpath.

The second is an entire Scala Minecraft Server Plugin API that cleans up the Bukkit API very nicely, and allows for writing clean, declarative, immutable, and fun plugins.

The source for the project can be found here:
[https://github.com/joshcough/MinecraftPlugins](https://github.com/joshcough/MinecraftPlugins).

## Examples

### Listener example: LightningArrows

The following example demonstrates how to use listeners in Scala.
This plugin strikes an entity (any player or mob) with lightning if that entity is hit with an arrow:

    import jcdc.pluginfactory.Listeners._
    import org.bukkit.entity.EntityType.ARROW

    class LightningArrows extends ListeningFor(OnEntityDamageByEntity { e =>
      if (e.getDamager isAn ARROW) e.world.strikeLightning(e.loc)
    })

### Commands example: Shock

The following example demonstrates how to write commands in Scala.
It has one command called "shock" which takes a player as input, and shocks that player with lightning.

    import jcdc.pluginfactory.CommandPlugin

    class Shock extends CommandPlugin {
      val command = Command("shock", "shock a player", player){
        case (you, them) => them.shock
      }
    }

## Other features

* plugin.yml is automatically generated for you.
* Advanced cube api for 3D world manipulation
* Cleaner database connectivity
* Cleaner logging api

## Getting Started

A complete guide to getting started writing Scala plugins can be found here:
[https://github.com/joshcough/MinecraftPlugins/wiki/Getting%20Started](https://github.com/joshcough/MinecraftPlugins/wiki/Getting%20Started)

## More examples

Many, many more examples can be found here:
[https://github.com/joshcough/MinecraftPlugins/tree/master/examples/src/main/scala/jcdc/pluginfactory/examples](https://github.com/joshcough/MinecraftPlugins/tree/master/examples/src/main/scala/jcdc/pluginfactory/examples),
including Scala versions of WorldEdit, Multi Player Commands (a plugin that supports many of the
same commands as Single Player Commands), Warp, and more.

## Scaladoc

The Scaladoc for the project can be found here:

[http://joshcough.github.com/MinecraftPlugins/scaladoc](http://joshcough.github.com/MinecraftPlugins/scaladoc)