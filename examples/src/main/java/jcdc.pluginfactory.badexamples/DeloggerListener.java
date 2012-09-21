package jcdc.pluginfactory.badexamples;


import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

//http://www.youtube.com/watch?v=YZdp3hqoWKk
public class DeloggerListener implements Listener {

    public static Delogger plugin;

    public DeloggerListener(Delogger instance) {
        plugin = instance;
    }

    @EventHandler
    public void onBlockBreak (BlockBreakEvent event){
        Material block = event.getBlock().getType();
        Player player = event.getPlayer();
        if(block == Material.LOG && player.getItemInHand().getType().name().toLowerCase().contains("axe")){
            Location blockLocation = event.getBlock().getLocation();
            double y = blockLocation.getBlockY();
            double x = blockLocation.getBlockX();
            double z = blockLocation.getBlockZ();
            World currentWorld = event.getPlayer().getWorld();
            boolean logsLeft = true;
            while(logsLeft == true) {
                y++; // Increment Y Coordinate
                Location blockAbove = new Location(currentWorld, x, y, z);
                Material blockAboveType = blockAbove.getBlock().getType();
                Byte blockAboveData = blockAbove.getBlock().getData();
                if (blockAboveType == Material.LOG){
                    ItemStack droppedItem = new ItemStack(blockAboveType, 1, blockAboveData);
                    currentWorld.playEffect(blockAbove, Effect.SMOKE, 1);
                    blockAbove.getBlock().setType(Material.AIR);
                    currentWorld.dropItem(blockAbove, droppedItem);
                    logsLeft = true;
                }else{
                    logsLeft = false;
                }
            }
        }
    }
}
