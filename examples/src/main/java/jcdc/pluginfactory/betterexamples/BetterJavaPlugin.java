package jcdc.pluginfactory.betterexamples;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BetterJavaPlugin extends JavaPlugin {
  public final Logger logger = Logger.getLogger("Minecraft");

  public List<Listener> listeners = new ArrayList<Listener>();
  public List<Cmd> commands = new ArrayList<Cmd>();

  public void onDisable() {
    info(getDescription().getName() + " is now disabled.");
  }

  public void onEnable() {
    for (Listener l : listeners) { register(l); }
    info(getDescription().getName() + " version " + getVersion() + " is now enabled.");
  }


  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    for(Cmd c: commands){
      if(c.name.toLowerCase().equals(commandLabel.toLowerCase())){
        c.body.run((Player)sender, args);
      }
    }
    return true;
  }

  public void info(String message) {
    logger.info(message);
  }

  public String getVersion() {
    return getDescription().getVersion();
  }

  public void register(Listener listener) {
    getServer().getPluginManager().registerEvents(listener, this);
  }

  public void erase(Block b){
    ItemStack droppedItem = new ItemStack(b.getType(), 1, b.getData());
    b.getWorld().playEffect(b.getLocation(), Effect.SMOKE, 1);
    b.setType(Material.AIR);
    b.getWorld().dropItem(b.getLocation(), droppedItem);
  }

  public Block blockAbove(Block b){
    return new Location(b.getWorld(), b.getX(), b.getY() + 1, b.getZ()).getBlock();
  }

  public class Cmd {
    final String name;
    final String description;
    final CommandBody body;
    public Cmd(String name, String description, CommandBody body){
      this.name = name;
      this.description = description;
      this.body = body;
    }
  }

  abstract public class CommandBody{
    abstract public void run(Player p, String[] args);
  }
}
