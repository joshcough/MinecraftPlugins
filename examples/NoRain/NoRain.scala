package com.joshcough.minecraft.examples

import com.joshcough.minecraft._
import Listeners._

class NoRainPlugin extends ListenerPlugin {
  val listener = OnWeatherChange(e => e.cancelIf(e.rain, broadcast("Put up an umbrella.")))
}

class NoRainConfig extends PluginConfig[NoRainPlugin] {
  val pluginClass = classOf[NoRainPlugin]
}
