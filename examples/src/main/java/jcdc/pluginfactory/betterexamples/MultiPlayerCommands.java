package jcdc.pluginfactory.betterexamples;

import jcdc.pluginfactory.betterjava.BetterJavaPlugin;
import static jcdc.pluginfactory.betterjava.JavaParsers.*;
import org.bukkit.entity.Player;

public class MultiPlayerCommands extends BetterJavaPlugin {
  public MultiPlayerCommands(){
    commands.add(new Command("goto", "Teleport to a player.", new CommandBody<Player>(player) {
      public void run(Player you, Player them) { you.teleport(them); }
    }));

    commands.add(new Command("set-time", "Sets the time.", new CommandBody<Integer>(integer) {
      public void run(Player p, Integer i) { p.getWorld().setTime(i.longValue()); }
    }));

    commands.add(new Command("day", "Sets the time to day.", new EmptyCommandBody() {
      public void run(Player p) { p.getWorld().setTime(1); }
    }));

    commands.add(new Command("night", "Sets the time to night.", new EmptyCommandBody() {
      public void run(Player p) { p.getWorld().setTime(15000); }
    }));
  }
}


/**
 Command("entities", "Display all the entities.",
 noArgs(p => p !* (p.world.entities.map(_.toString): _*))),
 Command("feed",     "Fill a players hunger bar",
 opOnly(p2p((you, them) => you.doTo(them, them.setFoodLevel(20), "fed")))),
 Command("starve",   "Drain a players hunger bar.",
 opOnly(p2p((you, them) => you.doTo(them, them.setFoodLevel(0), "starved")))),
 Command("shock",    "Shock a player.",
 opOnly(p2p((you, them) => you.doTo(them, them.strike, "shocked")))),
 Command("gm",       "Set your game mode", args(gamemode){ case p ~ gm => p.setGameMode(gm) }),
 Command("gms",      "Set your game mode to survival.", noArgs(_.setGameMode(SURVIVAL))),
 Command("gmc",      "Set your game mode to creative.", noArgs(_.setGameMode(CREATIVE))),
 Command("spawn",    "Spawn some mobs.",
 args(entity ~ opt(num.named("number to spawn"))){
 case p ~ (e ~ n) => p.loc.spawnN(e, n.fold(1)(id))
 }),
 Command("ban",      "Ban some players.",
 args(anyString+){ case you ~ them =>
 server.findOnlinePlayers (them).foreach { _.ban(you.name + " doesn't like you.") }
 server.findOfflinePlayers(them).foreach { _ setBanned true }
 }),
 Command("box",      "Put a box around yourself, made of any material.",
 args(material){ case p ~ m  => p.blocksAround.foreach(_ changeTo m) }),
 Command("safe",     "Put yourself in a box made of bedrock.",
 noArgs(_.blocksAround.foreach(_ changeTo BEDROCK))),
 Command("up",       "Go up to the surface.",
 noArgs(p => p.teleportTo(p.world.getHighestBlockAt(p.loc)))),
 Command("drill",    "Drill down to bedrock immediately.",
 noArgs(p =>
 for (b <- p.blockOn.blocksBelow.takeWhile(_ isNot BEDROCK); if (b isNot AIR)) {
 b.erase
 if (b.blockBelow is BEDROCK) b.nthBlockAbove(2) changeTo STATIONARY_WATER
 })),
 Command("kill",     "Kill entities.",
 args(("player" ~ player) or entity){
 case killer ~ Left(_ ~ deadMan) => killer.kill(deadMan)
 case killer ~ Right(e) => killer.world.entities.filter { _ isAn e }.foreach(_.remove)
 }),
 Command("creeper-kill", "Surround a player with creepers", opOnly(p2p((_, them) => {
 them.setGameMode(SURVIVAL)
 them.loc.block.neighbors8.foreach(_.loc.spawn(CREEPER))
 })))
 )
 }
 **/