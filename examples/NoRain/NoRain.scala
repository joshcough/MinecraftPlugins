package com.joshcough.minecraft.examples

import com.joshcough.minecraft.{ListenerPlugin, Listeners}
import Listeners._

class NoRainPlugin extends ListenerPlugin {
  val listener = OnWeatherChange(e => e.cancelIf(e.rain, broadcast("Put up an umbrella.")))
}

object NoRainCommands {
  def main(args: Array[String]): Unit = {}
}
