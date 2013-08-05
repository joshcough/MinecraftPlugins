package com.joshcough.minecraft.betterexamples;

import com.joshcough.minecraft.betterjava.BetterJavaPlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

public class BlockChangerGoldV2 extends BetterJavaPlugin {

  public void onEnable() {
    register(new Listener(){
      @EventHandler
      public void onBlockDamage(BlockDamageEvent event) {
        event.getBlock().setType(Material.GOLD_BLOCK);
      }
    });
  }
}
