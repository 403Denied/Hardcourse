package com.denied403.Hardcourse.Utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.denied403.Hardcourse.Hardcourse.checkpointDatabase;

public class CheckpointLevelTimer implements Listener {

    private static final Map<UUID, Long> accumulatedTime = new HashMap<>();
    private static final Map<UUID, Long> sessionStart = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        long saved = checkpointDatabase.getLevelTime(uuid);
        accumulatedTime.put(uuid, saved);
        sessionStart.put(uuid, System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        saveAndClear(e.getPlayer().getUniqueId());
    }

    public static void shutdown() {
        for (UUID uuid : sessionStart.keySet()) {
            saveAndClear(uuid);
        }
    }

    private static void saveAndClear(UUID uuid) {
        Long start = sessionStart.get(uuid);
        if (start == null) return;

        long elapsed = System.currentTimeMillis() - start;
        long total = accumulatedTime.getOrDefault(uuid, 0L) + elapsed;

        checkpointDatabase.setLevelTime(uuid, total);

        sessionStart.remove(uuid);
        accumulatedTime.remove(uuid);
    }

    public static void resetForNewLevel(UUID uuid) {
        accumulatedTime.put(uuid, 0L);
        sessionStart.put(uuid, System.currentTimeMillis());
        checkpointDatabase.setLevelTime(uuid, 0L);
    }

    public static long getCurrentLevelTime(UUID uuid) {
        long stored = accumulatedTime.getOrDefault(uuid, 0L);
        Long start = sessionStart.get(uuid);
        if (start == null) return stored;
        return stored + (System.currentTimeMillis() - start);
    }

    public static String getCurrentLevelTimeFormatted(UUID uuid) {
        long millis = getCurrentLevelTime(uuid);

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;

        days %= 7;
        hours %= 24;
        minutes %= 60;
        seconds %= 60;

        StringBuilder sb = new StringBuilder();

        appendCompact(sb, weeks, "w");
        appendCompact(sb, days, "d");
        appendCompact(sb, hours, "h");
        appendCompact(sb, minutes, "m");
        appendCompact(sb, seconds, "s");

        return sb.isEmpty() ? "0s" : sb.toString().trim();
    }

    private static void appendCompact(StringBuilder sb, long value, String suffix) {
        if (value <= 0) return;
        if (!sb.isEmpty()) sb.append(" ");
        sb.append(value).append(suffix);
    }

}
