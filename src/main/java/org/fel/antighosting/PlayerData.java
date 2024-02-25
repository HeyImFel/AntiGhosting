package org.fel.antighosting;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    public static final BukkitScheduler scheduler = Bukkit.getScheduler();

    public static class data {
        public boolean diedToExplosion;
        public boolean toggleState; //to be removed, can think of better ways to toggle the plugin on and off
        public boolean totCheckRun;
        public boolean checkUntotemed;
        public int checkPingPong;
        public int untotSlot;
        public BukkitTask playerTask;
        public data(boolean death, boolean toggle, boolean tot) {
            diedToExplosion = death;
            toggleState = toggle;
            totCheckRun = tot;

        }

    }
    private static final Map<UUID, data> playerData = new HashMap<>();

    /**
     * acts as constructor
     * @param uuid uuid of player
     * @param death death state (should always be "false" on initialization)
     * @param toggle toggle state of command (should always be "true" on initialization)
     * @param tot if the retotem check was run (should always be "false" on initialization)
     */
    public static void setPlayerData(UUID uuid, boolean death, boolean toggle, boolean tot) {
        data tempData = new data(death, toggle, tot);
        playerData.put(uuid, tempData);
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
     * gets whether the plugins effects have been toggled on or off
     * @param player uuid of player info to get
     * @return toggle state
     */
    public static boolean toggleState (UUID player) {
        return playerData.get(player).toggleState;
    }
    /**
     * gets whether the player has already been retot checked
     * @param player uuid of player info to get
     * @return whether tot check was run
     */
    public static boolean totCheckRun (UUID player) {
        return playerData.get(player).totCheckRun;
    }
    /**
     * gets the status of the untotem check
     * @param player uuid of player info to get
     * @return whether tot check was run
     */
    public static boolean untotState (UUID player) {
        return playerData.get(player).checkUntotemed;
    }
    /**
     * gets the BukkitTask scheduled to run the retotem check after 350ms latency timeout
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
     * sets the slot the player was holding when they popped server-side
     * @param player uuid of player to set
     * @return held slot
     */
    public static int untotSlot(UUID player) {
        return playerData.get(player).untotSlot;
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
     * sets if the retotem check has already been run
     * @param player uuid of player info to set
     * @param in info to set
     */
    public static void totCheckRun (UUID player, boolean in) {
        playerData.get(player).totCheckRun = in;
    }
    public static void untotState (UUID player, boolean in) {
        playerData.get(player).checkUntotemed = in;
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

    /**
     * sets the slot the player was holding when they popped server-side
     * @param player uuid of player to set
     * @param in held slot
     */
    public static void untotSlot(UUID player, int in) {
        playerData.get(player).untotSlot = in;
    }
}