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

public class PingPongListeners {
    /**
     * listens for Pong response and runs the retot check when received
     */
    private static final PacketListener pongListener = new PacketAdapter(JavaPlugin.getPlugin(AntiGhosting.class), ListenerPriority.NORMAL, PacketType.Play.Client.PONG) {
        @Override
        public void onPacketReceiving(PacketEvent event) {

            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            if((pingID(uuid) != event.getPacket().getIntegers().read(0))
                    || totCheckRun(uuid)) {
                totCheckRun(uuid, false);
                return;
            }

            task(uuid).cancel();
            runRetotCheck(player);
        }
    };

    /**
     * sets the ID to listen for in the above packet listener
     * Always enabled
     */
    private static final PacketListener pingListener = new PacketAdapter(JavaPlugin.getPlugin(AntiGhosting.class), ListenerPriority.NORMAL, PacketType.Play.Server.PING) {
        @Override
        public void onPacketSending(PacketEvent event) {
            pingID(event.getPlayer().getUniqueId(), event.getPacket().getIntegers().read(0));
        }
    };

    /**
     * getter for ping listener
     * @return ping listener
     */
    public static PacketListener getPingListener() { return pingListener; }
    /**
     * getter for pong listener
     * @return pong listener
     */
    public static PacketListener getPongListener() { return pongListener; }
}