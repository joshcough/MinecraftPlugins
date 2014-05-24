package com.joshcough.minecraft.betterexamples;

import com.joshcough.minecraft.betterjava.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class WorldEdit extends BetterJavaPlugin {
  public final Map<Player, List<Location>> corners = new HashMap<>();

  public WorldEdit() {
    Listeners(
      (LeftClickBlockHandler) (p, event) -> {
        if(isHolding(p, Material.WOOD_AXE)){
          setFirstPos(p, event.getClickedBlock().getLocation());
          event.setCancelled(true);
        }
      },
      (RightClickBlockHandler) (p, event) -> {
        if(isHolding(p, Material.WOOD_AXE)){
          setSecondPos(p, event.getClickedBlock().getLocation());
        }
      }
    );
    Commands(
      Command("wand", "Get a WorldEdit wand.", p ->
        p.getWorld().dropItem(p.getLocation(), itemStack(Material.WOOD_AXE))
      ),
      Command("set", "Set all the selected blocks to the given material type.", material,
        (p, m) -> cube(p).forEach(b -> b.setType(m))
      ),
      Command(
        "change",
        "Change all the selected blocks of the first material type to the second material type.",
        material.and(material),
        (p, ms) -> { for(Block b: cube(p)) { if(b.getType() == ms._1()) b.setType(ms._2()); }}
      )
    );
  }

  private void setFirstPos(Player player, final Location location) {
    corners.put(player, new LinkedList<Location>(){{add(location);}});
    player.sendMessage("first corner set to: " + location); // icky location toString...
  }

  private void setSecondPos(Player p, Location loc2) {
    List<Location> locs = getCorners(p);
    if(locs.size() > 0) {
      if(locs.size() == 1) locs.add(loc2); else locs.set(1, loc2);
      p.sendMessage("second corner set to: " + loc2);
    }
    else p.sendMessage("set corner one first! (with a left click)");
  }

  private List<Location> getCorners(Player p) {
    return corners.containsKey(p) ? corners.get(p) : new LinkedList<>();
  }

  public Iterable<Block> iterable(final Location loc1, final Location loc2){
    return () -> new Iterator<Block>() {
      final int minX = Math.min((int)loc1.getX(), (int)loc2.getX());
      final int maxX = Math.max((int)loc1.getX(), (int)loc2.getX());
      final int minY = Math.min((int)loc1.getY(), (int)loc2.getY());
      final int maxY = Math.max((int)loc1.getY(), (int)loc2.getY());
      final int minZ = Math.min((int)loc1.getZ(), (int)loc2.getZ());
      final int maxZ = Math.max((int)loc1.getZ(), (int)loc2.getZ());
      private int x = minX;
      private int y = minY;
      private int z = minZ;
      public boolean hasNext() { return x <= maxX && y <= maxY && z <= maxZ; }
      public Block next() {
        if(!hasNext()) new IllegalStateException("no more blocks in this cube!");
        Block b = loc1.getWorld().getBlockAt(x, y, z);
        if(x < maxX) x++; else if(y < maxY) y++; else if(z < maxZ) z++;
        return b;
      }
      public void remove() {
        throw new IllegalStateException("cant remove from this iterator!");
      }
    };
  }

  private Iterable<Block> cube(Player p){
    List<Location> cs = getCorners(p);
    if(cs.size() == 2) return iterable(cs.get(0), cs.get(1));
    else {
      p.sendMessage("Both corners must be set!");
      return new LinkedList<>();
    }
  }
}
