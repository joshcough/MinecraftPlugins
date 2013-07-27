module Minecraft.Helpers where

import Control.Monad
import Function
import Maybe
import Native.Function
import Native.Object
import Parse
import Minecraft.Native

findEntityType : String -> Maybe EntityType
findEntityType name = orMaybe (liftNull (entityFromName n)) (liftNull (entityValueOf n)) where n = toUpperCase name

findMaterialById   : Int -> Maybe Material
findMaterialById   = ffiToMaybe . getMaterialById#
findMaterialByName : String -> Maybe Material
findMaterialByName = ffiToMaybe . getMaterialByName#
findMaterialByNameOrId : String -> Maybe Material
findMaterialByNameOrId nameOrId = orMaybe
  (findMaterialByName $ toUpperCase nameOrId)
  (bind maybeMonad (parseInt 10 nameOrId) findMaterialById)

findPlayer : Server -> String -> Maybe Player
findPlayer server name = ffiToMaybe (getPlayer server name)

findPlugin: PluginManager -> String -> Maybe Plugin
findPlugin pm name = ffiToMaybe (getPlugin# pm name)

getPluginManagerFromPlayer = getPluginManager . getServerFromPlayer

