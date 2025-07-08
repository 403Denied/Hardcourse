package com.denied403.hardcoursecheckpoints.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WinnerTP implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(sender instanceof Player p)){
            sender.sendMessage("This command can only be used by players.");
            return false;
        }
        if(!p.hasPermission("hardcourse.winner")){
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rYou do not have permission to use this command!"));
            return false;
        }
        if(strings.length == 0){
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rPlease specify a player!"));
            return false;
        }
        String playerName = strings[0];
        Player target = p.getServer().getPlayerExact(playerName);
        if(target == null){
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rPlayer not found or not online!"));
            return false;
        }
        p.teleport(target);
        return false;
    }
}
