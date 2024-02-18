package org.fel.antighosting;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.fel.antighosting.customEvents.CommandEvent;

public class Command implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("antighost")) {
            //toggleState.put(player.getUniqueId(), !toggleState.get(player.getUniqueId()));
            System.out.println("command event triggered");
            Bukkit.getServer().getPluginManager().callEvent(new CommandEvent(player));
        }
        return true;
    }
}
