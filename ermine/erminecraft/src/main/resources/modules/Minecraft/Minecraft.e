module Minecraft.Minecraft where

import Error
import Native.Function
import Native.List
import Prelude
import String as String

-- ermine stdlib exports
export Function
export List
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

commandPlugin  commands  = ErminePlugin commands []
listenerPlugin listeners = ErminePlugin [] listeners

-- Bukkit commands in Ermine
-- | Command: Name, Description, Arguments Description, Body.
data Command = Command String String String (List String -> Player -> IO ())

-- | Command: Name, Description, Arguments, Body.
command : String -> String -> (Parser a) -> (Player -> a -> IO ()) -> Command
command name desc p@(Parser _ pname) body = command' name desc (p & eof) pname body where
  command' : String -> String -> (Parser (And a ())) -> String -> (Player -> a -> IO ()) -> Command
  command' name desc (Parser pbody _) pname body = Command name desc pname (args -> runCommandAfterParsing (pbody args) (flip body))
  runCommandAfterParsing : ParseResult (And a ()) -> (a -> Player -> IO ()) -> Player -> IO ()
  runCommandAfterParsing (Success (a :& _) _) f p = f a p
  runCommandAfterParsing (Failure message)    _ p = sendMessage p message -- TODO: p !* (RED(msg), RED(desc), RED(s"/$name ${args.describe}"))

commandName : Command -> String
commandName (Command name _ _ _) = name
commandDescription : Command -> String
commandDescription (Command _ desc _ _) = desc
commandArgDescription : Command -> String
commandArgDescription (Command _ _ argDesc _) = argDesc
commandBody : Command -> (List String -> Player -> IO ())
commandBody (Command _ _ _ body) = body

-- If the given plugin contains the given command, return the IO action that will run the command.
-- If not, return Nothing.
onCommand : ErminePlugin -> Player -> BukkitCommand -> String -> List# String -> IO ()
onCommand plugin p bc c args =
  maybe (return $ error "no such command") (c -> commandBody c (fromList# args) p) (findCommand plugin c) where
    findCommand : ErminePlugin -> String -> Maybe Command
    findCommand (ErminePlugin cs _) c = find (x -> (commandName x) == c) cs

listeners : ErminePlugin -> List# Listener
listeners (ErminePlugin _ ls) = toList# ls

-- | Plugin, Plugin name, Class name, Author, Version
yml : ErminePlugin -> String -> String -> String -> String -> String
yml (ErminePlugin commands _) pluginName className author version = toLines [
  "name: "     ++_String pluginName,
  "main: "     ++_String className,
  "author: "   ++_String author,
  "version: "  ++_String version,
  "database: " ++_String "false",
  "depend: ["  ++_String "ScalaLibPlugin, ScalaPluginAPI, ErmineLibPlugin" ++_String "]",
  "softdepend: []\n" ] ++_String commandsYml where
  commandsYml : String
  commandsYml = concat_String $ fmap listFunctor commandYml commands
  commandYml : Command -> String
  commandYml c = toLines [
    "  " ++_String (commandName c) ++_String ":",
    "    description: " ++_String (commandDescription c),
    "    usage: /" ++_String (commandName c) ++_String " " ++_String (commandArgDescription c) ++_String "\n" ]
  toLines =  unsplit_String '\n'
