package jcdc.pluginfactory.betterexamples.jcdc.pluginfactory.badexamples;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

// original video here: http://www.youtube.com/watch?v=mqZI5wEmhLQ
// https://github.com/insanj/BlockChanger/blob/master/me/insanj/BlockChanger/BlockChanger.java
// plugin.yml here: https://github.com/insanj/BlockChanger/blob/master/plugin.yml
public class BlockChanger extends JavaPlugin
{
    private static final Logger log = Logger.getLogger("Minecraft");
    private final BlockChangerListener blockListener = new BlockChangerListener(this);
    public final ArrayList<Player> BlockChangerUsers = new ArrayList<Player>();

    public int id = 35;

    @Override
    public void onEnable()
    {
        log.info("[BlockChanger] has been enabled!");
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(blockListener, this);
    }

    @Override
    public void onDisable()
    {
        log.info("[BlockChanger] has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if(commandLabel.equalsIgnoreCase("BlockChanger")){
            if(args.length > 0)
                id = Integer.parseInt(args[0]);
            else
                toggleBlockChanger(sender);
        }
        return true;
    }

    private void toggleBlockChanger(CommandSender sender)
    {
        if( !enabled((Player) sender) )
        {
            BlockChangerUsers.add((Player) sender);
            ((Player) sender).sendMessage(ChatColor.BLUE + "BlockChanger has been enabled!");
        }

        else
        {
            BlockChangerUsers.remove((Player) sender);
            ((Player) sender).sendMessage(ChatColor.RED + "BlockChanged has been disabled!");
        }
    }

    public boolean enabled(Player player)
    {
        return BlockChangerUsers.contains(player);
    }
}