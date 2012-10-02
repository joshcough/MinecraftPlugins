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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BlockChanger extends JavaPlugin {
  public final Map<Player, Material> blocks = new HashMap<Player, Material>();

  public void onEnable() {
    getServer().getPluginManager().registerEvents(new Listener (){
      @EventHandler public void onBlockDamage(BlockDamageEvent event) {
        if (blocks.containsKey(event.getPlayer())) {
          event.getBlock().setTypeId(blocks.get(event.getPlayer()).getId());
        }
      }
    }, this);
  }

  /**
   * One function to rule them...
   */
  public boolean onCommand(CommandSender sender, Command cmd,
                           String commandLabel, String[] args) {
    if("bc".equals(commandLabel.toLowerCase())) {
      blockChange((Player)sender, args);
      return true;
    }
    // if we had more commands, we'd check add more cases here
    // else if("someOtherCommand".equals(commandLabel.toLowerCase())) { ... }
    else return false;
  }

  // bc command was called, now manually parse all the arguments
  private void blockChange(Player p, String[] args){
    switch (args.length){
      // zero arguments, disable block changer for this player
      case 0:
        blocks.remove(p);
        p.sendMessage(ChatColor.RED + "BlockChanger has been disabled!");
      case 1:
        // one argument, check if its a material
        try {
          Material m = findMaterial(args[1]);
          blocks.put(p, m);
          p.sendMessage(ChatColor.BLUE + "BlockChanger using: " + m.name());
        } catch (IllegalArgumentException e) {
          p.sendMessage("no such material: " + args[1]);
        }
      default:
        // too many arguments case
        String argsWithoutFirst = Arrays.toString(Arrays.copyOfRange(args, 1, args.length));
        p.sendMessage(ChatColor.RED + "/bc can't process these arguments: " + argsWithoutFirst);
    }
  }

  // go searching for a Material based on a name or an id.
  // you did know it was legal to pass a number in here, right?
  public Material findMaterial(String nameOrId){
    Material m = Material.getMaterial(nameOrId);
    if(m == null) m = Material.getMaterial(Integer.parseInt(nameOrId));
    if(m == null) throw new IllegalArgumentException("no such material: " + nameOrId);
    return m;
  }
}
