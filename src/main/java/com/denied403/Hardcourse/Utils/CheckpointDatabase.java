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
        createCheckpointTable();
    }
    public Connection getConnection() throws SQLException {
        File dbFile = new File(plugin.getDataFolder(), "data.db");
        dbFile.getParentFile().mkdirs();
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile);
    }
    private void createCheckpointTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS checkpoints (
                uuid TEXT PRIMARY KEY NOT NULL,
                season INTEGER NOT NULL,
                level REAL NOT NULL,
                points INTEGER NOT NULL
            )
        """;
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()){
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to create data table:");
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
            plugin.getLogger().severe("Failed to save checkpoint data: " + e.getMessage());
        }
    }

    public CheckpointData getCheckpointData(UUID uuid) {
        String sql = "SELECT season, level, points FROM checkpoints WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int season = rs.getInt("season");
                    int level = rs.getInt("level");
                    int points = rs.getInt("points");
                    return new CheckpointData(uuid, season, level, points);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load checkpoint data: " + e.getMessage());
        }

        return null;
    }
    public Integer getSeason(UUID uuid) {
        Integer season = getSingleIntField(uuid, "season");
        if(!(season == null)) {
            return getSingleIntField(uuid, "season");
        } else {
            return null;
        }
    }

    public Double getLevel(UUID uuid) {
        Double level = getSingleDouble(uuid, "level");
        if(!(level == null)) {
            return level;
        } else {
            return null;
        }
    }

    public Integer getPoints(UUID uuid) {
        Integer points = getSingleIntField(uuid, "points");
        if(!(points == null)) {
            return points;
        } else {
            setPoints(uuid, 0);
            return 0;
        }
    }
    private Integer getSingleIntField(UUID uuid, String fieldName) {
        String sql = "SELECT " + fieldName + " FROM checkpoints WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(fieldName);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get " + fieldName + " for " + uuid + ": " + e.getMessage());
        }
        return null;
    }
    private Double getSingleDouble(UUID uuid, String fieldName) {
        String sql = "SELECT " + fieldName + " FROM checkpoints WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(fieldName);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get " + fieldName + " for " + uuid + ": " + e.getMessage());
        }
        return null;
    }
    public void setSeason(UUID uuid, Integer season) {
        upsertField(uuid, "season", season, null);
    }

    public void setLevel(UUID uuid, Double level) {
        upsertField(uuid, "level", null, level);
    }

    public void setPoints(UUID uuid, Integer points) {
        upsertField(uuid, "points", points, null);
    }
    private void upsertField(UUID uuid, String field, Integer value, Double value2) {
        CheckpointData current = getCheckpointData(uuid);

        int season = (current != null) ? current.season() : 0;
        double level = (current != null) ? current.level() : 0;
        int points = (current != null) ? current.points() : 0;

        switch (field) {
            case "season" -> season = value;
            case "level" -> level = value2;
            case "points" -> points = value;
            default -> throw new IllegalArgumentException("Invalid field: " + field);
        }
        setCheckpointData(uuid, season, level, points);
    }
    public void deleteAll() {
        String sql = "DELETE FROM checkpoints";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
        }
    }
    public void deleteSpecific(UUID uuid) {
        String sql = "DELETE FROM checkpoints WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
        }
    }

    public List<CheckpointData> getAllSortedBySeasonLevel() {
        List<CheckpointData> list = new ArrayList<>();
        String sql = "SELECT uuid, season, level, points FROM checkpoints ORDER BY season DESC, level DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                int season = rs.getInt("season");
                double level = rs.getDouble("level");
                int points = rs.getInt("points");
                list.add(new CheckpointData(uuid, season, level, points));
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to fetch sorted checkpoint data: " + e.getMessage());
        }
        return list;
    }
    public List<CheckpointData> getAllSortedByPoints() {
        List<CheckpointData> list = new ArrayList<>();
        String sql = "SELECT uuid, season, level, points FROM checkpoints ORDER BY points DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                int season = rs.getInt("season");
                double level = rs.getDouble("level");
                int points = rs.getInt("points");
                list.add(new CheckpointData(uuid, season, level, points));
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to fetch sorted points data: " + e.getMessage());
        }

        return list;
    }
    public boolean hasData(UUID uuid) {
        String sql = "SELECT 1 FROM checkpoints WHERE uuid = ? LIMIT 1";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check data existence for " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    public record CheckpointData(UUID uuid, int season, double level, int points) {}
}
