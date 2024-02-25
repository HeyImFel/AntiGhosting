package org.fel.antighosting;


import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static org.fel.antighosting.PlayerData.*;


public class Command implements CommandExecutor {
    /**
     * runs when "AntiGhost" command is run
     *
     * Will be modified, remnant of old player.getPing based solution
     *
     * @param sender sender of command (player, console, whatever)
     * @param cmd command details
     * @param label dnc
     * @param args dnc
     *
     */
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        if (cmd.getName().equalsIgnoreCase("antighost")) {
            toggleState(uuid, !toggleState(uuid));

            String outMsg = "&c&lAnti-Ghost &8&l▶ &r&7";

            if(toggleState(uuid)) {
                outMsg += "Anti-Ghost enabled";
            }
            else {
                outMsg += "Anti-Ghost disabled";
            }

            player.sendMessage(AntiGhosting.color(outMsg));
        }
        return true;
    }
}
