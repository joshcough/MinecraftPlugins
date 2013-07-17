module Hello where

import IO.Unsafe

onCommand p command args = unsafePerformIO (sendMessage p "it worked!")

hello p = unsafePerformIO (sendMessage p "hello back")

foreign
  data "org.bukkit.entity.Player" Player
  method "sendMessage" sendMessage : Player -> String -> IO ()
