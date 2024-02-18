package org.fel.antighosting.customEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;



public class CommandEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    public CommandEvent(Player plyr) {
        player = plyr;
    }
    public Player getPlayer() {
        return player;
    }
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
