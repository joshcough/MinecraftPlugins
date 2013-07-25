module Minecraft.Minecraft where

import Syntax.IO
import Native.Function
import Parsers.ParserCombinators

foreign
  data "org.bukkit.entity.Player" Player
  method "sendMessage" sendMessage : Player -> String -> IO ()
  method "getWorld"    getWorld : Player -> IO World
  method "getLocation" getLocation : Player -> IO Location

  data "org.bukkit.plugin.Plugin" Plugin
  method "getServer" getServer : Plugin -> Server

  data "org.bukkit.plugin.PluginManager" PluginManager
  method "getPlugin" getPlugin : PluginManager -> String -> Plugin
  method "registerEvents" registerEvents : PluginManager -> Listener -> Plugin -> IO ()

  data "org.bukkit.Server" Server
  method "getPlayer" getPlayer : Server -> String -> IO Player
  method "getPluginManager" getPluginManager : Server -> PluginManager

  data "org.bukkit.World" World
  method "strikeLightning" strikeLightning : World -> Location -> IO LightningStrike

  data "org.bukkit.Location" Location
  constructor location# : World -> Double -> Double -> Double -> Location

  data "org.bukkit.entity.LightningStrike" LightningStrike
  data "org.bukkit.event.Listener" Listener
  data "org.bukkit.event.block.BlockDamageEvent" BlockDamageEvent

  data "org.bukkit.Material" Material
  value "org.bukkit.Material" "GOLD_BLOCK" gold : Material
  value "org.bukkit.Material" "DIAMOND_BLOCK" diamond : Material

  data "org.bukkit.block.Block" Block
  method "setType" setType : Block -> Material -> ()

  function "jcdc.pluginfactory.ListenersObject" "OnBlockDamage" onBlockDamage# : Function2 Block BlockDamageEvent () -> Listener

zap p = getWorld p >>= (w -> getLocation p >>= strikeLightning w)

onBlockDamage f = onBlockDamage# (function2 f)


{--
case class Command(
    name: String,
    description: String,
    argsDescription: Option[String],
    body: (Player, List[String]) => Unit)
--}

data Command = Command String String (Player -> [String] -> IO ())
sayHiCommand = Command "hi" "Say hi" (p _ -> sendMessage p "hi")