package com.denied403.hardcoursecheckpoints.Points;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;

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
}
