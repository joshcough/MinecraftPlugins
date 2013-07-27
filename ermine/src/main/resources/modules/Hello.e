module Hello where

import Minecraft.Minecraft

hello p args   = sendMessage p "hello back"
goodbye p args = sendMessage p "see ya!!!!"
