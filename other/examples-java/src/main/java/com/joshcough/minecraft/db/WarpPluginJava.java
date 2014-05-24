package com.joshcough.minecraft.db;

import com.avaje.ebean.EbeanServer;
import com.joshcough.minecraft.betterjava.*;
import static com.joshcough.minecraft.betterjava.JavaParsers.*;
import org.bukkit.entity.Player;

import java.util.List;

public class WarpPluginJava extends BetterJavaPlugin {

  private static Parser<String> warpToken = anyString;

  public WarpPluginJava(){
    addDependency("WarpPluginDB");

    commands.add(Command("warps", "List all warps.", p -> {
      for (Warp w : warpsFor(p)) { p.sendMessage(w.toString()); }
    }));

    commands.add(Command("warp", "Warp to the given warp location.", warpToken,
      (p, wt) -> p.teleport(getWarp(p, wt).location(p.getWorld()))
    ));

    commands.add(Command("set-warp", "Create a new warp location.", warpToken,
      (p, warpName) -> {
        Warp w = createWarp(warpName, p);
        getDatabase().save(w);
        p.sendMessage("created warp: " + warpName);
      }
     ));
  }

  public EbeanServer getDatabase(){
    return getServer().getPluginManager().getPlugin("WarpPluginDB").getDatabase();
  }

  // go to the database to find all the warps for the given player
  List<Warp> warpsFor(Player p){
    return getDatabase().find(Warp.class).where().eq("player", p.getName()).findList();
  }

  Warp getWarp(Player p, String name){
    return getDatabase().find(Warp.class).where().
        eq("player", p.getName()).eq("name", name).findUnique();
  }

  Warp createWarp(String n, Player p) {
    Warp w = new Warp();
    w.name = n;
    w.player = p.getName();
    w.x = p.getLocation().getX();
    w.y = p.getLocation().getY();
    w.z = p.getLocation().getZ();
    return w;
  }

  /*
    Command("delete-warp", "Delete a warp location.", warpToken){ case (p, wt) =>
      withWarp(p, wt){w => db.delete(w); p ! s"deleted warp: ${w.name}" }
    },

    Command("delete-all", "Delete all your warps.")(p =>
    warpsFor(p).foreach{ w => p ! s"deleting: $w"; db.delete(w); }
    ),

    OpOnly(Command("purge-warps-database", "Delete all warps in the database.")(p =>
    db.foreach { w => p ! s"deleting: $w"; db.delete(w) })
        )
        )
   */

}
