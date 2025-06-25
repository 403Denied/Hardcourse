package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.highestCheckpoint;

public class CheckpointCommands implements CommandExecutor {

    private final HardcourseCheckpoints plugin;

    public CheckpointCommands(HardcourseCheckpoints plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.isOp()) && !sender.hasPermission("hardcourse.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("resetcheckpoint")) {
            return handleResetCheckpoint(sender, args);
        } else if (command.getName().equalsIgnoreCase("resetallcheckpoints")) {
            return handleResetAllCheckpoints(sender, args);
        } else if( command.getName().equalsIgnoreCase("purgeinactive")) {
            return handlePurgeInactiveCheckpoints(sender, args);
        }

        return false;
    }

    private boolean handleResetCheckpoint(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /resetcheckpoint <player> confirm");
            return true;
        }

        String playerName = args[0];
        String confirmation = args[1];

        if (!confirmation.equalsIgnoreCase("confirm")) {
            sender.sendMessage(ChatColor.RED + "Please confirm the reset by typing: /resetcheckpoint " + playerName + " confirm");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer != null) {
            UUID playerUUID = targetPlayer.getUniqueId();
            if (highestCheckpoint.containsKey(playerUUID)) {
                highestCheckpoint.remove(playerUUID);
                plugin.saveCheckpoints();  // Save changes immediately
                sender.sendMessage(ChatColor.GREEN + "Checkpoint for player " + playerName + " has been reset.");
            } else {
                sender.sendMessage(ChatColor.RED + "Player " + playerName + " does not have a recorded checkpoint.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Player " + playerName + " is not online or does not exist.");
        }

        return true;
    }

    private boolean handleResetAllCheckpoints(CommandSender sender, String[] args) {
        if (args.length < 1 || !args[0].equalsIgnoreCase("confirm")) {
            sender.sendMessage(ChatColor.RED + "Please confirm the reset by typing: /resetallcheckpoints confirm");
            return true;
        }

        highestCheckpoint.clear();
        plugin.saveCheckpoints();  // Save changes immediately
        sender.sendMessage(ChatColor.GREEN + "All player checkpoints have been reset.");
        return true;
    }
    private boolean handlePurgeInactiveCheckpoints(CommandSender sender, String[] args) {
        if (args.length < 1 || !args[0].equalsIgnoreCase("confirm")) {
            sender.sendMessage(ChatColor.RED + "Please confirm the purge by typing: /purgeinactive confirm");
            return true;
        }
        //remove checkpoints equal to 1.0
        highestCheckpoint.entrySet().removeIf(entry -> entry.getValue().equals(1.0));
        highestCheckpoint.entrySet().removeIf(entry -> entry.getKey().equals(0.0));
        plugin.saveCheckpoints();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &fAll inactive checkpoints have been purged."));
        return true;
    }
}
