package jcdc.pluginfactory.betterexamples;

import jcdc.pluginfactory.java.BetterJavaPlugin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class TreeDelogger extends BetterJavaPlugin {
  public TreeDelogger() {
    listeners.add(new Listener() {
      @EventHandler public void onBlockBreak(BlockBreakEvent event) {
        Block current = event.getBlock();
        while (current.getType() == Material.LOG) {
          erase(current);
          current = blockAbove(current);
        }
      }
    });
  }
}
