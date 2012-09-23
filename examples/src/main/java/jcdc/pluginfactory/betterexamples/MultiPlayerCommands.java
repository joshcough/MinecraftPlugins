package jcdc.pluginfactory.betterexamples;

import jcdc.pluginfactory.betterjava.BetterJavaPlugin;
import static jcdc.pluginfactory.betterjava.JavaParsers.*;

import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import scala.Option;
import scala.Tuple2;
import scala.util.Either;

import java.util.List;

public class MultiPlayerCommands extends BetterJavaPlugin {

  public MultiPlayerCommands(){

    commands.add(new Command("goto", "Teleport to a player.",
      new CommandBody<Either<Player, Tuple2<Tuple2<Integer, Integer>, Option<Integer>>>>(
          either(player, integer.and(integer).and(opt(integer)))) {
        public void run(Player you,
                        Either<Player, Tuple2<Tuple2<Integer, Integer>, Option<Integer>>> e) {
          if(e.isLeft()) you.teleport(e.left().get());
          else{
            Tuple2<Tuple2<Integer, Integer>, Option<Integer>> t = e.right().get();
            int x = t._1()._1();
            if(t._2().isDefined()) teleport(you, x, t._1()._2(), t._2().get());
            else {
              int z = t._1()._2();
              teleport(you, x, you.getWorld().getHighestBlockYAt(x, z), z);
            }
          }
        }
      }
    ));

    commands.add(new Command("set-time", "Sets the time.", new CommandBody<Integer>(integer) {
      public void run(Player p, Integer i) { p.getWorld().setTime(i.longValue()); }
    }));

    commands.add(new Command("day", "Sets the time to day.", new NoArgCommandBody() {
      public void run(Player p) { p.getWorld().setTime(1); }
    }));

    commands.add(new Command("night", "Sets the time to night.", new NoArgCommandBody() {
      public void run(Player p) { p.getWorld().setTime(15000); }
    }));

    commands.add(new Command("entities", "Display all the entities.", new NoArgCommandBody() {
      public void run(Player p) {
        for(Entity e: p.getWorld().getEntities()){ p.sendMessage(e.toString()); }
      }
    }));

    commands.add(new Command("feed", "Fill a players hunger bar", new CommandBody<Player>(player) {
      public void run(Player you, final Player them) { doTo(you, them, new Runnable(){
        public void run() { them.setFoodLevel(20); }
      }, "fed"); }
    }));

    commands.add(new Command("starve", "Drain a players hunger bar", new CommandBody<Player>(player)
    {
      public void run(Player you, final Player them) { doTo(you, them, new Runnable(){
        public void run() { them.setFoodLevel(0); }
      }, "starved"); }
    }));

    commands.add(new Command("shock", "Shock a player.", new CommandBody<Player>(player) {
      public void run(Player you, final Player them) { doTo(you, them, new Runnable(){
        public void run() { them.getWorld().strikeLightning(them.getLocation()); }
      }, "starved"); }
    }));

    commands.add(new Command("gm", "Set your game mode", new CommandBody<GameMode>(gamemode) {
      public void run(Player you, GameMode gm) { you.setGameMode(gm); }
    }));

    commands.add(new Command("gms", "Set your game mode to survival", new NoArgCommandBody() {
      public void run(Player you) { you.setGameMode(GameMode.SURVIVAL); }
    }));

    commands.add(new Command("gmc", "Set your game mode to creative", new NoArgCommandBody() {
      public void run(Player you) { you.setGameMode(GameMode.CREATIVE); }
    }));

    commands.add(new Command("spawn", "Spawn some mobs.",
      new CommandBody<Tuple2<EntityType, Option<Integer>>>(entity.and(opt(integer))) {
        public void run(Player p, Tuple2<EntityType, Option<Integer>> t) {
          spawnN(t._1(), t._2().isDefined() ? t._2().get() : 1, p.getLocation());
        }
    }));

    commands.add(new Command("ban", "Ban some players.",
      new CommandBody<List<String>>(anyString.plus()) {
        public void run(Player you, List<String> ss) {
          for(Player them: getServer().getOnlinePlayers()){
            if(ss.contains(them.getName())) ban(them, you.getName() + " doesn't like you.");
          }
          for(OfflinePlayer them: getServer().getOfflinePlayers()){
            if(ss.contains(them.getName())) them.setBanned(true);
          }
        }
      }
    ));

    commands.add(new Command("up", "Go up to the surface.", new NoArgCommandBody() {
      public void run(Player p) {
        p.teleport(p.getWorld().getHighestBlockAt(p.getLocation()).getLocation());
      }
    }));

    commands.add(new Command("kill", "Kill entities.",
      new CommandBody<Either<Tuple2<String, Player>, EntityType>>(
        either(match("player").and(player), entity)) {
        public void run(Player killer, final Either<Tuple2<String, Player>, EntityType> e) {
          if(e.isLeft()) doTo(killer, e.left().get()._2(), new Runnable() {
            public void run() { e.left().get()._2().setHealth(0); } }, "killed"
          );
          else for(Entity en: killer.getWorld().getEntities()){
            if(en.getType() == e.right().get()) en.remove();
          }
        }
    }));
  }

  /**
   TODO: still need to implement these, but they require all the block pimping
   which will be non-trivial and ugly in java.

   Command("box",      "Put a box around yourself, made of any material.",
     args(material){ case p ~ m  => p.blocksAround.foreach(_ changeTo m) }),

   Command("safe",     "Put yourself in a box made of bedrock.",
    noArgs(_.blocksAround.foreach(_ changeTo BEDROCK))),

   Command("drill",    "Drill down to bedrock immediately.", noArgs(p =>
     for (b <- p.blockOn.blocksBelow.takeWhile(_ isNot BEDROCK); if (b isNot AIR)) {
     b.erase
     if (b.blockBelow is BEDROCK) b.nthBlockAbove(2) changeTo STATIONARY_WATER
    })),

   Command("creeper-kill", "Surround a player with creepers", opOnly(p2p((_, them) => {
    them.setGameMode(SURVIVAL)
    them.loc.block.neighbors8.foreach(_.loc.spawn(CREEPER))
   })
   **/
}
