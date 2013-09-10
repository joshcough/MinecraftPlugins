module Goto where

import Minecraft.Minecraft

gotoCommand = command "goto" "Teleport" loc (p floc -> discard $ withWorld p (w -> floc w >>= (teleport p)))

plugin = ErminePlugin [gotoCommand] []
