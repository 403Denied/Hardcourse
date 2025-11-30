package com.denied403.Hardcourse.Utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckpointDatabase {

    private final Plugin plugin;

    public CheckpointDatabase(Plugin plugin) {
        this.plugin = plugin;
        try {
            createCheckpointTableAndMigrateIfNeeded();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[HARDCOURSE] Failed to initialize checkpoint DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* ----------------------------
       CONNECTION
       ---------------------------- */
    public Connection getConnection() throws SQLException {
        File dbFile = new File(plugin.getDataFolder(), "data.db");
        dbFile.getParentFile().mkdirs();
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile);
    }

    /* ----------------------------
       SCHEMA CREATION + MIGRATION
       ---------------------------- */
    private void createCheckpointTableAndMigrateIfNeeded() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // If table does not exist, create it with correct schema (including defaults)
            if (!tableExists(conn, "checkpoints")) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS checkpoints (
                        uuid TEXT PRIMARY KEY NOT NULL,
                        season INTEGER NOT NULL DEFAULT 0,
                        level REAL NOT NULL DEFAULT 0,
                        points INTEGER NOT NULL DEFAULT 0,
                        discord TEXT
                    )
                """);
                Bukkit.getLogger().info("[HARDCOURSE] Created checkpoints table.");
                return;
            }

            // If table exists, check columns
            boolean hasDiscord = false;
            boolean seasonHasDefault = false;
            boolean levelHasDefault = false;
            boolean pointsHasDefault = false;

            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(checkpoints)")) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String dflt_value = rs.getString("dflt_value"); // may be null
                    if ("discord".equalsIgnoreCase(name)) hasDiscord = true;
                    if ("season".equalsIgnoreCase(name) && dflt_value != null) seasonHasDefault = true;
                    if ("level".equalsIgnoreCase(name) && dflt_value != null) levelHasDefault = true;
                    if ("points".equalsIgnoreCase(name) && dflt_value != null) pointsHasDefault = true;
                }
            }

            // Add discord column if missing
            if (!hasDiscord) {
                Bukkit.getLogger().info("[HARDCOURSE] Adding missing 'discord' column to checkpoints table...");
                stmt.executeUpdate("ALTER TABLE checkpoints ADD COLUMN discord TEXT");
                Bukkit.getLogger().info("[HARDCOURSE] 'discord' column added.");
            }

            // If any of season/level/points don't have defaults, rebuild table with defaults to avoid NOT NULL insert failures
            if (!seasonHasDefault || !levelHasDefault || !pointsHasDefault) {
                Bukkit.getLogger().info("[HARDCOURSE] Ensuring NOT NULL DEFAULTs for season/level/points...");
                rebuildTableWithDefaults(conn);
                Bukkit.getLogger().info("[HARDCOURSE] Migration to add defaults complete.");
            }
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT name FROM sqlite_master WHERE type='table' AND name = ?")) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void rebuildTableWithDefaults(Connection conn) throws SQLException {
        // This rebuild preserves existing data and sets defaults for missing values.
        // We do it in a single transaction to be safe.
        conn.setAutoCommit(false);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE checkpoints RENAME TO checkpoints_old");

            stmt.executeUpdate("""
                    CREATE TABLE checkpoints (
                        uuid TEXT PRIMARY KEY NOT NULL,
                        season INTEGER NOT NULL DEFAULT 0,
                        level REAL NOT NULL DEFAULT 0,
                        points INTEGER NOT NULL DEFAULT 0,
                        discord TEXT
                    )
                """);

            // Copy data, using COALESCE to provide defaults if old values were NULL or missing
            stmt.executeUpdate("""
                INSERT INTO checkpoints (uuid, season, level, points, discord)
                SELECT
                    uuid,
                    COALESCE(season, 0),
                    COALESCE(level, 0),
                    COALESCE(points, 0),
                    discord
                FROM checkpoints_old
            """);

            stmt.executeUpdate("DROP TABLE checkpoints_old");
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /* ----------------------------
       CORE CHECKPOINT DATA (does NOT touch discord)
       ---------------------------- */
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
        String sql = "SELECT season, level, points, discord FROM checkpoints WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int season = rs.getInt("season");
                    double level = rs.getDouble("level");
                    int points = rs.getInt("points");
                    String discord = rs.getString("discord"); // returns null if column is NULL
                    return new CheckpointData(uuid, season, level, points, discord);
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
        String sql = "SELECT uuid, season, level, points, discord FROM checkpoints ORDER BY season DESC, level DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new CheckpointData(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getInt("season"),
                        rs.getDouble("level"),
                        rs.getInt("points"),
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
        String sql = "SELECT uuid, season, level, points, discord FROM checkpoints ORDER BY points DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new CheckpointData(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getInt("season"),
                        rs.getDouble("level"),
                        rs.getInt("points"),
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
    public record CheckpointData(UUID uuid, int season, double level, int points, String discord) {}
}
