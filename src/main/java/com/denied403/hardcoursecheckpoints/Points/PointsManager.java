package com.denied403.hardcoursecheckpoints.Points;

import com.denied403.hardcoursecheckpoints.Utils.CheckpointDatabase;

import java.util.UUID;

public class PointsManager {

    private static CheckpointDatabase database;
    public static void initialize(CheckpointDatabase db) {
        database = db;
    }

    public static int getPoints(UUID playerUUID) {
        return database.getPoints(playerUUID);
    }

    public void setPoints(UUID playerUUID, int points) {
        database.setPoints(playerUUID, points);
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
