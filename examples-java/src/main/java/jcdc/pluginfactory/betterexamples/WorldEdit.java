package jcdc.pluginfactory.betterexamples;

import jcdc.pluginfactory.betterjava.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class WorldEdit extends BetterJavaPlugin {
  public final Map<Player, List<Location>> corners = new HashMap<Player, List<Location>>();

  public WorldEdit() {
    listeners.add(new LeftClickBlockHandler() {
      public void onLeftClickBlock(Player p, PlayerInteractEvent event) {
        if(isHolding(p, Material.WOOD_AXE)){
          setFirstPos(p, event.getClickedBlock().getLocation());
          event.setCancelled(true);
        }
      }
    });
    listeners.add(new RightClickBlockHandler() {
      public void onRightClickBlock(Player p, PlayerInteractEvent event) {
        if(isHolding(p, Material.WOOD_AXE)){
          setSecondPos(p, event.getClickedBlock().getLocation());
        }
      }
    });

    commands.add(new Command("wand", "Get a WorldEdit wand.", new NoArgCommandBody() {
      public void run(Player p) {
        p.getWorld().dropItem(p.getLocation(), itemStack(Material.WOOD_AXE));
      }
    }));
    commands.add(new Command(
        "set", "Set all the selected blocks to the given material type.",
        new CommandBody<Material>(material) {
          public void run(Player p, final Material m) {
            for(Block b: cube(p)) { b.setType(m); }
          }
        }
    ));
    commands.add(new Command(
        "change",
        "Change all the selected blocks of the first material type to the second material type.",
        new CommandBody<Tuple2<Material, Material>>(material.and(material)) {
          public void run(Player p, final Tuple2<Material, Material> t) {
            for(Block b: cube(p)) { if(b.getType() == t._1()) b.setType(t._2()); }
          }
        }
    ));
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
    else
      p.sendMessage("set corner one first! (with a left click)");
  }

  private List<Location> getCorners(Player p) {
    return corners.containsKey(p) ? corners.get(p) : new LinkedList<Location>();
  }

  public Iterable<Block> iterable(final Location loc1, final Location loc2){
    return new Iterable<Block>(){
      public Iterator<Block> iterator() {
        return new Iterator<Block>() {
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
    };
  }

  private Iterable<Block> cube(Player p){
    List<Location> cs = getCorners(p);
    if(cs.size() == 2) return iterable(cs.get(0), cs.get(1));
    else {
      p.sendMessage("Both corners must be set!");
      return new LinkedList<Block>();
    }
  }
}
