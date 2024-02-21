package org.fel.antighosting;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.fel.antighosting.EventListeners.DeathEvents;
import org.fel.antighosting.PacketListeners.PingPongListeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class AntiGhosting extends JavaPlugin implements Listener {

    public static ProtocolManager manager;
    public static final BukkitScheduler scheduler = Bukkit.getScheduler();
    private static final Map<UUID, data> playerData = new HashMap<>();

    /**
     * death state and command toggle state,
     * status of if totem check was already run by Scheduler,
     * ping packet ID to check against pong packet,
     * BukkitTask to be cancelled when Pong packet is received
     */
    public static class data {
        public boolean diedToExplosion;
        public boolean toggleState; //to be removed, can think of better ways to toggle the plugin on and off
        public boolean totCheckRun;
        public int checkPingPong;
        public BukkitTask playerTask;
        public data (boolean death, boolean toggle, boolean tot) {
            diedToExplosion = death;
            toggleState = toggle;
            totCheckRun = tot;

        }
    }
    /**
     * onEnable, registers events and commands
     * and initializes protocol manager to add
     * one of the two packet listeners
     */
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new DeathEvents(), this);
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("antighost")).setExecutor(new Command());
        manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(PingPongListeners.getPingListener());
        manager.addPacketListener(PingPongListeners.getPongListener());
    }

    /**
     * basic onJoin event to initialize playerData
     *
     * @param join info on player that joined
     */
    @EventHandler
    public void onJoin(PlayerLoginEvent join) {
        data tempData = new data(false, true, false);
        playerData.put(join.getPlayer().getUniqueId(), tempData);
    }


    /**
     * checks if a player retotemed in time
     *
     * @param player player to check
     */
    public static void runRetotCheck(Player player) {
        if (player.getInventory().getItemInMainHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))){
            Objects.requireNonNull(player.getEquipment()).setItemInMainHand(null);
            player.sendMessage(color("&c&lAnti-Ghost &8&l▶ &r&7Saved by Anti-Ghost!"));
        }
        else if (player.getInventory().getItemInOffHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))) {
            Objects.requireNonNull(player.getEquipment()).setItemInOffHand(null);
            player.sendMessage(color("&c&lAnti-Ghost &8&l▶ &r&7Saved by Anti-Ghost!"));
        }
        else {
            player.setHealth(0);
            player.sendMessage(color("&c&lAnti-Ghost &8&l▶ &r&7you missed the retot timing." +
                    "\nthis was not a ghost, work on your retotem!"));
        }
    }

    /**
     * colorizes shit idk im lazy
     *
     * @param string string to colorize
     */
    public static String color(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }


    /**
     * gets the death state of the player (true if dead false if not)
     * @param player uuid of player info to get
     * @return death state
     */
    public static boolean deathState (UUID player) {
        return playerData.get(player).diedToExplosion;
    }
    /**
     * sets the players specific latency timeout retotem check task
     * @param player uuid of player info to get
     * @return toggle state
     */
    public static boolean toggleState (UUID player) {
        return playerData.get(player).toggleState;
    }
    /**
     * sets the players specific latency timeout retotem check task
     * @param player uuid of player info to get
     * @return whether tot check was run
     */
    public static boolean totCheckRun (UUID player) {
        return playerData.get(player).totCheckRun;
    }
    /**
     * gets the BukkitTask scheduled to run the retotem check after latency timeout
     * @param player uuid of player info to get
     * @return task to modify or cancel
     */
    public static BukkitTask task(UUID player) {
        return playerData.get(player).playerTask;
    }
    /**
     * gets the ID of the Ping packet to be checked against the Pong packet
     * @param player uuid of player info to get
     * @return ID of Ping packet
     */
    public static int pingID(UUID player) {
        return playerData.get(player).checkPingPong;
    }


    /**
     * sets deathState of player (true if dead, false if not)
     * @param player uuid of player info to set
     * @param in info to set
     */
    public static void deathState (UUID player, boolean in) {
        playerData.get(player).diedToExplosion = in;
    }
    /**
     * sets the command toggle state
     * @param player uuid of player info to set
     * @param in info to set
     */
    public static void toggleState (UUID player, boolean in) {
        playerData.get(player).toggleState = in;
    }
    /**
     * checks if the retotem check has already been run
     * @param player uuid of player info to set
     * @param in info to set
     */
    public static void totCheckRun (UUID player, boolean in) {
        playerData.get(player).totCheckRun = in;
    }
    /**
     * sets the players specific latency timeout retotem check task
     * @param player uuid of player info to set
     * @param in task
     */
    public static void task(UUID player, BukkitTask in) {
        playerData.get(player).playerTask = in;
    }
    /**
     * sets the Ping packet ID to later be checked against Pong packet ID
     * @param player uuid of player info to set
     * @param in ID to set
     */
    public static void pingID(UUID player, int in) {
        playerData.get(player).checkPingPong = in;
    }
}
