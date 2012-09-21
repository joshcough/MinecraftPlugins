package jcdc.pluginfactory.betterexamples;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import scala.*;

import java.util.ArrayList;
import java.util.LinkedList;
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
    b.getWorld().playEffect(b.getLocation(), Effect.SMOKE, 1);
    b.setType(Material.AIR);
    b.getWorld().dropItem(b.getLocation(), new ItemStack(b.getType(), 1, b.getData()));
  }

  public Block blockAbove(Block b){
    return new Location(b.getWorld(), b.getX(), b.getY() + 1, b.getZ()).getBlock();
  }

  abstract class F1<A, B>{ abstract B run(A a); }

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
  }

  abstract class ParseResult<T>{
    abstract boolean isFailure();
    abstract boolean isSuccess();
    abstract T get();
  }
  class Success<T> extends ParseResult<T> {
    private final T t;
    private final LinkedList<String> rest;
    public Success(T t, LinkedList<String> rest){
      this.t = t;
      this.rest = rest;
    }
    boolean isFailure() { return false; }
    boolean isSuccess() { return true; }
    T get(){ return t; }
  }
  class Failure<T> extends ParseResult<T> {
    private String message;
    public Failure(String message){
      this.message = message;
    }
    boolean isFailure() { return true; }
    boolean isSuccess() { return false; }
    T get(){ throw new RuntimeException("cant get from Failure"); }
  }

  abstract class ArgParser<T> {
    abstract ParseResult<T> parse(LinkedList<String> args);

  // TODO: this is going to be difficult...

//    def |[U >: T] (p2: => Parser[U]) = new Parser[U] {
//      def apply(args: List[String]): ParseResult[U] = self(args) mapFailure { m1 =>
//        p2(args) mapFailure { m2 => Failure(s"$m1 or $m2") }
//      }
//      def describe = s"(${self.describe} or ${p2.describe})"
//    }
  }

  ArgParser<String> anyString = new ArgParser<String>() {
    @Override
    ParseResult<String> parse(LinkedList<String> args) {
      if(args.size() > 0) {
        LinkedList<String> ss = new LinkedList<String>(args);
        return new Success<String>(ss.removeFirst(), ss);
      }
      else return new Failure<String>("expected a string, but didn't get any");
    }
  };

  public <T> ArgParser<Option<T>> opt(final ArgParser<T> p){
    return new ArgParser<Option<T>>(){
      @Override
      ParseResult<Option<T>> parse(LinkedList<String> args) {
        ParseResult<T> pr = p.parse(args);
        if(pr.isFailure()) return new Success<Option<T>>(Option.<T>empty(), args);
        else {
          LinkedList<String> ss = new LinkedList<String>(args);
          ss.removeFirst();
          return new Success<Option<T>>(Option.apply(pr.get()), ss);
        }
      }
    };
  }

  public <T> ArgParser<T> token(final String name, final F1<String, Option<T>> f){
    return new ArgParser<T>() {
      ParseResult<T> parse(LinkedList<String> args) {
        if(args.isEmpty()) return new Failure<T>("expected " + name + ", got nothing");
        else{
          Option<T> ot = f.run(args.getFirst());
          LinkedList<String> ss = new LinkedList<String>(args);
          ss.removeFirst();
          if(ot.isDefined()) return new Success<T>(ot.get(), ss);
          else return new Failure<T>("invalid " + name + ": " + args.getFirst());
        }
      }
    };
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

  // TODO: this is also going to be difficult
//  val gamemode =
//      ("c" | "creative" | "1") ^^^ CREATIVE |
//      ("s" | "survival" | "0") ^^^ SURVIVAL
}
