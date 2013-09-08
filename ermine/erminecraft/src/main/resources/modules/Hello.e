module Hello where

import Minecraft.Minecraft

commands = [
  Command "echo"    "Repeats what you say" slurp sendMessage,
  Command "hello"   "hello"   noArguments (p _ -> sendMessage p "hello back"),
  Command "goodbye" "goodbye" noArguments (p _ -> sendMessage p "see ya!!!!")
]

plugin = ErminePlugin commands []
