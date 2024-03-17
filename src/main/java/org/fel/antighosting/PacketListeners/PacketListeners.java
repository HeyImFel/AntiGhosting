package org.fel.antighosting.PacketListeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.fel.antighosting.AntiGhosting;

import java.util.UUID;

import static org.fel.antighosting.AntiGhosting.*;
import static org.fel.antighosting.PlayerData.*;

public class PacketListeners {
    /**
     * listens for Pong response and runs the retot check when received
     */
    public static final PacketListener pongListener = new PacketAdapter(JavaPlugin.getPlugin(AntiGhosting.class), ListenerPriority.NORMAL, PacketType.Play.Client.PONG) {
        @Override
        public void onPacketReceiving(PacketEvent event) {

            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            if ((!packet.getIntegers().read(0).equals(event.getPacket().getIntegers().read(0))) || !toggleState(uuid)) {
                return;
            }
            if (untotState(uuid)) {
                runUntotCheck(player);
                untotState(uuid, false);
                return;
            }
            if (totCheckRun(uuid)) {
                totCheckRun(uuid, false);
                return;
            }
            task(uuid).cancel();
            runRetotCheck(player, false);
        }
    };
}
