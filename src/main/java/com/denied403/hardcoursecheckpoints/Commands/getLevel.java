package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import com.denied403.hardcoursecheckpoints.Utils.PermissionChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.denied403.hardcoursecheckpoints.Utils.PermissionChecker.playerHasPermission;

public class getLevel implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 0) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rPlease specify a player!"));
            return true;
        }
        String playerName = strings[0];
        Player onlineTarget = Bukkit.getPlayerExact(playerName);
        OfflinePlayer p = (onlineTarget != null) ? onlineTarget : Bukkit.getOfflinePlayer(playerName);
        if(p.getName() == null) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rPlayer not found or has never played before!"));
            return true;
        }
        UUID uuid = p.getUniqueId();
        Double level = HardcourseCheckpoints.getHighestCheckpoint(uuid);
        if (level == null) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rPlayer not found or has no checkpoints!"));
            return true;
        }

        String levelString = level.toString().replace(".0", "");
        if(!p.isOnline()) {
            if (playerHasPermission(p.getName(), "hardcourse.season3")) {
                levelString = "3-" + levelString;
            } else if (playerHasPermission(p.getName(), "hardcourse.season2")) {
                levelString = "2-" + levelString;
            } else if (playerHasPermission(p.getName(), "hardcourse.season1")) {
                levelString = "1-" + levelString;
            }
        }
        else {
            switch (onlineTarget.getWorld().getName().toLowerCase()) {
                case "season1" -> levelString = "1-" + levelString;
                case "season2" -> levelString = "2-" + levelString;
                case "season3", "season4" -> levelString = "3-" + levelString;
            }

        }
        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &c" + playerName + "&f's level is: &c" + levelString));
        return true;
    }
}
