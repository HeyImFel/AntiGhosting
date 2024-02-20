package org.fel.antighosting;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.fel.antighosting.customEvents.CommandEvent;

import java.util.*;

public final class AntiGhosting extends JavaPlugin implements Listener {

    private ProtocolManager manager;
    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final Map<UUID, data> playerData = new HashMap<>();
    Plugin plugin = this;

    /**
     * death state and command toggle state,
     * status of if totem check was already run by Scheduler,
     * ping packet ID to check against pong packet,
     * BukkitTask to be cancelled when Pong packet is received
     */
    private static class data {
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
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("antighost")).setExecutor(new Command());
        manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(pingYoinker);
    }

    /**
     * basic onJoin event to initialize playerData
     *
     * @param join info on player that joined
     */
    @EventHandler
    public void onJoin(PlayerLoginEvent join) {
        data tempData = new data(false, true, false);
        tempData.diedToExplosion = false;
        tempData.toggleState = true;
        playerData.put(join.getPlayer().getUniqueId(), tempData);
    }


    /**
     * Checks if the player's death was caused by an explosion. if it was caused by an explosion,
     * the players "diedToExplosion" state is set positive.
     * this event will ALWAYS trigger *before* the EntityResurrectEvent.
     *
     * @param death damage event
     */
    @EventHandler
    public void onPlayerDmgDeath(EntityDamageEvent death) {
        if (!(death.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) death.getEntity();
        UUID uuid = player.getUniqueId();

        if ((death.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
                || death.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION))
                    && playerData.get(uuid).toggleState) {
            playerData.get(uuid).diedToExplosion = player.getHealth() <= death.getFinalDamage();
        }
        else {
            playerData.get(uuid).diedToExplosion = false;
        }
    }


    /**
     * if player is already holding a totem, don't do anything. If player is not,
     * create a Ping packet and listen for its Pong response and schedule an event
     *
     * @param death when player takes fatal damage
     */

    @EventHandler
    public void onDeath (EntityResurrectEvent death) {
        if (!(death.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) death.getEntity();
        UUID uuid = player.getUniqueId();

        if ((player.getInventory().getItemInOffHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))
                || player.getInventory().getItemInMainHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))
                /*|| player.getPing() <= 25 NOT NECESSARY, only here to remind myself to re-implement something similar later*/)
                || !playerData.get(player.getUniqueId()).diedToExplosion) {
            return;
        }
        death.setCancelled(false);

        manager.sendServerPacket(player, new PacketContainer(PacketType.Play.Server.PING));
        manager.addPacketListener(listener);//temp, put in onEnable and replace with filtering

        scheduler.runTaskLater(this, () -> {
            runRetotCheck(player);
            playerData.get(uuid).totCheckRun = true;
        }, 7L);

        playerData.get(uuid).playerTask
                = scheduler.getPendingTasks().get(scheduler.getPendingTasks().size() - 1);
    }

    /**
     * runs when "AntiGhost" command is run
     *
     * Will be modified, I can think of better ways to enable and disable the effects of the plugin but
     * don't want to write them right now
     *
     * @param cmd info on player sending command
     */
    @EventHandler
    public void onCommand(CommandEvent cmd) {

        playerData.get(cmd.getPlayer().getUniqueId()).toggleState
                = !playerData.get(cmd.getPlayer().getUniqueId()).toggleState;

        String outMsg = "&c&lAnti-Ghost &8&l▶ &r&7";

        if(playerData.get(cmd.getPlayer().getUniqueId()).toggleState) {
            outMsg += "Anti-Ghost enabled";
        }
        else {
            outMsg += "Anti-Ghost disabled";
        }

        cmd.getPlayer().sendMessage(color(outMsg));
    }




    /**
     * listens for Pong response and runs the retot check when received
     * Enabled in onDeath method, disables itself upon finishing <-- THIS WILL BE CHANGED TO PACKET FILTERING SOON
     *
     */
    private final PacketListener listener = new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.PONG) {
        @Override
        public void onPacketReceiving(PacketEvent event) {

            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            if((playerData.get(uuid).checkPingPong != event.getPacket().getIntegers().read(0))
                    || playerData.get(uuid).totCheckRun) {
                playerData.get(uuid).totCheckRun = false;
                return;
            }

            playerData.get(uuid).playerTask.cancel();
            runRetotCheck(player);
            manager.removePacketListener(this);//temp, replace with filtering
        }
    };

    /**
     * sets the ID to listen for in the above packet listener
     * Always enabled
     */
    private final PacketListener pingYoinker = new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.PING) {
        @Override
        public void onPacketSending(PacketEvent event) {
            playerData.get(event.getPlayer().getUniqueId()).checkPingPong = event.getPacket().getIntegers().read(0);
        }
    };

    /**
     * checks if a player retotemed in time
     *
     * Would make this whole method run synchronously but for some reason when I tried that it caused problems,
     * will fix at a later date
     *
     * @param player player to check
     */
    public void runRetotCheck(Player player) {

        if (player.getInventory().getItemInMainHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))){
            Objects.requireNonNull(player.getEquipment()).setItemInMainHand(null);
            player.sendMessage(color("&c&lAnti-Ghost &8&l▶ &r&7Saved by Anti-Ghost!"));
        }

        else if (player.getInventory().getItemInOffHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))) {
            Objects.requireNonNull(player.getEquipment()).setItemInOffHand(null);
            player.sendMessage(color("&c&lAnti-Ghost &8&l▶ &r&7Saved by Anti-Ghost!"));
        }

        else {
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                if (player.getInventory().getItemInMainHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))) {
                    Objects.requireNonNull(player.getEquipment()).setItemInMainHand(null);
                }
                else if (player.getInventory().getItemInOffHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))) {
                    Objects.requireNonNull(player.getEquipment()).setItemInOffHand(null);
                }
                player.setHealth(0);
            });
            player.sendMessage(color("&c&lAnti-Ghost &8&l▶ &r&7you missed the retot timing." +
                    "\nthis was not a ghost, work on your retotem!"));
        }

    }

    /**
     * colorizes shit idk im lazy
     *
     * @param string string to colorize
     */
    public String color(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
