package jcdc.pluginfactory.betterexamples;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockChangerGold extends JavaPlugin {

  class BlockChangerListener implements Listener {
    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
      event.getBlock().setType(Material.GOLD_BLOCK);
    }
  };

  public void onEnable() {
    getServer().getPluginManager().registerEvents(new BlockChangerListener(), this);
  }
}
