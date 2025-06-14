package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class restartForUpdate implements CommandExecutor {

    private final HardcourseCheckpoints plugin;

    public restartForUpdate(HardcourseCheckpoints plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hardcourse.admin")) {
            sender.sendMessage("&cYou do not have permission to run this command!");
            return true;
        }
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &fThe server will be restarting to apply updates in &c30 seconds&f. Please find a safe stopping point."));

        new BukkitRunnable() {
            int timeLeft = 30;

            @Override
            public void run() {
                if (timeLeft == 15 || (timeLeft <= 5 && timeLeft > 0)) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &fRestarting in &c" + timeLeft + " second" + (timeLeft == 1 ? "" : "s") + "&f..."));
                }

                if (timeLeft == 0) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &fRestarting now... The server will be back online shortly."));
                    cancel();
                    return;
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 20L, 20L); // Runs every second (20 ticks)

        return true;
    }
}
