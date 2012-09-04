package jcdc.pluginfactory

import org.scalacheck.Properties
import org.scalacheck.Prop._

object MinecraftParserTests extends Properties("MinecraftParserTests") with Compare {

  import MinecraftParsers._

  compare("player name description", player.describe, "player-name")
}