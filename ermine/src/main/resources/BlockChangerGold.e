module BlockChangerGold where

import IO.Unsafe
import Minecraft

change = onBlockDamage (b _ -> setType b diamond)

-- Run this command:
--registerErmineListener BlockChangerGold change