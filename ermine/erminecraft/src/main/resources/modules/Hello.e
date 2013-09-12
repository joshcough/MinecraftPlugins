module Hello where

import Minecraft.Minecraft

commands = [
  command "echo"    "Repeats what you say" (rename slurp "anything") sendMessage,
  command "hello"   "hello"   noArguments (p _ -> sendMessage p "hello back"),
  command "goodbye" "goodbye" noArguments (p _ -> sendMessage p "see ya!!!!")
]

plugin = ErminePlugin commands []
