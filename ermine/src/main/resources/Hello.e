module Hello where

import IO.Unsafe
import Minecraft

hello p args = unsafePerformIO (sendMessage p "hello back")
goodbye p args = unsafePerformIO (sendMessage p "see ya!!!!")
