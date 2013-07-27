module Minecraft.Parsers where

import Bool
import Function
import Maybe
import Parsers.ParserCombinators
import Primitive
import Minecraft.Helpers
import Minecraft.Native

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
location : Parser (World -> Location)
location = coordinates ^^ (c -> case c of
  ((x :& z) :& (Just y)) -> w -> location# w x y z
  ((x :& z) :& Nothing)  -> w -> _ --TODO: add getHighestBlockAt
)
--  case x ~ z ~ Some(y) => (w:World) => w(x, y, z).loc
--  case x ~ z ~ None    => (w:World) => w.getHighestBlockAt(x, z).loc