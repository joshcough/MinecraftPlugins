package jcdc.pluginfactory.examples;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NetLogoEvent extends Event {
    private final Player player;
    private final int ticksRemaining;
    private final long sleepTime;
    private static final HandlerList handlers = new HandlerList();
    public NetLogoEvent(Player player, int ticksRemaining, long sleepTime) {
        this.player = player;
        this.ticksRemaining = ticksRemaining;
        this.sleepTime = sleepTime;
    }
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
    public int getTicksRemaining() { return ticksRemaining; }
    public Player getPlayer() { return player; }
    public long getSleepTime() { return sleepTime; }
}
