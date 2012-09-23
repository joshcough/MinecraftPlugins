package jcdc.pluginfactory.betterexamples;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class BlockChangerV2 extends JavaPlugin {
  public final Map<Player, Material> blockChangerUsers = new HashMap<Player, Material>();

  class BlockChangerListener implements Listener {
    @EventHandler public void onBlockDamage(BlockDamageEvent event) {
      if (blockChangerUsers.containsKey(event.getPlayer())) {
        event.getBlock().setTypeId(blockChangerUsers.get(event.getPlayer()).getId());
      }
    }
  };

  public void onEnable() {
    getServer().getPluginManager().registerEvents(new BlockChangerListener(), this);
  }

  public boolean onCommand(CommandSender sender, Command cmd,
                           String commandLabel, String[] args) {
    if("bc".equals(commandLabel.toLowerCase())) {
      blockChange((Player)sender, args);
      return true;
    }
    else return false;
  }

  private void blockChange(Player p, String[] args){
    if(args.length == 0) {
      blockChangerUsers.remove(p);
      p.sendMessage(ChatColor.RED + "BlockChanger has been disabled!");
    }
    else try {
      Material m = findMaterial(args[1]);
      blockChangerUsers.put(p, m);
      p.sendMessage(ChatColor.BLUE + "BlockChanger using: " + m.name());
    } catch (IllegalArgumentException e) {
      p.sendMessage("no such material: " + args[1]);
    }
  }

  public Material findMaterial(String nameOrId){
    Material m = Material.getMaterial(nameOrId);
    if(m == null) m = Material.getMaterial(Integer.parseInt(nameOrId));
    if(m == null) throw new IllegalArgumentException("no such material: " + nameOrId);
    return m;
  }
}
