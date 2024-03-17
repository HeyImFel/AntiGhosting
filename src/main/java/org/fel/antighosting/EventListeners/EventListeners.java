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
import static org.fel.antighosting.PlayerData.*;

public class EventListeners implements Listener {
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


        deathState(uuid, player.getHealth() <= death.getFinalDamage()
                && (death.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
                || death.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)));
        
    }
    /**
     * if player is already holding a totem, don't do anything. If player is not,
     * create a Ping packet and listen for its Pong response and schedule an event
     * this event will ALWAYS trigger *after* the EntityDamageEvent
     *
     * @param pop when player takes fatal damage
     */
    @EventHandler
    public void onPop (EntityResurrectEvent pop) {
        if (!(pop.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) pop.getEntity();
        UUID uuid = player.getUniqueId();
        if (!deathState(uuid) || !toggleState(uuid)) {
            return;
        }

        if (player.getInventory().getItemInOffHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))) {
            return;
        }
        if (player.getInventory().getItemInMainHand().equals(new ItemStack(Material.TOTEM_OF_UNDYING))) {
            untotSlot(uuid, player.getInventory().getHeldItemSlot());
            untotState(uuid, true);
            manager.sendServerPacket(player, packet);
            return;
        }
        pop.setCancelled(false);

        manager.sendServerPacket(player, packet);
        scheduler.runTaskLater(JavaPlugin.getPlugin(AntiGhosting.class), () -> {
            runRetotCheck(player, true);
            totCheckRun(uuid, true);
        }, 7L);

        task(uuid, scheduler.getPendingTasks().get(scheduler.getPendingTasks().size() - 1));
    }

}
