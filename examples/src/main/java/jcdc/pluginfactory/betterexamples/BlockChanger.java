package jcdc.pluginfactory.betterexamples;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import scala.Option;

public class BlockChanger extends BetterJavaPlugin {
  public final Map<Player, Material> BlockChangerUsers = new HashMap<Player, Material>();

  public BlockChanger() {
    listeners.add(new Listener() {
      @EventHandler
      public void onBlockDamage(BlockDamageEvent event) {
        if (BlockChangerUsers.containsKey(event.getPlayer())) {
          event.getBlock().setTypeId(BlockChangerUsers.get(event.getPlayer()).getId());
        }
      }
    });

    commands.add(new Cmd(
        "bc",
        "Hit blocks to change them to the block with type blockId, or just /bc to turn it off.",
        new CommandBody<Option<Material>>(opt(material)) {
          public void run(Player p, Option<Material> om) {
            if (om.isDefined()) {
              BlockChangerUsers.put(p, om.get());
              p.sendMessage(ChatColor.BLUE + "BlockChanger using: " + om.get().name());
            } else {
              BlockChangerUsers.remove(p);
              p.sendMessage(ChatColor.RED + "BlockChanger has been disabled!");
            }
          }
        }
    ));
  }
}
