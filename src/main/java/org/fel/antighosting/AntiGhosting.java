package org.fel.antighosting;

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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.fel.antighosting.customEvents.CommandEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class AntiGhosting extends JavaPlugin implements Listener {
    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final Map<UUID, data> playerData = new HashMap<>();
    /**
     * death state and command toggle state
     */
    private static class data {
        public boolean diedToExplosion;
        public boolean toggleState;
        public data (boolean death, boolean toggle) {
            diedToExplosion = death;
            toggleState = toggle;
        }
    }

    /**
     * basic onEnable
     */
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("antighost")).setExecutor(new Command());
    }

    /**
     * basic onJoin event to initialize playerData
     *
     * @param join info on player that joined
     */
    @EventHandler
    public void onJoin(PlayerLoginEvent join) {
        data tempData = new data(false, true);
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
     * Gets ping and sets delay on death to that ping. If the player holds a totem in their main hand before
     * the ping timer runs out, they will have the totem they equipped removed (or "popped")
     * this event will ALWAYS trigger *after* the EntityDamageEvent
     *
     * @param death when player takes fatal damage
     */
    @EventHandler
    public void onDeath (EntityResurrectEvent death) {
        if (!(death.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) death.getEntity();
        if ((player.getInventory().getItemInOffHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))
                || player.getInventory().getItemInMainHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))
                || player.getPing() <= 25)
                || !playerData.get(player.getUniqueId()).diedToExplosion) {
            return;
        }

        int ping = player.getPing();//PLACEHOLDER, must use total round trip ping in final version
        death.setCancelled(false);
        scheduler.runTaskLater(this, () -> {
            if (player.getInventory().getItemInMainHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))){
                Objects.requireNonNull(player.getEquipment()).setItemInMainHand(null);
                player.sendMessage(color("&c&lAnti-Ghost &8&l▶ &r&7Saved by Anti-Ghost!"));
            }
            else if (player.getInventory().getItemInOffHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))) {
                Objects.requireNonNull(player.getEquipment()).setItemInOffHand(null);
                player.sendMessage(color("&c&lAnti-Ghost &8&l▶ &r&7Saved by Anti-Ghost!"));
            }
            else {
                player.setHealth(0.0);
                player.sendMessage(color("&c&lAnti-Ghost &8&l▶ &r&7you missed the retot timing." +
                        "\nthis was not a ghost, work on your retotem!"));
            }
        }, (long)Math.ceil((float)ping/50));
    }

    /**
     * runs when "AntiGhost" command is run
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
     * thanks to some random guy on spigot forums for this
     * colorizes shit idk im lazy
     *
     * @param string string to colorize
     */
    public String color(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
