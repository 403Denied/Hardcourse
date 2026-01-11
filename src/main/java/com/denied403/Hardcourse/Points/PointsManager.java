package com.denied403.Hardcourse.Points;

import java.util.UUID;

import static com.denied403.Hardcourse.Hardcourse.checkpointDatabase;

public class PointsManager {

    public static int getPoints(UUID playerUUID) {
        return checkpointDatabase.getPoints(playerUUID);
    }

    public void setPoints(UUID playerUUID, int points) {
        checkpointDatabase.setPoints(playerUUID, points);
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
