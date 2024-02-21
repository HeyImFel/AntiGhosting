package org.fel.antighosting.EventListeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.fel.antighosting.AntiGhosting;

import java.util.UUID;

import static org.fel.antighosting.AntiGhosting.*;

public class DeathEvents implements Listener {
    /**
     * Checks if the player's death was caused by an explosion. if it was caused by an explosion,
     * the players "diedToExplosion" state is set positive.
     * this event will ALWAYS trigger *before* the EntityResurrectEvent.
     *
     * @param death damage event
     */
    @EventHandler
    public void dmgDeath(EntityDamageEvent death) {
        death.setCancelled(false);
        if (!(death.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) death.getEntity();
        UUID uuid = player.getUniqueId();

        if ((death.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
                || death.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION))
                && toggleState(uuid)) {
            deathState(uuid, player.getHealth() <= death.getFinalDamage());
        }
        else {
            deathState(uuid, false);
        }
    }
    /**
     * if player is already holding a totem, don't do anything. If player is not,
     * create a Ping packet and listen for its Pong response and schedule an event
     * this event will ALWAYS trigger *after* the EntityDamageEvent
     *
     * @param death when player takes fatal damage
     */
    @EventHandler
    public void onPop (EntityResurrectEvent death) {
        if (!(death.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) death.getEntity();
        UUID uuid = player.getUniqueId();

        if ((player.getInventory().getItemInOffHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))
                || player.getInventory().getItemInMainHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))
                /*|| player.getPing() <= 25 NOT NECESSARY, only here to remind myself to re-implement something similar later*/)
                || !deathState(uuid)) {
            return;
        }
        death.setCancelled(false);

        manager.sendServerPacket(player, new PacketContainer(PacketType.Play.Server.PING));
        scheduler.runTaskLater(JavaPlugin.getPlugin(AntiGhosting.class), () -> {
            runRetotCheck(player);
            totCheckRun(uuid, true);
        }, 7L);

        task(uuid, scheduler.getPendingTasks().get(scheduler.getPendingTasks().size() - 1));
    }

}
