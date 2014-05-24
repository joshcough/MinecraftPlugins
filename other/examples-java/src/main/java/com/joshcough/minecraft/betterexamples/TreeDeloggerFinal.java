package com.joshcough.minecraft.betterexamples;

import com.joshcough.minecraft.betterjava.BetterJavaPlugin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class TreeDeloggerFinal extends BetterJavaPlugin {
  public TreeDeloggerFinal() {
    Listeners(new Listener() {
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
