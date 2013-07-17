module Hello where

import IO.Unsafe

zap p args = unsafePerformIO (sendMessage p "ZAP")

foreign
  data "org.bukkit.entity.Player" Player
  method "sendMessage" sendMessage : Player -> String -> IO ()
