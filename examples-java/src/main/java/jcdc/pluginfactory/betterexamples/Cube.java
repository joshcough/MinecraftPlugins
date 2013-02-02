package jcdc.pluginfactory.betterexamples;

import jcdc.pluginfactory.betterjava.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Iterator;

public class Cube{

  public  final Location loc1;
  public  final Location loc2;
  private final World world;

  private final int minX;
  private final int maxX;
  private final int minY;
  private final int maxY;
  private final int minZ;
  private final int maxZ;

  public Cube(Location loc1, Location loc2) {
    this.loc1 = loc1; this.loc2 = loc2; this.world = loc1.getWorld();
    minX = Math.min((int)loc1.getX(), (int)loc2.getX());
    maxX = Math.max((int)loc1.getX(), (int)loc2.getX());
    minY = Math.min((int)loc1.getY(), (int)loc2.getY());
    maxY = Math.max((int)loc1.getY(), (int)loc2.getY());
    minZ = Math.min((int)loc1.getZ(), (int)loc2.getZ());
    maxZ = Math.max((int)loc1.getZ(), (int)loc2.getZ());
  }

  // TODO: how can you terminate early?
  private Void iterate(Function1<Block, Void> f){
    for(int x = minX; x<=maxX; x++){
      for(int y = minY; y<=maxY; y++){
        for(int z = minZ; z<=maxZ; z++){
          f.apply(world.getBlockAt(new Location(world, x, y, z)));
        }
      }
    }
    return null;
  }

  public Iterable<Block> iterable(){
    return new Iterable<Block>(){
      public Iterator<Block> iterator() {
        return new Iterator<Block>() {
          private int x = minX;
          private int y = minY;
          private int z = minZ;
          public boolean hasNext() { return x <= maxX && y <= maxY && z <= maxZ; }
          public Block next() {
            if(!hasNext()) new IllegalStateException("no more blocks in this cube!");
            Block b = world.getBlockAt(x, y, z);
            if     (x < maxX) x++;
            else if(y < maxY) y++;
            else if(z < maxZ) z++;
            return b;
          }
          public void remove() {
            throw new IllegalStateException("cant remove from this iterator!");
          }
        };
      }
    };
  }

  public Void set(final Material m){
    return iterate(new Function1<Block, Void>() {
      public Void apply(Block b) { b.setType(m); return null; }
    });
  }

  public Void change(final Material oldM, final Material newM) {
    return iterate(new Function1<Block, Void>() {
      public Void apply(Block b) {
        if(b.getType() == oldM) b.setType(newM);
        return null;
      }
    });
  }

  public Void seti(final Material m){
    for(Block b: iterable()){ b.setType(m); }
    return null;
  }

  public Void changei(final Material oldM, final Material newM) {
    for(Block b: iterable()){ if(b.getType() == oldM) b.setType(newM); }
    return null;
  }

  public Option<Block> find(final Material m) {
    for(Block b: iterable()){
      if(b.getType() == m) return Option.apply(b);
    }
    return new None<Block>();
  }


  // just a test...
  static public <T,U> Iterable<Tuple2<T,U>> zip(final Iterable<T> ts, final Iterable<U> us){
    return new Iterable<Tuple2<T,U>>(){
      public Iterator<Tuple2<T,U>> iterator() {
        final Iterator<T> tsI = ts.iterator();
        final Iterator<U> usI = us.iterator();
        return new Iterator<Tuple2<T, U>>() {
          public boolean hasNext() { return tsI.hasNext() && usI.hasNext(); }
          public Tuple2<T, U> next() {
            return new Tuple2<T, U>(tsI.next(), usI.next());
          }
          public void remove() {
            throw new IllegalStateException("cant remove from this iterator!");
          }
        };
      }
    };
  }
}