package jcdc.pluginfactory

import org.scalacheck.Properties
import org.scalacheck.Prop._
import org.bukkit.entity.{Player, EntityType}
import org.bukkit.Material

object MinecraftParserTests extends Properties("MinecraftParserTests") {

  import MinecraftParsers._

  val unusedPlayer: Player = null

  property("player name description") = { player.describe ?= "player-name" }

  for(m <- Material.values){
    property(m.name) = secure {
      (material(unusedPlayer, List(m.name)).get == m) &&
      (material(unusedPlayer, List(m.toString.toLowerCase)).get == m) &&
      (material(unusedPlayer, List(m.getId.toString)).get == m)
    }
  }

  for(e <-EntityType.values){
    property(e.name) = secure {
      (entity(unusedPlayer, List(e.name)).get ?= e) &&
      (entity(unusedPlayer, List(e.toString.toLowerCase)).get == e)
    }
  }
}