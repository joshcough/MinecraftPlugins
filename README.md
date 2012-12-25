# Overview

This project contains an API for building Minecraft Plugins in Scala. It is currently made up of several sub-projects, but the most important ones to know about are:

  * core         - The main API
  * microexample - A first example of how to create a plugin in Scala.
  * examples     - Several more example plugins using the core API
  
There are several reasons for building this API, but the three that pop to mind immediately are:

  * To show how easy it is to put better/cleaner APIs on top of existing Java APIs.
  * To make command and argument parsing vastly easier using parser combinators.
  * To hopefully spark some interest in Scala among the Minecraft community.

Note: The core API is built on Bukkit version 1.2.5-R1 (http://bukkit.org/). 

## Examples

While there will be much more written soon in this section, here is an example that demonstrates how amazingly
simple it is to write Minecraft plugins using this API. The following example demonstrates how to strike
an entity (any player or mob) with lightning if that entity is hit with an arrow:

```scala
import jcdc.pluginfactory.Listeners._
import org.bukkit.entity.EntityType.ARROW

class LightningArrows extends ListeningFor(OnEntityDamageByEntity { e =>
  if (e.getDamager isAn ARROW) e.world.strikeLightning(e.loc)
})
```

## Other Stuff

There are a few other sub projects and I'll mention them just very briefly here.

  * netlogo      - A plugin that enables running NetLogo models in Minecraft servers. More info here: [NetLogo Plugin](https://github.com/joshcough/MinecraftPlugins/wiki/NetLogo)
  * minelang     - An ongoing attempt at writing my own language (very much like Clojure, but not as good) that allows users to write their own minecraft plugins without having to know anything about IDE's and Jar files and stuff like that. I don't have any other information to give on this just yet, though.


