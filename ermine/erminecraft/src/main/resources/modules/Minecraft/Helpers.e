module Minecraft.Helpers where

import Control.Monad
import Function
import Maybe
import Minecraft.Native
import Native.Function
import Native.Object
import Num
import Parse
import Syntax.IO

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

location : World -> Int -> Int -> Int -> Location
location w x y z = location# w (toDouble x) (toDouble y) (toDouble z)

-- misc stuff
onBlockDamage f = onBlockDamage# (function2 f)
withWorld p f = getWorldFromPlayer p >>= f

discard : IO a -> IO ()
discard io = (_ -> ()) <$> io

discardMaybe : Maybe (IO LightningStrike) -> IO ()
discardMaybe = maybe (return ()) discard
