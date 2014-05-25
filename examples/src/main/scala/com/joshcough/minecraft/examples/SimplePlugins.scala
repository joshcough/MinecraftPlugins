//package com.joshcough.minecraft.examples
//
//import org.bukkit.ChatColor._
//import com.joshcough.minecraft.{CommandsPlugin, Listeners}
//import Listeners._
//
//object Curses {
//  import org.bukkit.event.player.PlayerChatEvent
//  val curses = List("btt", "tiju", "gvdl", "cjudi").map(_.map(c => (c - 1).toChar))
//  def containsSwear(event: PlayerChatEvent) =
//    curses.filter(event.getMessage.toLowerCase.contains(_)).size > 0
//  def handle(e: PlayerChatEvent, f: => Unit) = e.cancelIf(containsSwear(e), f)
//}
//
//class CurseBan extends ListeningFor(OnPlayerChat((p, e) =>
//  Curses.handle(e, e.getPlayer.ban("no swearing")))
//)
//
//class CursePreventer extends ListeningFor(OnPlayerChat((p, e) => Curses.handle(e, ())))
//
//class PermissionsTest extends CommandsPlugin { self =>
//  val permission = anyStringAs("permission")
//  val commands = List(
//    Command("give-perm",  "Give a player a permission", permission){
//      case (p, perm) => p.addAttachment(self).setPermission(perm, true)
//    },
//    Command("test-perm",  "Test", permission){
//      case (p, perm) =>
//        if (p hasPermission perm) p ! s"you have permission: $perm"
//        else p ! RED(s"you don't have permission: $perm")
//    }
//  )
//}
