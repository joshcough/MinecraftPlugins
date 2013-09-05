package com.joshcough.minecraft;

import org.bukkit.event.HandlerList;

public class NetLogoEventJ extends org.bukkit.event.Event {
    private static final HandlerList handlers = new HandlerList();
    public final NetLogoEvent event;
    public NetLogoEventJ(NetLogoEvent event) { this.event = event; }
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
