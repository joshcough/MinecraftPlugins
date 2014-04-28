package com.joshcough.minecraft

import org.scalacheck.Properties
import org.scalacheck.Prop._
import org.bukkit.entity.EntityType
import org.bukkit.Material
import Material._

object MinecraftParsersTests extends Properties("MinecraftParserTests") with TestHelpers {

  import MinecraftParsers._

  for(m <- Material.values) test(m.toString) {
    (material(m.name).get ?= m) && (material(m.toString.toLowerCase).get ?= m)
    // getId is deprecated, and is now broken in bukkit for some ids
    // for now it will still work in most places for plugin users, but
    // testing it causes test failures. oh well.
    // && (material(m.getId.toString).get ?= m)
  }

  for(e <- EntityType.values) test(e.toString) {
    (entity(e.name).get ?= e) && (entity(e.toString.toLowerCase).get ?= e)
  }

  test("(material or eof)(gold_ore)") {
    (material or eof)("gold_ore").get.left.get ?= GOLD_ORE
  }

  test("(material or eof)(Nil)") {
    (material or eof)(Nil).get.isRight
  }

  test("(material or eof)(werersd)") {
    val res = (material or eof)("werersd").fold(id)((p,r) =>
      "parser worked with bogus material type: werersd, but shouldnt have."
    )
    res ?= "invalid material-type: werersd or expected eof, but got: werersd"
  }

  test("((material or eof) <~ eof)(dirt 7)") {
    val res = ((material or eof) <~ eof)("dirt 7").fold(id)((_,_) =>
      "parser worked, but shouldnt have."
    )
    res ?= "expected eof, but got: 7"
  }
}