package jcdc.pluginfactory

import org.scalacheck.Properties
import org.scalacheck.Prop._
import org.bukkit.entity.EntityType
import org.bukkit.Material

object BasicMinecraftParsersTests extends Properties("MinecraftParserTests") with TestHelpers {

  import BasicMinecraftParsers._

  for(m <- Material.values)
    test(m.name) {
      (material(List(m.name)).get == m) &&
      (material(List(m.toString.toLowerCase)).get == m) &&
      (material(List(m.getId.toString)).get == m)
    }

  for(e <- EntityType.values)
    test(e.name) {
      (entity(List(e.name)).get == e) &&
      (entity(List(e.toString.toLowerCase)).get == e)
    }
}