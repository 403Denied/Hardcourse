package com.denied403.hardcoursecheckpoints.Points;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PointsManager {

    private final HardcourseCheckpoints plugin;

    public PointsManager(HardcourseCheckpoints plugin) {
        this.plugin = plugin;
    }

    public static int getPoints(UUID playerUUID) {
        return HardcourseCheckpoints.playerPoints.getOrDefault(playerUUID, 0);
    }

    public void setPoints(UUID playerUUID, int points) {
        HardcourseCheckpoints.playerPoints.put(playerUUID, points);
        plugin.savePoints();
    }

    public void addPoints(UUID playerUUID, int amount) {
        int current = getPoints(playerUUID);
        setPoints(playerUUID, current + amount);
    }

    public void removePoints(UUID playerUUID, int amount) {
        int current = getPoints(playerUUID);
        int newAmount = Math.max(0, current - amount);
        setPoints(playerUUID, newAmount);
    }

    public void sendPointsActionBar(Player player) {
        int points = getPoints(player.getUniqueId());
        String message = ChatColor.RED + "Points: " + ChatColor.WHITE + points;
        player.sendActionBar(message);
    }

    public void sendTemporaryPointsMessage(Player player, String tempMessage, long durationTicks) {
        // Show temporary message immediately
        player.sendActionBar(tempMessage);

        // Schedule task to send the normal points message after the specified delay
        new BukkitRunnable() {
            @Override
            public void run() {
                sendPointsActionBar(player);
            }
        }.runTaskLater(plugin, durationTicks);
    }
}
