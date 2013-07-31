module Minecraft.Parsers where

import Bool
import Function
import Maybe
import Minecraft.Helpers
import Minecraft.Native
import Num
import Parsers.ParserCombinators
import Primitive
import Syntax.IO

-- Minecraft parsers
gamemode = (string "c" ^^^ creative) | (string "s" ^^^ survival)
material = maybeP "material-type" findMaterialByNameOrId
player : Parser (Player -> Maybe Player)
player = anyStringAs "player-name" ^^ (name p -> findPlayer (getServerFromPlayer p) name)
plugin : Parser (Player -> Maybe Plugin)
plugin = anyStringAs "plugin-name" ^^ (name p -> findPlugin (getPluginManagerFromPlayer p) name)
coordinates = double & double & opt double
length  = rename int "length"
height  = rename int "height"
depth   = rename int "depth"
time    = rename p "time" where
  p = filterWithP (i ->  i >= 0 && i <= 24000) (_ -> "time must be between 0 and 24000") int
location : Parser (World -> IO Location)
location = coordinates ^^ (c -> case c of
  ((x :& z) :& (Just y)) -> w -> return $ location# w x y z
  ((x :& z) :& Nothing)  -> w ->  getLocationFromBlock <$> (getHighestBlockAt w (toInt x) (toInt z))
)
