package jcdc.pluginfactory.betterexamples;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import scala.Option;

import java.util.*;
import java.util.logging.Logger;

import static jcdc.pluginfactory.betterexamples.JavaParsers.*;

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
        // todo: doesnt work for console yet.
        c.body.parseAndRun((Player)sender, args);
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
    b.getWorld().playEffect(b.getLocation(), Effect.SMOKE, 1);
    b.setType(Material.AIR);
    b.getWorld().dropItem(b.getLocation(), new ItemStack(b.getType(), 1, b.getData()));
  }

  public Block blockAbove(Block b){
    return new Location(b.getWorld(), b.getX(), b.getY() + 1, b.getZ()).getBlock();
  }

  public class Cmd {
    final String name;
    final String description;
    final CommandBody body;
    public <T> Cmd(String name, String description, CommandBody<T> body){
      this.name = name;
      this.description = description;
      this.body = body;
    }
  }

  abstract public class CommandBody<T>{
    private ArgParser<T> argParser;
    public CommandBody(ArgParser<T> argParser){
      this.argParser = argParser;
    }
    abstract public void run(Player p, T t);
    public void parseAndRun(Player p, String[] args){
      ParseResult<T> pr = argParser.parse(args);
      if(pr.isSuccess()) run(p, pr.get());
      else p.sendMessage(pr.error());
    }
  }

  public ArgParser<Material> material = token("material", new F1<String, Option<Material>>() {
    Option<Material> run(String s) {
      Material m = Material.getMaterial(s);
      if(m == null) m = Material.getMaterial(Integer.parseInt(s));
      return Option.apply(m);
    };
  });

  public ArgParser<EntityType> entity = token("entity", new F1<String, Option<EntityType>>() {
    Option<EntityType> run(String s) {
      EntityType e = EntityType.fromName(s.toUpperCase());
      if(e == null) e = EntityType.valueOf(s.toUpperCase());
      return Option.apply(e);
    };
  });

  public ArgParser<Player> player = token("player", new F1<String, Option<Player>>() {
    Option<Player> run(String s) {
      return Option.apply(getServer().getPlayer(s));
    };
  });

  public ArgParser<GameMode> gamemode =
    match("c").or(match("creative")).or(match("1")).outputting(GameMode.CREATIVE).or(
    match("s").or(match("survival")).or(match("0")).outputting(GameMode.SURVIVAL));
}
