package com.joshcough.minecraft.examples

import com.joshcough.minecraft.{CommandPlugin, ListenerPlugin}
import org.bukkit.entity.Player

// this same plugin exists in microexamples, and elsewhere, but that's ok.
class God extends ListenerPlugin with CommandPlugin {
  val gods = collection.mutable.Map[Player, Boolean]().withDefaultValue(false)
  val listener = OnPlayerDamage { (p, e) => e cancelIf gods(p) }
  val command = Command(
    name = "god", desc = "Toggle God mode.")(
    body = p => {
      gods.update(p, ! gods(p))
      p ! s"god mode ${if(gods(p)) "enabled" else "disabled"}"
    }
  )
}