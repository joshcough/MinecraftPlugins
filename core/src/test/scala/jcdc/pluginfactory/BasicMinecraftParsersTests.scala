package jcdc.pluginfactory

import org.scalacheck.Properties
import org.scalacheck.Prop._
import org.bukkit.entity.{Player, EntityType}
import org.bukkit.Material

object BasicMinecraftParsersTests extends Properties("MinecraftParserTests") {

  import BasicMinecraftParsers._

  //property("player name description") = { player.describe ?= "player-name" }

  for(m <- Material.values){
    property(m.name) = secure {
      (material(List(m.name)).get == m) &&
      (material(List(m.toString.toLowerCase)).get == m) &&
      (material(List(m.getId.toString)).get == m)
    }
  }

  for(e <- EntityType.values){
    property(e.name) = secure {
      (entity(List(e.name)).get == e) &&
      (entity(List(e.toString.toLowerCase)).get == e)
    }
  }
}