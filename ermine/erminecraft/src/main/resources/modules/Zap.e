module Zap where

import Minecraft.Minecraft

zap p = withWorld p (w -> getLocationFromPlayer p >>= strikeLightning w)

zapCommand = Command "zap" "zap someone" playerArg (pInput playerToZapF ->
  discardMaybe $ fmap maybeFunctor zap (playerToZapF pInput)
)

plugin = ErminePlugin [zapCommand] []
