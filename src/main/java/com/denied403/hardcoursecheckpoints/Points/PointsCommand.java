package com.denied403.hardcoursecheckpoints.Points;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PointsCommand implements CommandExecutor {

    private final PointsManager pointsManager;

    public PointsCommand(PointsManager pointsManager) {
        this.pointsManager = pointsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("hardcourse.points.manage")) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE " + ChatColor.WHITE + "You don't have permission to use this command.");
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE " + ChatColor.WHITE + "Usage: /points <player> set|give|remove <amount>");
            return true;
        }

        String targetName = args[0];
        String action = args[1].toLowerCase();
        String amountStr = args[2];

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE " + ChatColor.WHITE + "Player '" + ChatColor.RED + targetName + ChatColor.WHITE + "' not found or not online.");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount < 0) {
                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE " + ChatColor.WHITE + "Amount must be positive.");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE " + ChatColor.WHITE + "Invalid number: " + ChatColor.RED + amountStr);
            return true;
        }

        UUID targetUUID = target.getUniqueId();

        switch (action) {
            case "set":
                pointsManager.setPoints(targetUUID, amount);
                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE " + ChatColor.WHITE + "Set " + ChatColor.RED + targetName + ChatColor.WHITE + "'s points to " + ChatColor.RED + amount + ChatColor.WHITE + ".");
                target.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE " + ChatColor.WHITE + "Your points have been set to " + ChatColor.RED + amount + ChatColor.WHITE + " by " + ChatColor.RED + sender.getName() + ChatColor.WHITE + ".");
                break;

            case "give":
                pointsManager.addPoints(targetUUID, amount);
                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE " + ChatColor.WHITE + "Gave " + ChatColor.RED + amount + ChatColor.WHITE + " points to " + ChatColor.RED + targetName + ChatColor.WHITE + ".");
                target.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE " + ChatColor.WHITE + "You received " + ChatColor.RED + amount + ChatColor.WHITE + " points from " + ChatColor.RED + sender.getName() + ChatColor.WHITE + ".");
                break;

            case "remove":
                pointsManager.removePoints(targetUUID, amount);
                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE " + ChatColor.WHITE + "Removed " + ChatColor.RED + amount + ChatColor.WHITE + " points from " + ChatColor.RED + targetName + ChatColor.WHITE + ".");
                target.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE " + ChatColor.WHITE + ChatColor.RED + amount + ChatColor.WHITE + " points were removed by " + ChatColor.RED + sender.getName() + ChatColor.WHITE + ".");
                break;
            case "view":
                int currentPoints = pointsManager.getPoints(targetUUID);
                if(target == sender){
                    sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE " + ChatColor.WHITE + "You have " + currentPoints + ChatColor.WHITE + " points.");
                } else {
                    sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE " + ChatColor.WHITE + targetName + ChatColor.RED + " has " + currentPoints + ChatColor.WHITE + " points.");
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE" + ChatColor.WHITE + "Invalid action. Use set, give, or remove.");
                break;
        }
        return true;
    }
}
