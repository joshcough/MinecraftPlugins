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

-- Bukkit commands in Ermine
-- | Command: Name, Description, Arguments, Body.
data Command = forall a b. Command String String (Parser a) (Player -> a -> IO ())
commandName (Command name _ _ _) = name

-- | A Plugin contains some commands, and some listeners.
data ErminePlugin = ErminePlugin (List Command) (List Listener)

-- override def onCommand(p: Player, cmd: BukkitCommand, commandName: String, args: List String)
onCommand : ErminePlugin -> Player -> BukkitCommand -> String -> List String -> Maybe (IO ())
onCommand plugin p bc command args = fmap maybeFunctor (runCommand p args) (findCommand plugin command)

findCommand : ErminePlugin -> String -> Maybe Command
findCommand (ErminePlugin cs _) c = find (x -> (commandName x) == c) cs

runCommand :  Player -> List String -> Command -> IO ()
runCommand p args (Command name desc parser body) = runCommandAfterParsing p body (runParser (parser & eof) args)

runCommandAfterParsing : Player -> (Player -> a -> IO ()) -> ParseResult (And a ()) -> IO ()
runCommandAfterParsing p f (Success (a :& _) _) = f p a
runCommandAfterParsing p _ (Failure message)    = sendMessage p message -- TODO: p !* (RED(msg), RED(desc), RED(s"/$name ${args.describe}"))
