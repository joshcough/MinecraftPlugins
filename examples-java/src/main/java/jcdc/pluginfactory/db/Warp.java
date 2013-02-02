package jcdc.pluginfactory.db;

import org.bukkit.Location;
import org.bukkit.World;

import javax.persistence.*;

@Entity
public class Warp {

  @Id @GeneratedValue
  public int id;
  public String name, player; public double x, y, z;

  @Override
  public String toString(){
    return player + "." + name + "(" + x + ", " + y + ", " + z + ")";
  }

  public int    getId(){ return id; }
  public void   setId(int id){ this.id = id; }
  public String getName() { return name; }
  public void   setName(String name) { this.name = name; }
  public String getPlayer() { return player; }
  public void   setPlayer(String player) { this.player = player; }
  public double getX() { return x; }
  public void   setX(double x) { this.x = x; }
  public double getY() { return y; }
  public void   setY(double y) { this.y = y; }
  public double getZ() { return z; }
  public void   setZ(double z) { this.z = z; }
  public Location location(World w){ return new Location(w, x, y, z); }
}
