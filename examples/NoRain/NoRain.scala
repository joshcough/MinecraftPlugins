package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{ListenerPlugin, Listeners}
import Listeners._

class NoRain extends ListenerPlugin {
  val listener = OnWeatherChange(e => e.cancelIf(e.rain, broadcast("Put up an umbrella.")))
}