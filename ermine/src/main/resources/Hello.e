module Hello where

import IO.Unsafe
import Minecraft

hello p args   =  sendMessage p "hello back"
goodbye p args = sendMessage p "see ya!!!!"
