package com.denied403.hardcoursecheckpoints.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class stuck implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player p){
            p.setHealth(0);
            return true;
        } else {
            commandSender.sendMessage("§c§lHARDCOURSE §fThis command can only be used by players.");
        }
        return false;
    }
}
