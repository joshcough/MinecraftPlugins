module Minecraft.Native where

import Either
import IO.Unsafe
import Native.Function

-- all the foreign imports for Bukkit code
foreign
  data "org.bukkit.entity.EntityType" EntityType
  function "org.bukkit.entity.EntityType" "fromName" entityFromName : String -> EntityType
  function "org.bukkit.entity.EntityType" "valueOf"  entityValueOf  : String -> EntityType

  data "org.bukkit.entity.Player" Player
  method "sendMessage" sendMessage : Player -> String -> IO ()
  method "getWorld"    getWorld : Player -> IO World
  method "getLocation" getLocation : Player -> IO Location
  method "getServer"   getServerFromPlayer : Player -> Server

  data "org.bukkit.plugin.Plugin" Plugin
  method "getServer" getServerFromPlugin : Plugin -> Server

  data "org.bukkit.plugin.PluginManager" PluginManager
  method "getPlugin" getPlugin# : PluginManager -> String -> FFI Plugin
  method "registerEvents" registerEvents : PluginManager -> Listener -> Plugin -> IO ()

  data "org.bukkit.Server" Server
  method "getPlayer" getPlayer : Server -> String -> FFI Player
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
  function "org.bukkit.Material" "getMaterial" getMaterialById# : Int -> FFI Material
  function "org.bukkit.Material" "getMaterial" getMaterialByName# : String -> FFI Material

  data "org.bukkit.block.Block" Block
  method "setType" setType : Block -> Material -> ()

  function "jcdc.pluginfactory.ListenersObject" "OnBlockDamage" onBlockDamage# : Function2 Block BlockDamageEvent () -> Listener

  data "org.bukkit.GameMode" GameMode
  value "org.bukkit.GameMode" "CREATIVE" creative : GameMode
  value "org.bukkit.GameMode" "SURVIVAL" survival : GameMode


--TODO move to String
foreign
  method "toUpperCase" toUpperCase : String -> String

--TODO: move to IO.Unsafe
ffiToMaybe : FFI a -> Maybe a
ffiToMaybe f = case unsafeFFI f of
  Left e  -> Nothing
  Right a -> Just a
