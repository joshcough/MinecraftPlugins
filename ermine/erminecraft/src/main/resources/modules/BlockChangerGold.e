module BlockChangerGold where

import Minecraft.Minecraft

whenHitChangeTo : Material -> Listener
whenHitChangeTo m = onBlockDamage (b _ -> setType b m)

-- | Example listener
whenHitChangeToDiamond = whenHitChangeTo diamond

plugin = ErminePlugin [] [whenHitChangeToDiamond]
