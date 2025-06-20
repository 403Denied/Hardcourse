package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.setHighestCheckpoint;

public class setLevel implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!commandSender.hasPermission("hardcourse.admin")){
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rYou do not have permission to use this command!"));
            return true;
        }
        if (strings.length < 2) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rPlease specify a player and a number!"));
            return true;
        }
        String playerName = strings[0];
        Double level = (Double.parseDouble(strings[1]));
        if(level < 1){
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rThe level must be at least 1!"));
            return true;
        }
        Player p = Bukkit.getPlayer(playerName);
        UUID uuid = p != null ? p.getUniqueId() : Bukkit.getOfflinePlayer(playerName).getUniqueId();
        setHighestCheckpoint(uuid, level);
        if (p != null) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rYour level has been set to " + level.toString().replace(".0", "") + "!"));
        }
        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rThe level of " + playerName + " has been set to " + level.toString().replace(".0", "") + "!"));
        return true;
    }
}
