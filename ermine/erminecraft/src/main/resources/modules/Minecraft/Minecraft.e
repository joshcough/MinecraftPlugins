module Minecraft.Minecraft where

import Native.Function
import Native.List

-- ermine stdlib exports
export Function
export List
export Prelude
export Syntax.IO

-- minecraft related exports
export Parsers.ParserCombinators
export Minecraft.Block
export Minecraft.Helpers
export Minecraft.Native
export Minecraft.Parsers

-- | A Plugin contains some commands, and some listeners.
-- TODO: maybe the Commands should be a (Map String Command)
data ErminePlugin = ErminePlugin (List Command) (List Listener)

-- Bukkit commands in Ermine
-- | Command: Name, Description, Arguments, Body.
data Command = Command String String (List String -> Player -> IO ())

-- | Command: Name, Description, Arguments, Body.
command : String -> String -> (Parser a) -> (Player -> a -> IO ()) -> Command
command name desc p body = command' name desc (p & eof) body where
  command' : String -> String -> (Parser (And a ())) -> (Player -> a -> IO ()) -> Command
  command' name desc (Parser pbody pname) body = Command name desc (args -> runCommandAfterParsing (pbody args) (flip body))
  runCommandAfterParsing : ParseResult (And a ()) -> (a -> Player -> IO ()) -> Player -> IO ()
  runCommandAfterParsing (Success (a :& _) _) f p = f a p
  runCommandAfterParsing (Failure message)    _ p = sendMessage p message -- TODO: p !* (RED(msg), RED(desc), RED(s"/$name ${args.describe}"))

-- If the given plugin contains the given command, return the IO action that will run the command.
-- If not, return Nothing.
onCommand : ErminePlugin -> Player -> BukkitCommand -> String -> List String -> Maybe (IO ())
onCommand plugin p bc c args = fmap maybeFunctor (c -> commandBody c args p) (findCommand plugin c) where
  findCommand : ErminePlugin -> String -> Maybe Command
  findCommand (ErminePlugin cs _) c = find (x -> (commandName x) == c) cs
  commandName : Command -> String
  commandName (Command name _ _) = name
  commandBody : Command -> (List String -> Player -> IO ())
  commandBody (Command _ _ body) = body
