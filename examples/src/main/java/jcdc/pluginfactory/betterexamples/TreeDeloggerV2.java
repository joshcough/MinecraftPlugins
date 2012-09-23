package jcdc.pluginfactory.betterexamples;

import jcdc.pluginfactory.betterjava.BetterJavaPlugin;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class TreeDeloggerV2 extends JavaPlugin {

  Listener l = new Listener() {
    @EventHandler public void onBlockBreak(BlockBreakEvent event) {
      Block current = event.getBlock();
      while (current.getType() == Material.LOG) {
        erase(current);
        current = blockAbove(current);
      }
    }
  };

  public void onEnable() {
    getServer().getPluginManager().registerEvents(l, this);
  }

  public void erase(Block b){
    b.getWorld().playEffect(b.getLocation(), Effect.SMOKE, 1);
    b.setType(Material.AIR);
    b.getWorld().dropItem(b.getLocation(), new ItemStack(b.getType(), 1, b.getData()));
  }

  public Block blockAbove(Block b){
    return new Location(b.getWorld(), b.getX(), b.getY() + 1, b.getZ()).getBlock();
  }
}
