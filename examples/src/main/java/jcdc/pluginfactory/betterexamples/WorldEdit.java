package jcdc.pluginfactory.betterexamples;

import jcdc.pluginfactory.betterjava.BetterJavaPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import scala.Function1;
import scala.Tuple2;
import scala.runtime.AbstractFunction1;

import java.util.HashMap;
import java.util.Map;

public class WorldEdit extends BetterJavaPlugin {
  public final Map<Player, Corners> corners = new HashMap<Player, Corners>();

  static private interface Corners{}
  static private Corners NoCorners = new Corners() {};
  static private class OneCorner implements Corners{
    private Location loc;
    OneCorner(Location loc){ this.loc = loc; }
  }
  static private class BothCorners implements Corners{
    private Cube cube;
    BothCorners(Cube cube){ this.cube = cube; }
  }

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

    commands.add(new Command(
        "wand",
        "Get a WorldEdit wand.",
        new NoArgCommandBody() {
          public void run(Player p) {
            p.getWorld().dropItem(p.getLocation(), itemStack(Material.WOOD_AXE));
          }
        }
    ));

    commands.add(new Command(
        "set",
        "Set all the selected blocks to the given material type.",
        new CommandBody<Material>(material) {
          public void run(Player p, final Material m) {
            runCorners(p, new AbstractFunction1<Cube, Void>() {
              public Void apply(Cube cube) { return cube.set(m); }
            });
          }
        }
    ));

    commands.add(new Command(
        "change",
        "Change all the selected blocks of the first material type to the second material type.",
        new CommandBody<Tuple2<Material, Material>>(material.and(material)) {
          public void run(Player p, final Tuple2<Material, Material> t) {
            runCorners(p, new AbstractFunction1<Cube, Void>() {
              public Void apply(Cube cube) { return cube.change(t._1(), t._2()); }
            });
          }
        }
    ));
  }

  private void setFirstPos(Player player, Location location) {
    corners.put(player, new OneCorner(location));
    player.sendMessage("first corner set to: " + location); // icky location toString...
  }

  private void setSecondPos(Player p, Location loc2) {
    Corners c = getCorners(p);
    if(c instanceof OneCorner) {
      corners.put(p, new BothCorners(new Cube(((OneCorner)c).loc, loc2)));
      p.sendMessage("second corner set to: " + loc2);
    }
    else if(c instanceof BothCorners){
      corners.put(p, new BothCorners(new Cube(((BothCorners)c).cube.loc1, loc2)));
      p.sendMessage("second corner set to: " + loc2);
    }
    else
      p.sendMessage("set corner one first! (with a left click)");
  }

  private Corners getCorners(Player p) {
    return corners.containsKey(p) ? corners.get(p) : NoCorners;
  }

  private void runCorners(Player p, Function1<Cube, Void> f){
    Corners c = getCorners(p);
    if(c instanceof BothCorners){ f.apply(((BothCorners)c).cube); }
    else p.sendMessage("Both corners must be set!");
  }
}
