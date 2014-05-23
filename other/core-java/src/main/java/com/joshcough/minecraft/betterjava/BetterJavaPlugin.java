package com.joshcough.minecraft.betterjava;

import org.bukkit.*;
import static org.bukkit.ChatColor.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.*;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import javax.persistence.PersistenceException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.Logger;

import static com.joshcough.minecraft.betterjava.JavaParsers.*;

public class BetterJavaPlugin extends JavaPlugin {
  public final Logger logger = Logger.getLogger("Minecraft");

  public List<Listener> listeners = new ArrayList<>();
  public List<Command>  commands  = new ArrayList<>();

  public void onEnable() {
    super.onEnable();
    setupDatabase();
    for (Listener l : listeners) { register(l); }
    for(Command c: commands) { info(c.name); }
    info("version " + getVersion() + " is now enabled.");
  }

  public void onDisable() {
    super.onDisable();
    info(getDescription().getName() + " is now disabled.");
  }

  public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd,
                           String commandLabel, String[] args) {
    info("in onCommand, command is: " + commandLabel);
    boolean handled = false;
    Player p = sender instanceof Player ? (Player)sender : consolePlayer;
    for(Command c: commands){
      info("in loop, c.name=" + c.name);
      if(c.name.toLowerCase().equals(commandLabel.toLowerCase())){
        try {
          c.body.parseAndRun(p, args);
          handled = true;
        } catch (Exception e){
          if(e.getMessage() != null) p.sendMessage(e.getMessage());
          e.printStackTrace();
        }
      }
    }
    return handled;
  }

  public String pluginName(){
    try{ return this.getDescription().getName(); }
    catch (Exception e) { return this.getClass().getSimpleName(); }
  }

  /**
   * Generates the plugin.yml contents for this plugin.
   * See http://wiki.bukkit.org/Plugin_YAML for more info
   * @param author  the author  of the plugin
   * @param version the version of the plugin
   **/
  public String yml(String author, String version){
    return
      "name: "      + this.pluginName() + "\n" +
      "main: "      + this.getClass().getName()  + "\n" +
      "author: "    + author + "\n" +
      "version: "   + version + "\n" +
      "database: "  + (this.getDatabaseClasses().size() > 0) + "\n" +
      "depend: ["   + mkString(this.dependencies, ", ") + "]\n" +
      "commands:\n" + commandsYml();
  }

  private String commandYml(Command c){
    return
        "  " + c.name + ":\n" +
        "    description: " + c.description + "\n" +
        "    usage: " + "TODO - implement parser descriptions" + "\n";
  }

  private String commandsYml(){
    StringBuilder s = new StringBuilder();
    for(Command c: commands) { s.append(commandYml(c)); }
    return s.toString();
  }

  // holy shit.
  private static <T> String mkString(List<T> list, String separator) {
    StringBuilder s = new StringBuilder();
    Iterator<T> it = list.iterator();
    if (it.hasNext()) { s.append(it.next()); }
    while (it.hasNext()) { s.append(separator).append(it.next()); }
    return s.toString();
  }

  public void writeYML(String author, String version){
    String ymlContents = this.yml(author, version);
    writeResourcesFile(this.pluginName().toLowerCase() + ".yml", ymlContents);
    writeResourcesFile("plugin.yml", ymlContents);
  }

  private File resources = new java.io.File("./src/main/resources");
  private void writeResourcesFile(String filename, String contents){
    resources.mkdir();
    writeFile(new File(resources, filename), contents);
  }
  private void writeFile(File f, String contents){
    try {
      FileWriter fw = new FileWriter(f);
      fw.write(contents);
      fw.close();
    } catch (IOException e) { throw new RuntimeException(e); }
  }

  /**
   */
  private List<String> dependencies = new java.util.ArrayList<>();
  public void addDependency(String d){ dependencies.add(d); }

  /**
   * Classes that want to use a database should override this def, providing
   * all of the Entity classes. See WarpPlugin in examples.
   */
  private List<Class<?>> dbClasses = new java.util.ArrayList<>();
  public void addDbClass(Class<?> c){ dbClasses.add(c); }
  public List<Class<?>> getDatabaseClasses(){ return dbClasses; }


  // this is horrible bukkit nonsense that every plugin must do if it wants to use the database.
  private void setupDatabase(){
    if(! getDatabaseClasses().isEmpty()){
      // this somehow forces attempting to initialize the database
      try { getDatabase().find(getDatabaseClasses().get(0)).findRowCount(); }
      // and if it throws... that means you haven't yet initialized the db,
      // and you need to call installDLL...
      // really, this is just crap. happy to hide it from any users.
      catch(PersistenceException e) { installDDL(); }
    }
  }

  public void register(Listener l){
    getServer().getPluginManager().registerEvents(l, this);
  }

  public void info(String message) { logger.info("[" + pluginName() + "] " + message); }

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

  static public boolean isHolding(Player p, Material m){
    return p.getItemInHand().getType() == m;
  }

  static public ItemStack itemStack(Material m){ return new ItemStack(m, 1); }

  static public abstract class LeftClickBlockHandler implements Listener {
    @EventHandler
    public void on_Dont_Call_Me_Directly(PlayerInteractEvent event){
      if(event.getAction() == Action.LEFT_CLICK_BLOCK)
        onLeftClickBlock(event.getPlayer(), event);
    }
    abstract public void onLeftClickBlock(Player p, PlayerInteractEvent event);
  }

  static public abstract class RightClickBlockHandler implements Listener {
    @EventHandler
    public void on_Dont_Call_Me_Directly(PlayerInteractEvent event){
      if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
        onRightClickBlock(event.getPlayer(), event);
    }
    abstract public void onRightClickBlock(Player p, PlayerInteractEvent event);
  }

  abstract public class CommandBody<T>{
    private Parser<T> parser;
    public CommandBody(Parser<T> parser){ this.parser = parser; }
    abstract public void run(Player p, T t);
    public void parseAndRun(Player p, String[] args){
      parser.parse(args).foldVoid(
        err -> p.sendMessage(err),
        (t, rest) -> run(p, t)
      );
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

  public Parser<Material> material = token("material", this::findMaterial);

  public Parser<EntityType> entity = token("entity", this::findEntity);

  public Parser<Player> player = token("player", s -> Option.apply(getServer().getPlayer(s)));

  public Parser<GameMode> gamemode =
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
    public void playSound(Location location, Sound sound, float v, float v1) {}
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
    public void setPlayerWeather(WeatherType weatherType) {}
    public WeatherType getPlayerWeather() { return null; }
    public void resetPlayerWeather() {}
    public void giveExp(int i) {}
    public void giveExpLevels(int i) {}
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
    public void setBedSpawnLocation(Location location, boolean b) {}
    public boolean getAllowFlight() { return false; }
    public void setAllowFlight(boolean b) {}    
    public void hidePlayer(Player player) {}    
    public void showPlayer(Player player) {}    
    public boolean canSee(Player player) { return false; }
    public boolean isOnGround() { return false; }
    public boolean isFlying() { return false; }
    public void setFlying(boolean b) {}    
    public void setFlySpeed(float v) throws IllegalArgumentException {}    
    public void setWalkSpeed(float v) throws IllegalArgumentException {}    
    public float getFlySpeed() { return 0; }    
    public float getWalkSpeed() { return 0; }
    public void setTexturePack(String s) {}
    public Scoreboard getScoreboard() { return null; }
    public void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {}
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
    public Inventory getEnderChest() { return null; }
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
    public double getHealth() { return 0; }
    public void setHealth(int i) {}    
    public double getMaxHealth() { return 0; }
    public void setMaxHealth(int i) {}
    public void resetMaxHealth() {}
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
    public double getLastDamage() { return 0; }
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
    public boolean getRemoveWhenFarAway() { return false; }
    public void setRemoveWhenFarAway(boolean b) {}
    public EntityEquipment getEquipment() { return null; }
    public void setCanPickupItems(boolean b) {}
    public boolean getCanPickupItems() { return false; }
    public void setCustomName(String s) {}
    public String getCustomName() { return null; }
    public void setCustomNameVisible(boolean b) {}
    public boolean isCustomNameVisible() { return false; }
    public Location getLocation() { return null; }
    public Location getLocation(Location location) { return getLocation(); }
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

    // 1.7 update
    public void _INVALID_damage(int i, org.bukkit.entity.Entity e){}
    public void _INVALID_damage(int i){}
    public int _INVALID_getHealth() { return 0; }
    public int _INVALID_getMaxHealth() { return 0; }
    public void _INVALID_setHealth(int i){}
    public void _INVALID_setMaxHealth(int i){}
    public void damage(double d, org.bukkit.entity.Entity e){}
    public void damage(double d){}
    public void setHealth(double d){}
    public void setMaxHealth(double d){}
    // Members declared in org.bukkit.entity.LivingEntity
    public int _INVALID_getLastDamage(){ return 0; }
    public void _INVALID_setLastDamage(int i){}
    public org.bukkit.entity.Entity getLeashHolder(){ return null; }
    public boolean isLeashed(){ return false; }
    public void setLastDamage(double d){}
    public boolean setLeashHolder(org.bukkit.entity.Entity e){ return false; }
    // Members declared in org.bukkit.entity.Player
    public double getHealthScale(){ return 0; }
    public boolean isHealthScaled(){ return false; }
    public void playSound(org.bukkit.Location l, String s, float f1, float f2){}
    public void setHealthScale(double d){}
    public void setHealthScaled(boolean b){}
    public void setResourcePack(String s){}
  };
}
