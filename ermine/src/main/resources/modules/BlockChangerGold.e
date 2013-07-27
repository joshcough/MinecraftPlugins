module BlockChangerGold where

import Minecraft.Minecraft

change = onBlockDamage (b _ -> setType b diamond)

-- Run this command:
--registerErmineListener BlockChangerGold change