package jcdc.pluginfactory.betterexamples;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import scala.Function1;
import scala.runtime.AbstractFunction1;

public class Cube{

  public  final Location loc1;
  public  final Location loc2;
  private final World world;

  public Cube(Location loc1, Location loc2) {
    this.loc1 = loc1; this.loc2 = loc2; this.world = loc1.getWorld();
  }

  // TODO: how can you terminate early?
  private Void iterate(Function1<Block, Void> f){
    int minX = Math.min((int)loc1.getX(), (int)loc1.getX());
    int maxX = Math.max((int)loc1.getX(), (int)loc1.getX());
    int minY = Math.min((int)loc1.getY(), (int)loc1.getY());
    int maxY = Math.max((int)loc1.getY(), (int)loc1.getY());
    int minZ = Math.min((int)loc1.getZ(), (int)loc1.getZ());
    int maxZ = Math.max((int)loc1.getZ(), (int)loc1.getZ());
    for(int x = minX; x<=maxX; x++){
      for(int y = minY; y<=maxY; y++){
        for(int z = minZ; z<=maxZ; z++){
          f.apply(world.getBlockAt(new Location(world, x, y, z)));
        }
      }
    }
    return null;
  }

  public Void set(final Material m){
    return iterate(new AbstractFunction1<Block, Void>() {
      public Void apply(Block b) { b.setType(m); return null; }
    });
  }

  public Void change(final Material oldM, final Material newM) {
    return iterate(new AbstractFunction1<Block, Void>() {
      public Void apply(Block b) {
        if(b.getType() == oldM) b.setType(newM);
        return null;
      }
    });
  }
}