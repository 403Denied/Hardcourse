package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class reloadHardcourseConfig implements CommandExecutor {
    private final HardcourseCheckpoints plugin;
    public reloadHardcourseConfig(HardcourseCheckpoints plugin) {this.plugin = plugin;}
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!commandSender.hasPermission("hardcourse.admin")) {
            commandSender.sendMessage("§c§lHARDCOURSE §fYou do not have permission to use this command.");
            return true;
        }
        plugin.saveCheckpoints();
        plugin.loadBroadcastMessages();
        plugin.reloadConfig();
        plugin.reloadCheckpointsConfig();
        plugin.reloadPointsConfig();
        plugin.reloadWordsConfig();
        commandSender.sendMessage("§c§lHARDCOURSE §fHardcourse Config Reloaded.");
        return true;
    }
}
