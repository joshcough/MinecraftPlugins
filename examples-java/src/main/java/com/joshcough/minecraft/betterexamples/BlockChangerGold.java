package com.joshcough.minecraft.betterexamples;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

// 1:  Extend JavaPlugin
public class BlockChangerGold extends JavaPlugin {
  // 2:  Create a handler for the event
  class BlockChangerListener implements Listener {
    @EventHandler
    public void onBlockDamage(BlockDamageEvent event){
      event.getBlock().setType(Material.GOLD_BLOCK);
    }
  }
  public void onEnable() {
    // 3:  Register the handler for the event
    getServer().getPluginManager().registerEvents(new BlockChangerListener(), this);
  }
}