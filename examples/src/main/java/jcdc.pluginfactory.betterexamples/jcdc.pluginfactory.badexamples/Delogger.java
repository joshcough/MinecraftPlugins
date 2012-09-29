package jcdc.pluginfactory.betterexamples.jcdc.pluginfactory.badexamples;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The video for the code for this file is here: http://www.youtube.com/watch?v=ohEJKdqqMXo
 * and the plugin.yml file is here: http://www.youtube.com/watch?v=bONATOUrDig
 */
public class Delogger extends JavaPlugin {
    public final Logger logger = Logger.getLogger("Minecraft");
    public final DeloggerListener listener = new DeloggerListener(this);

    public void onDisable() {
        PluginDescriptionFile pdffile = this.getDescription();
        this.logger.info(pdffile.getName() + " is now disabled.");
    }

    public void onEnable() {
        PluginDescriptionFile pdffile = this.getDescription();
        this.logger.info(pdffile.getName() + " version " + pdffile.getVersion() + " is now enabled.");
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(listener, this);
    }
}
