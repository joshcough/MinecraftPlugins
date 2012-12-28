package jcdc.pluginfactory

import org.scalacheck.Properties
import org.scalacheck.Prop._
import org.bukkit.entity.EntityType
import org.bukkit.Material
import Material._

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

  test("(material or noArguments)(gold_ore)") {
    (material or noArguments)("gold_ore").get.left.get ?= GOLD_ORE
  }

  test("(material or noArguments)(Nil)") {
    (material or noArguments)(Nil).get.isRight
  }

  test("(material or noArguments)(werersd)") {
    val res = (material or noArguments)("werersd").fold(id)((p,r) => "parser worked, but shouldnt have.")
    res ?= "invalid material-type: werersd or unprocessed input: werersd"
  }
}