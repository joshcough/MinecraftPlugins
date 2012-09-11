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
      (parseAll(material, m.name).get == m) &&
      (parseAll(material, m.toString.toLowerCase).get == m) &&
      (parseAll(material, m.getId.toString).get == m)
    }
  }

  for(e <- EntityType.values){
    property(e.name) = secure {
      (parseAll(entity, e.name).get == e) &&
      (parseAll(entity, e.toString.toLowerCase).get == e)
    }
  }
}