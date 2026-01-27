package com.denied403.Hardcourse.Utils;

import net.kyori.adventure.text.Component;

import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.denied403.Hardcourse.Commands.Clock.giveItems;
import static com.denied403.Hardcourse.Hardcourse.checkpointDatabase;
import static com.denied403.Hardcourse.Hardcourse.plugin;
import static com.denied403.Hardcourse.Points.PointsManager.getPoints;
import static com.denied403.Hardcourse.Utils.CheckpointLevelTimer.resetForNewLevel;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class CheckpointUpdating {

    public CheckpointUpdating() {
        try {
            createTables();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[HARDCOURSE] Failed to initialize update tables");
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return checkpointDatabase.getConnection();
    }

    /* ================================
       ========== TABLE SETUP ==========
       ================================ */

    private void createTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS level_updates (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    season INTEGER NOT NULL,
                    start_level REAL NOT NULL,
                    end_level REAL NOT NULL,
                    reset_target_level REAL NOT NULL,
                    created_at INTEGER NOT NULL
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_update_state (
                    uuid TEXT PRIMARY KEY NOT NULL,
                    last_processed_update_id INTEGER NOT NULL DEFAULT 0
                )
            """);
        }
    }

    /* ================================
       ======= UPDATE MANAGEMENT =======
       ================================ */

    public int addLevelUpdate(int season, double start, double end, double resetTo) {
        String sql = """
            INSERT INTO level_updates
            (season, start_level, end_level, reset_target_level, created_at)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, season);
            ps.setDouble(2, start);
            ps.setDouble(3, end);
            ps.setDouble(4, resetTo);
            ps.setLong(5, System.currentTimeMillis());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to add level update: " + e.getMessage());
        }
        return -1;
    }

    /* ================================
       ===== PLAYER UPDATE STATE =======
       ================================ */

    private int getLastProcessedUpdate(UUID uuid) {
        String sql = """
            SELECT last_processed_update_id
            FROM player_update_state
            WHERE uuid = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException ignored) {}

        return 0;
    }

    private void setLastProcessedUpdate(UUID uuid, int updateId) {
        String sql = """
            INSERT INTO player_update_state (uuid, last_processed_update_id)
            VALUES (?, ?)
            ON CONFLICT(uuid) DO UPDATE SET
                last_processed_update_id = excluded.last_processed_update_id
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid.toString());
            ps.setInt(2, updateId);
            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to save update state for " + uuid);
        }
    }

    /* ================================
       ===== UPDATE APPLICATION ========
       ================================ */

    public void applyPendingUpdates(Player player) {
        UUID uuid = player.getUniqueId();
        int season = checkpointDatabase.getSeason(uuid);
        double level = checkpointDatabase.getLevel(uuid);
        int lastProcessed = getLastProcessedUpdate(uuid);

        String sql = """
        SELECT id, start_level, end_level, reset_target_level
        FROM level_updates
        WHERE season = ?
          AND id > ?
        ORDER BY id
    """;

        // Collect updates first
        List<LevelUpdate> updatesToApply = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, season);
            ps.setInt(2, lastProcessed);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    updatesToApply.add(new LevelUpdate(
                            rs.getInt("id"),
                            rs.getDouble("start_level"),
                            rs.getDouble("end_level"),
                            rs.getDouble("reset_target_level")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[HARDCOURSE] Failed to fetch updates for " + uuid);
            e.printStackTrace();
            return;
        }

        for (LevelUpdate update : updatesToApply) {
            Location targetLoc = checkpointDatabase.getCheckpointLocation(season, update.resetTo);
            if (targetLoc == null) {
                plugin.getLogger().warning("[HARDCOURSE] Missing checkpoint location for season " + season + " level " + update.resetTo + " (update " + update.id + ")");
                lastProcessed = update.id;
                continue;
            }
            if (!player.hasPlayedBefore()) {
                setLastProcessedUpdate(uuid, update.id);
                return;
            }

            if (level >= update.start && level <= update.end) {
                player.teleport(targetLoc);
                player.setRespawnLocation(targetLoc);
                checkpointDatabase.setCheckpointData(uuid, 1, update.resetTo, getPoints(uuid));
                level = update.resetTo;
                giveItems(player);
                resetForNewLevel(uuid);
                player.sendMessage(Colorize("<prefix>Your checkpoint has been updated to level <accent>" + String.valueOf(update.resetTo).replace(".0", "") + "<main> for season <accent>" + season + "<main> due to a recent update. Contact a staff member for more information."));
            } else {
                Component message = Colorize("<prefix>A level update has been applied for Season <accent>" + season + " <main>Levels <accent>" + String.valueOf(update.start).replaceAll(".0", "") + "<main>-<accent>" + String.valueOf(update.end).replaceAll(".0", "") + "<main>.<accent> Click here to reset back to the updated section<main>! You could also run <accent>/checkpoint tp " + update.resetTo + "<main> to go see the updated portion without resetting yourself!")
                        .hoverEvent(HoverEvent.showText(Colorize("Doing this will set your level back to <accent>" + String.valueOf(update.resetTo).replace(".0", ""))))
                        .clickEvent(ClickEvent.callback(audience -> {
                            if (!(audience instanceof Player p)) return;

                            p.teleport(targetLoc);
                            checkpointDatabase.setCheckpointData(p.getUniqueId(), 1, update.resetTo, getPoints(p.getUniqueId()));
                            giveItems(p);
                            resetForNewLevel(p.getUniqueId());

                            p.sendMessage(Colorize("<prefix>You have been moved to level <accent>" + String.valueOf(update.resetTo).replace(".0", "")));
                        }));

                player.sendMessage(message);
            }

            lastProcessed = update.id;
        }

        setLastProcessedUpdate(uuid, lastProcessed);
    }

    private record LevelUpdate(int id, double start, double end, double resetTo) {}


    /* ================================
       ======= UTILITY METHODS =========
       ================================ */

    public void applyUpdatesToAllOnlinePlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            applyPendingUpdates(p);
        }
    }

    public boolean hasPendingUpdates(UUID uuid, int season) {
        int lastProcessed = getLastProcessedUpdate(uuid);

        String sql = """
            SELECT 1
            FROM level_updates
            WHERE season = ?
              AND id > ?
            LIMIT 1
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, season);
            ps.setInt(2, lastProcessed);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            return false;
        }
    }
    public boolean updateIdExists(int id) {
        String sql = "SELECT 1 FROM level_updates WHERE id = ? LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeQuery().next();

        } catch (SQLException e) {
            return false;
        }
    }
}
