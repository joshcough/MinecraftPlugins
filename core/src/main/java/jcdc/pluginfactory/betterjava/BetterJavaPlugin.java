package jcdc.pluginfactory.betterjava;

import org.bukkit.*;
import static org.bukkit.ChatColor.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import scala.Option;
import scala.runtime.AbstractFunction1;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.Logger;

import static jcdc.pluginfactory.betterjava.JavaParsers.*;

public class BetterJavaPlugin extends JavaPlugin {
  public final Logger logger = Logger.getLogger("Minecraft");

  public List<Listener> listeners = new ArrayList<Listener>();
  public List<Command> commands = new ArrayList<Command>();

  public void onEnable() {
    for (Listener l : listeners) { register(l); }
    info(getDescription().getName() + " version " + getVersion() + " is now enabled.");
  }

  public void onDisable() {
    info(getDescription().getName() + " is now disabled.");
  }

  public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd,
                           String commandLabel, String[] args) {
    boolean handled = false;
    Player p = sender instanceof Player ? (Player)sender : consolePlayer;
    for(Command c: commands){
      if(c.name.toLowerCase().equals(commandLabel.toLowerCase())){
        try{
          c.body.parseAndRun(p, args);
          handled = true;
        } catch (Exception e){
          p.sendMessage(e.getMessage());
          e.printStackTrace();
        }
      }
    }
    return handled;
  }

  public void register(Listener l){
    getServer().getPluginManager().registerEvents(l, this);
  }

  public void info(String message) { logger.info(message); }

  public String getVersion() { return getDescription().getVersion(); }

  public void erase(Block b){
    b.getWorld().playEffect(b.getLocation(), Effect.SMOKE, 1);
    b.setType(Material.AIR);
    b.getWorld().dropItem(b.getLocation(), new ItemStack(b.getType(), 1, b.getData()));
  }

  public Block blockAbove(Block b){
    return new Location(b.getWorld(), b.getX(), b.getY() + 1, b.getZ()).getBlock();
  }

  public void spawnN(EntityType e, int numToSpawn, Location l){
    for(int i = 0; i < numToSpawn; i++){ l.getWorld().spawnCreature(l, e); }
  }

  public void ban(Player p, String reason){
    p.sendMessage(reason);
    p.setBanned(true);
  }

  static public void teleport(Player p, int x, int y, int z){
    p.teleport(new Location(p.getWorld(), x, y, z));
  }

  public class Command {
    final String name;
    final String description;
    final CommandBody body;
    public <T> Command(String name, String description, CommandBody<T> body){
      this.name = name;
      this.description = description;
      this.body = body;
    }
  }

  static public void doTo(Player p1, Player p2, Runnable r, String actionName){
    r.run();
    p2.sendMessage(GREEN + "you have been " + actionName + " by " + p1.getName());
    p1.sendMessage(GREEN + "you have " + actionName + " " + p2.getName());
  }

  abstract public class CommandBody<T>{
    private ArgParser<T> argParser;
    public CommandBody(ArgParser<T> argParser){ this.argParser = argParser; }
    abstract public void run(Player p, T t);
    public void parseAndRun(Player p, String[] args){
      ParseResult<T> pr = argParser.parse(args);
      if(pr.isSuccess()) run(p, pr.get());
      else p.sendMessage(pr.error());
    }
  }

  abstract public class NoArgCommandBody extends CommandBody<Void> {
    public NoArgCommandBody(){ super(nothing()); }
    public void run(Player p, Void v){ run(p); }
    abstract public void run(Player p);
  }

  public Option<Material> findMaterial(String nameOrId){
    Material m = Material.getMaterial(nameOrId);
    if(m == null) try {
      m = Material.getMaterial(Integer.parseInt(nameOrId));
    } catch (NumberFormatException e){ /* ignore */ }
    return Option.apply(m);
  }

  public Option<EntityType> findEntity(String name){
    EntityType e = EntityType.fromName(name.toUpperCase());
    if(e == null) e = EntityType.valueOf(name.toUpperCase());
    return Option.apply(e);
  }

  public ArgParser<Material> material = token("material",
    new AbstractFunction1<String, Option<Material>>() {
      public Option<Material> apply(String s) { return findMaterial(s);};
    }
  );

  public ArgParser<EntityType> entity = token("entity",
    new AbstractFunction1<String, Option<EntityType>>() {
      public Option<EntityType> apply(String s) { return findEntity(s); };
    }
  );

  public ArgParser<Player> player = token("player", new AbstractFunction1<String, Option<Player>>() {
    public Option<Player> apply(String s) {
      return Option.apply(getServer().getPlayer(s));
    };
  });

  public ArgParser<GameMode> gamemode =
    match("c").or(match("creative")).or(match("1")).outputting(GameMode.CREATIVE).or(
    match("s").or(match("survival")).or(match("0")).outputting(GameMode.SURVIVAL));

  // TODO: really just for demonstration. im not going to fill this all in.
  private Player consolePlayer = new Player() {
    public String getDisplayName() { return null; }
    public void setDisplayName(String s) {}
    public String getPlayerListName() { return null; }
    public void setPlayerListName(String s) {}
    public void setCompassTarget(Location location) {}
    public Location getCompassTarget() { return null; }
    public InetSocketAddress getAddress() { return null; }
    public void sendRawMessage(String s) {}
    public void kickPlayer(String s) {}
    public void chat(String s) {}
    public boolean performCommand(String s) { return false; }
    public boolean isSneaking() { return false; }
    public void setSneaking(boolean b) {}
    public boolean isSprinting() { return false; }
    public void setSprinting(boolean b) {}
    public void saveData() {}
    public void loadData() {}
    public void setSleepingIgnored(boolean b) {}
    public boolean isSleepingIgnored() { return false; }
    public void playNote(Location location, byte b, byte b1) {}
    public void playNote(Location location, Instrument instrument, Note note) {}
    public void playEffect(Location location, Effect effect, int i) {}
    public <T> void playEffect(Location location, Effect effect, T t) {}
    public void sendBlockChange(Location location, Material material, byte b) {}
    public boolean sendChunkChange(Location location, int i, int i1, int i2, byte[] bytes) { return false; }
    public void sendBlockChange(Location location, int i, byte b) {}
    public void sendMap(MapView mapView) {}
    public void updateInventory() {}
    public void awardAchievement(Achievement achievement) {}
    public void incrementStatistic(Statistic statistic) {}
    public void incrementStatistic(Statistic statistic, int i) {}
    public void incrementStatistic(Statistic statistic, Material material) {}
    public void incrementStatistic(Statistic statistic, Material material, int i) {}
    public void setPlayerTime(long l, boolean b) {}
    public long getPlayerTime() { return 0; }
    public long getPlayerTimeOffset() { return 0; }
    public boolean isPlayerTimeRelative() { return false; }
    public void resetPlayerTime() {}
    public void giveExp(int i) {}
    public float getExp() { return 0; }
    public void setExp(float v) {}
    public int getLevel() { return 0; }    
    public void setLevel(int i) {}    
    public int getTotalExperience() { return 0; }    
    public void setTotalExperience(int i) {}    
    public float getExhaustion() { return 0; }    
    public void setExhaustion(float v) {}    
    public float getSaturation() { return 0; }    
    public void setSaturation(float v) {}    
    public int getFoodLevel() { return 0; }    
    public void setFoodLevel(int i) {}    
    public Location getBedSpawnLocation() { return null; }    
    public void setBedSpawnLocation(Location location) {}    
    public boolean getAllowFlight() { return false; }    
    public void setAllowFlight(boolean b) {}    
    public void hidePlayer(Player player) {}    
    public void showPlayer(Player player) {}    
    public boolean canSee(Player player) { return false; }    
    public boolean isFlying() { return false; }    
    public void setFlying(boolean b) {}    
    public void setFlySpeed(float v) throws IllegalArgumentException {}    
    public void setWalkSpeed(float v) throws IllegalArgumentException {}    
    public float getFlySpeed() { return 0; }    
    public float getWalkSpeed() { return 0; }    
    public void sendMessage(String s) {}    
    public void sendMessage(String[] strings) {}    
    public Map<String, Object> serialize() { return null; }    
    public boolean isConversing() { return false; }    
    public void acceptConversationInput(String s) {}    
    public boolean beginConversation(Conversation conversation) { return false; }    
    public void abandonConversation(Conversation conversation) {}    
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent conversationAbandonedEvent) {}    
    public String getName() { return null; }    
    public PlayerInventory getInventory() { return null; }    
    public boolean setWindowProperty(InventoryView.Property property, int i) { return false; }    
    public InventoryView getOpenInventory() { return null; }    
    public InventoryView openInventory(Inventory itemStacks) { return null; }    
    public InventoryView openWorkbench(Location location, boolean b) { return null; }    
    public InventoryView openEnchanting(Location location, boolean b) { return null; }    
    public void openInventory(InventoryView inventoryView) {}    
    public void closeInventory() {}    
    public ItemStack getItemInHand() { return null; }    
    public void setItemInHand(ItemStack itemStack) {}    
    public ItemStack getItemOnCursor() { return null; }    
    public void setItemOnCursor(ItemStack itemStack) {}    
    public boolean isSleeping() { return false; }    
    public int getSleepTicks() { return 0; }    
    public GameMode getGameMode() { return null; }    
    public void setGameMode(GameMode gameMode) {}    
    public boolean isBlocking() { return false; }    
    public int getExpToLevel() { return 0; }    
    public int getHealth() { return 0; }    
    public void setHealth(int i) {}    
    public int getMaxHealth() { return 0; }    
    public double getEyeHeight() { return 0; }    
    public double getEyeHeight(boolean b) { return 0; }    
    public Location getEyeLocation() { return null; }    
    public List<Block> getLineOfSight(HashSet<Byte> bytes, int i) { return null; }    
    public Block getTargetBlock(HashSet<Byte> bytes, int i) { return null; }    
    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> bytes, int i) { return null; }    
    public Egg throwEgg() { return null; }    
    public Snowball throwSnowball() { return null; }    
    public Arrow shootArrow() { return null; }    
    public <T extends Projectile> T launchProjectile(Class<? extends T> aClass) { return null; }    
    public int getRemainingAir() { return 0; }    
    public void setRemainingAir(int i) {}
    public int getMaximumAir() { return 0; }    
    public void setMaximumAir(int i) {}
    public void damage(int i) {}
    public void damage(int i, Entity entity) {}
    public int getMaximumNoDamageTicks() { return 0; }    
    public void setMaximumNoDamageTicks(int i) {}
    public int getLastDamage() { return 0; }    
    public void setLastDamage(int i) {}
    public int getNoDamageTicks() { return 0; }    
    public void setNoDamageTicks(int i) {}
    public Player getKiller() { return null; }    
    public boolean addPotionEffect(PotionEffect potionEffect) { return false; }    
    public boolean addPotionEffect(PotionEffect potionEffect, boolean b) { return false; }    
    public boolean addPotionEffects(Collection<PotionEffect> potionEffects) { return false; }    
    public boolean hasPotionEffect(PotionEffectType potionEffectType) { return false; }    
    public void removePotionEffect(PotionEffectType potionEffectType) {}
    public Collection<PotionEffect> getActivePotionEffects() { return null; }    
    public boolean hasLineOfSight(Entity entity) { return false; }    
    public Location getLocation() { return null; }    
    public void setVelocity(Vector vector) {}
    public Vector getVelocity() { return null; }    
    public World getWorld() { return null; }    
    public boolean teleport(Location location) { return false; }    
    public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause teleportCause) { return false; }    
    public boolean teleport(Entity entity) { return false; }    
    public boolean teleport(Entity entity, PlayerTeleportEvent.TeleportCause teleportCause) { return false; }    
    public List<Entity> getNearbyEntities(double v, double v1, double v2) { return null; }    
    public int getEntityId() { return 0; }    
    public int getFireTicks() { return 0; }    
    public int getMaxFireTicks() { return 0; }    
    public void setFireTicks(int i) {}
    public void remove() {}
    public boolean isDead() { return false; }    
    public boolean isValid() { return false; }    
    public Server getServer() { return null; }    
    public Entity getPassenger() { return null; }    
    public boolean setPassenger(Entity entity) { return false; }    
    public boolean isEmpty() { return false; }    
    public boolean eject() { return false; }    
    public float getFallDistance() { return 0; }    
    public void setFallDistance(float v) {}
    public void setLastDamageCause(EntityDamageEvent entityDamageEvent) {}
    public EntityDamageEvent getLastDamageCause() { return null; }    
    public UUID getUniqueId() { return null; }    
    public int getTicksLived() { return 0; }    
    public void setTicksLived(int i) {}
    public void playEffect(EntityEffect entityEffect) {}
    public EntityType getType() { return null; }    
    public boolean isInsideVehicle() { return false; }    
    public boolean leaveVehicle() { return false; }    
    public Entity getVehicle() { return null; }    
    public void setMetadata(String s, MetadataValue metadataValue) {}
    public List<MetadataValue> getMetadata(String s) { return null; }    
    public boolean hasMetadata(String s) { return false; }    
    public void removeMetadata(String s, Plugin plugin) {}
    public boolean isOnline() { return false; }    
    public boolean isBanned() { return false; }    
    public void setBanned(boolean b) {}
    public boolean isWhitelisted() { return false; }    
    public void setWhitelisted(boolean b) {}
    public Player getPlayer() { return null; }    
    public long getFirstPlayed() { return 0; }    
    public long getLastPlayed() { return 0; }    
    public boolean hasPlayedBefore() { return false; }    
    public boolean isPermissionSet(String s) { return false; }    
    public boolean isPermissionSet(Permission permission) { return false; }    
    public boolean hasPermission(String s) { return false; }    
    public boolean hasPermission(Permission permission) { return false; }    
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) { return null; }    
    public PermissionAttachment addAttachment(Plugin plugin) { return null; }    
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) { return null; }    
    public PermissionAttachment addAttachment(Plugin plugin, int i) { return null; }    
    public void removeAttachment(PermissionAttachment permissionAttachment) {}
    public void recalculatePermissions() {}
    public Set<PermissionAttachmentInfo> getEffectivePermissions() { return null; }    
    public void sendPluginMessage(Plugin plugin, String s, byte[] bytes) {}
    public Set<String> getListeningPluginChannels() { return null; }    
    public boolean isOp() { return false; }    
    public void setOp(boolean b) {}
  };
}
