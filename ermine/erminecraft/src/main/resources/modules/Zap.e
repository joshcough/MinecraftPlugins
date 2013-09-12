module Zap where

import Minecraft.Minecraft
import Prelude

zap p = withWorld p (w -> getLocationFromPlayer p >>= strikeLightning w)

zapCommand = command "zap" "zap someone" playerArg (pInput playerToZapF ->
  discardMaybe $ fmap maybeFunctor zap (playerToZapF pInput)
)

plugin = ErminePlugin [zapCommand] []
