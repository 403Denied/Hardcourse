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

public class getLevel implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 0) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rPlease specify a player!"));
            return true;
        }
        String playerName = strings[0];
        Player p = Bukkit.getPlayer(playerName);
        UUID uuid = p != null ? p.getUniqueId() : Bukkit.getOfflinePlayer(playerName).getUniqueId();
        Double level = HardcourseCheckpoints.getHighestCheckpoint(uuid);
        if (level == null) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rPlayer not found or has no checkpoints!"));
            return true;
        }
        String levelString = level.toString().replace(".0", "");
        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &c" + playerName + "&f's level is: &c" + levelString));
        return true;
    }
}
