package jcdc.pluginfactory.badexamples;

import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.EventHandler;

//https://github.com/insanj/BlockChanger/blob/master/me/insanj/BlockChanger/BlockChangerListener.java
public class BlockChangerListener implements Listener
{
    public static BlockChanger plugin;

    public BlockChangerListener(BlockChanger instance)
    {
        plugin = instance;
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event)
    {
        if(plugin.enabled(event.getPlayer()))
            event.getBlock().setTypeId(plugin.id);
    }

}