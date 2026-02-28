package com.denied403.Hardcourse.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.sql.*;
import java.util.*;

import static com.denied403.Hardcourse.Hardcourse.plugin;

public class CheckpointDatabase {
    public CheckpointDatabase() {
        try {
            createCheckpointTable();
            updateSchema();
            createCheckpointLocationsTable();
            updateLocationsSchema();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[HARDCOURSE] Failed to initialize checkpoint DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        File dbFile = new File(plugin.getDataFolder(), "data.db");
        dbFile.getParentFile().mkdirs();
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile);
    }

    private void createCheckpointTable(){
        String sql = """
                CREATE TABLE IF NOT EXISTS checkpoints (
                        uuid TEXT PRIMARY KEY NOT NULL,
                        season INTEGER NOT NULL DEFAULT 0,
                        level REAL NOT NULL DEFAULT 0,
                        points INTEGER NOT NULL DEFAULT 0,
                        level_time INTEGER NOT NULL DEFAULT 0,
                        discord TEXT
                    )
                """;
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[HARDCOURSE] Failed to create checkpoints table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void updateSchema() {
        java.util.Map<String, String> expectedColumns = new java.util.HashMap<>();
        expectedColumns.put("season", "INTEGER NOT NULL DEFAULT 0");
        expectedColumns.put("level", "REAL NOT NULL DEFAULT 0");
        expectedColumns.put("points", "INTEGER NOT NULL DEFAULT 0");
        expectedColumns.put("level_time", "INTEGER NOT NULL DEFAULT 0");
        expectedColumns.put("discord", "TEXT");

        try (Connection conn = getConnection()) {
            Set<String> existingColumns = new HashSet<>();
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("PRAGMA table_info(checkpoints)")) {
                while (rs.next()) {
                    existingColumns.add(rs.getString("name"));
                }
            }
            for (java.util.Map.Entry<String, String> entry : expectedColumns.entrySet()) {
                String columnName = entry.getKey();
                String definition = entry.getValue();

                if (!existingColumns.contains(columnName)) {
                    Bukkit.getLogger().info("Database Migration: Adding missing column '" + columnName + "' to checkpoints table.");
                    try (Statement alterStmt = conn.createStatement()) {
                        alterStmt.executeUpdate("ALTER TABLE checkpoints ADD COLUMN " + columnName + " " + definition + ";");
                    }
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to update player logs table schema:");
            e.printStackTrace();
        }
    }

    public void setCheckpointData(UUID uuid, int season, double level, int points) {
        String sql = """
            INSERT INTO checkpoints (uuid, season, level, points)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(uuid) DO UPDATE SET
                season = excluded.season,
                level = excluded.level,
                points = excluded.points
        """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, season);
            ps.setDouble(3, level);
            ps.setInt(4, points);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to save checkpoint data: " + e.getMessage());
        }
    }

    public CheckpointData getCheckpointData(UUID uuid) {
        String sql = "SELECT season, level, points, level_time, discord FROM checkpoints WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int season = rs.getInt("season");
                    double level = rs.getDouble("level");
                    int points = rs.getInt("points");
                    long level_time = rs.getLong("level_time");
                    String discord = rs.getString("discord");
                    return new CheckpointData(uuid, season, level, points, level_time, discord);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load checkpoint data: " + e.getMessage());
        }
        return null;
    }

    public Integer getSeason(UUID uuid) {
        CheckpointData d = getCheckpointData(uuid);
        return (d != null) ? d.season() : 0;
    }

    public Double getLevel(UUID uuid) {
        CheckpointData d = getCheckpointData(uuid);
        return (d != null) ? d.level() : 0.0;
    }

    public Integer getPoints(UUID uuid) {
        CheckpointData d = getCheckpointData(uuid);
        return (d != null) ? d.points() : 0;
    }

    public void setSeason(UUID uuid, int season) {
        int curSeason = getSeason(uuid);
        double curLevel = getLevel(uuid);
        int curPoints = getPoints(uuid);
        setCheckpointData(uuid, season, curLevel, curPoints);
    }

    public void setLevel(UUID uuid, double level) {
        int curSeason = getSeason(uuid);
        double curLevel = getLevel(uuid);
        int curPoints = getPoints(uuid);
        setCheckpointData(uuid, curSeason, level, curPoints);
    }

    public void setPoints(UUID uuid, int points) {
        int curSeason = getSeason(uuid);
        double curLevel = getLevel(uuid);
        setCheckpointData(uuid, curSeason, curLevel, points);
    }
    public long getLevelTime(UUID uuid) {
        String sql = "SELECT level_time FROM checkpoints WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("level_time");
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to get level time: " + e.getMessage());
        }
        return 0L;
    }
    public void setLevelTime(UUID uuid, long time) {
        String sql = "UPDATE checkpoints SET level_time = ? WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, time);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to save level time: " + e.getMessage());
        }
    }


    public void linkDiscord(UUID uuid, String discordId) {
        String sql = """
            INSERT INTO checkpoints (uuid, season, level, points, discord)
            VALUES (?, 0, 0, 0, ?)
            ON CONFLICT(uuid) DO UPDATE SET discord = excluded.discord
        """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, discordId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to link Discord: " + e.getMessage());
        }
    }

    public void unlinkDiscord(UUID uuid) {
        String sql = "UPDATE checkpoints SET discord = NULL WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to unlink Discord: " + e.getMessage());
        }
    }

    public String getDiscordId(UUID uuid) {
        String sql = "SELECT discord FROM checkpoints WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("discord");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to get Discord ID: " + e.getMessage());
        }
        return null;
    }

    public boolean isLinked(UUID uuid) {
        String id = getDiscordId(uuid);
        return id != null && !id.isEmpty();
    }

    public void deleteAll() {
        String sql = "DELETE FROM checkpoints";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to delete all checkpoints: " + e.getMessage());
        }
    }

    public void deleteSpecific(UUID uuid) {
        String sql = "DELETE FROM checkpoints WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to delete checkpoint for " + uuid + ": " + e.getMessage());
        }
    }

    public List<CheckpointData> getAllSortedBySeasonLevel() {
        List<CheckpointData> list = new ArrayList<>();
        String sql = "SELECT uuid, season, level, points, level_time, discord FROM checkpoints ORDER BY season DESC, level DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new CheckpointData(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getInt("season"),
                        rs.getDouble("level"),
                        rs.getInt("points"),
                        rs.getLong("level_time"),
                        rs.getString("discord")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to fetch sorted checkpoint data: " + e.getMessage());
        }
        return list;
    }

    public List<CheckpointData> getAllSortedByPoints() {
        List<CheckpointData> list = new ArrayList<>();
        String sql = "SELECT uuid, season, level, points, level_time, discord FROM checkpoints ORDER BY points DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new CheckpointData(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getInt("season"),
                        rs.getDouble("level"),
                        rs.getInt("points"),
                        rs.getLong("level_time"),
                        rs.getString("discord")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to fetch sorted points data: " + e.getMessage());
        }
        return list;
    }
    public String getUUIDFromDiscord(String discordId) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT uuid FROM checkpoints WHERE discord = ?")) {

            stmt.setString(1, discordId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("uuid");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<UUID> getPlayersAtCheckpoint(int season, double level) {
        List<UUID> players = new ArrayList<>();

        String sql = """
        SELECT uuid
        FROM checkpoints
        WHERE season = ? AND level = ?
    """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, season);
            ps.setDouble(2, level);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    players.add(UUID.fromString(rs.getString("uuid")));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to fetch players at checkpoint: " + e.getMessage());
        }

        return players;
    }


    public record CheckpointData(UUID uuid, int season, double level, int points, long level_time, String discord) {}



    //Checkpoint Locations
    private void createCheckpointLocationsTable() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS checkpoint_locations (
                season INTEGER NOT NULL,
                level REAL NOT NULL,
                world TEXT NOT NULL,
                x REAL NOT NULL,
                y REAL NOT NULL,
                z REAL NOT NULL,
                yaw INTEGER NOT NULL,
                difficulty TEXT,
                PRIMARY KEY (season, level)
            )
        """);
        }
    }
    public void updateLocationsSchema() {
        java.util.Map<String, String> expectedColumns = new java.util.HashMap<>();
        expectedColumns.put("season", "INTEGER NOT NULL");
        expectedColumns.put("level", "REAL NOT NULL");
        expectedColumns.put("world", "TEXT NOT NULL");
        expectedColumns.put("x", "REAL NOT NULL");
        expectedColumns.put("y", "REAL NOT NULL");
        expectedColumns.put("z", "REAL NOT NULL");
        expectedColumns.put("yaw", "INTEGER NOT NULL");
        expectedColumns.put("difficulty", "TEXT");

        try (Connection conn = getConnection()) {
            Set<String> existingColumns = new HashSet<>();
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("PRAGMA table_info(checkpoint_locations)")) {
                while (rs.next()) {
                    existingColumns.add(rs.getString("name"));
                }
            }
            for (java.util.Map.Entry<String, String> entry : expectedColumns.entrySet()) {
                String columnName = entry.getKey();
                String definition = entry.getValue();

                if (!existingColumns.contains(columnName)) {
                    Bukkit.getLogger().info("Database Migration: Adding missing column '" + columnName + "' to checkpoint locations table.");
                    try (Statement alterStmt = conn.createStatement()) {
                        alterStmt.executeUpdate("ALTER TABLE checkpoint_locations ADD COLUMN " + columnName + " " + definition + ";");
                    }
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to update player logs table schema:");
            e.printStackTrace();
        }
    }

    public void storeCheckpointLocationIfAbsent(int season, double level, Location loc, String difficulty) {
        String sql = """
        INSERT OR IGNORE INTO checkpoint_locations
        (season, level, world, x, y, z, yaw, difficulty)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """;

        double centerX = loc.getBlockX() + 0.5;
        double centerY = loc.getBlockY();
        double centerZ = loc.getBlockZ() + 0.5;
        int yaw = normalizeYawToCardinal(loc.getYaw());

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, season);
            ps.setDouble(2, level);
            ps.setString(3, loc.getWorld().getName());
            ps.setDouble(4, centerX);
            ps.setDouble(5, centerY);
            ps.setDouble(6, centerZ);
            ps.setInt(7, yaw);
            ps.setString(8, difficulty);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to store checkpoint location: " + e.getMessage());
        }
    }
    private int normalizeYawToCardinal(float yaw) {
        yaw = (yaw % 360 + 360) % 360;

        if (yaw >= 315 || yaw < 45) return 0;
        if (yaw < 135) return 90;
        if (yaw < 225) return 180;
        return 270;
    }

    public Location getCheckpointLocation(int season, double level) {
        String sql = """
        SELECT world, x, y, z, yaw
        FROM checkpoint_locations
        WHERE season = ? AND level = ?
    """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, season);
            ps.setDouble(2, level);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    World world = Bukkit.getWorld(rs.getString("world"));
                    if (world == null) return null;

                    Location loc = new Location(
                            world,
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z")
                    );
                    loc.setYaw(rs.getInt("yaw"));
                    return loc;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to get checkpoint location: " + e.getMessage());
        }
        return null;
    }
    public String getCheckpointDifficulty(int season, double level) {
        String sql = """
        SELECT difficulty
        FROM checkpoint_locations
        WHERE season = ? AND level = ?
    """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, season);
            ps.setDouble(2, level);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("difficulty");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to get checkpoint difficulty: " + e.getMessage());
        }
        return null;
    }
    public boolean checkpointLocationExists(int season, double level) {
        String sql = "SELECT 1 FROM checkpoint_locations WHERE season = ? AND level = ? LIMIT 1";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, season);
            ps.setDouble(2, level);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }
    public boolean removeCheckpointLocation(int season, double level) {
        String sql = "DELETE FROM checkpoint_locations WHERE season = ? AND level = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, season);
            ps.setDouble(2, level);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to remove checkpoint location: " + e.getMessage());
            return false;
        }
    }
}
