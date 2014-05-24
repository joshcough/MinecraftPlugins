package com.joshcough.minecraft.betterexamples;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

import com.joshcough.minecraft.betterjava.*;
import static com.joshcough.minecraft.betterjava.JavaParsers.opt;

public class BlockChangerFinal extends BetterJavaPlugin {
  public final Map<Player, Material> blockChangerUsers = new HashMap<Player, Material>();

  public BlockChangerFinal() {
    listeners.add(new Listener() {
      @EventHandler public void onBlockDamage(BlockDamageEvent event) {
        if (blockChangerUsers.containsKey(event.getPlayer())) {
          event.getBlock().setTypeId(blockChangerUsers.get(event.getPlayer()).getId());
        }
      }
    });

    commands.add(Command(
        "bc",
        "Hit blocks to change them to the block with type blockId, or just /bc to turn it off.",
        opt(material),
        (p, om) -> om.foldV(
          () -> {
            blockChangerUsers.remove(p);
            p.sendMessage(ChatColor.RED + "BlockChanger has been disabled!");
          },
          m -> {
            blockChangerUsers.put(p, om.get());
              p.sendMessage(ChatColor.BLUE + "BlockChanger using: " + m.name());
          }
        )
    ));
  }
}
