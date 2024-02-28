package org.fel.antighosting;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.fel.antighosting.EventListeners.DeathEvents;

import java.util.Objects;
import java.util.UUID;

import static org.fel.antighosting.PacketListeners.PacketListeners.pongListener;
import static org.fel.antighosting.PlayerData.packet;
import static org.fel.antighosting.PlayerData.untotSlot;

public final class AntiGhosting extends JavaPlugin implements Listener {

    public static ProtocolManager manager;
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
        manager.addPacketListener(pongListener);
        packet.getIntegers().write(0, 8008135);
    }

    /**
     * basic onJoin event to initialize playerData
     *
     * @param join info on player that joined
     */
    @EventHandler
    public void onJoin(PlayerLoginEvent join) {
        PlayerData.setPlayerData(join.getPlayer().getUniqueId(), false, true, false);
    }


    /**
     * checks if a player retotemed in time
     *
     * @param player player to check
     */
    public static void runRetotCheck(Player player) {
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        if (player.getInventory().getItemInMainHand().equals(totem)){
            Bukkit.getPluginManager().callEvent(new PlayerItemConsumeEvent(player, totem, EquipmentSlot.HAND));
            Bukkit.getLogger().info("AntiGhost -> mainhand totem for " + player.getName() + " removed");
            player.sendMessage(color("&c&lAnti-Ghost &8&l▶ &r&7Saved by Anti-Ghost!"));
        }
        else if (player.getInventory().getItemInOffHand().equals(totem)) {
            Bukkit.getPluginManager().callEvent(new PlayerItemConsumeEvent(player, totem, EquipmentSlot.OFF_HAND));
            Bukkit.getLogger().info("AntiGhost -> offhand totem for " + player.getName() + " removed");
            player.sendMessage(color("&c&lAnti-Ghost &8&l▶ &r&7Saved by Anti-Ghost!"));
        }
        else {
            player.setHealth(0);
            Bukkit.getLogger().info("AntiGhost -> player " + player.getName() + " killed by failed retot check");
            player.sendMessage(color("&c&lAnti-Ghost &8&l▶ &r&7you missed the retot timing." +
                    "\nthis was not a ghost, work on your retotem!"));
        }
    }

    /**
     * checks if a player switched off the slot their main hand totem was in before it popped client-side
     * does not properly manage items yet, will implement that later
     *
     * AntiRetotLag will replace this
     * @param player player to check
     */
    public static void runUntotCheck(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        if (untotSlot(uuid) != player.getInventory().getHeldItemSlot()) {
            if (!player.getInventory().getItemInMainHand().equals(totem)) {
                Bukkit.getLogger().info("AntiGhost -> player " + player.getName() + " killed by failed untot check");
                player.sendMessage(color("&c&lAnti-Ghost &8&l▶ &r&7You switched off your main hand totem too early."));
                player.getInventory().addItem(totem);
                player.setHealth(0.0);
            }
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
}
