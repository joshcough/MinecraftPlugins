module Minecraft where

import Syntax.IO

foreign
  data "org.bukkit.entity.Player" Player
  method "sendMessage" sendMessage : Player -> String -> IO ()
  method "getWorld"    getWorld : Player -> IO World
  method "getLocation" getLocation : Player -> IO Location

  data "org.bukkit.Server" Server
  method "getPlayer" getPlayer : Server -> String -> IO Player

  data "org.bukkit.World" World
  method "strikeLightning" strikeLightning : World -> Location -> IO LightningStrike

  data "org.bukkit.Location" Location
  constructor location# : World -> Double -> Double -> Double -> Location

  data "org.bukkit.entity.LightningStrike" LightningStrike

zap p = getWorld p >>= (w -> getLocation p >>= strikeLightning w)
