module Hello where

import IO.Unsafe
import Minecraft

main p args = unsafePerformIO (zap p)
