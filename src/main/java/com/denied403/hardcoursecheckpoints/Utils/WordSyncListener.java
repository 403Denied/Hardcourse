package com.denied403.hardcoursecheckpoints.Utils;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WordSyncListener implements Listener {

    public static List<String> blacklistedWords;
    public static List<String> whitelistedWords;
    private static final Map<UUID, Long> muteCache = new HashMap<>();

    private final HardcourseCheckpoints plugin; // Replace with your actual main plugin class

    public WordSyncListener(HardcourseCheckpoints plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase();

        if (msg.startsWith("/blacklistword") || msg.startsWith("/unblacklistword")
                || msg.startsWith("/whitelistword") || msg.startsWith("/unwhitelistword")) {
            Bukkit.getScheduler().runTaskLater(plugin, WordSyncListener::updateFilterWords, 10L);

        }
        if(msg.startsWith("/mute") || msg.startsWith("/tempmute") || msg.startsWith("/unmute")){
            Bukkit.getScheduler().runTaskLater(plugin, WordSyncListener::reloadMuteCache, 10L);
        }
    }
    @EventHandler
    public void onConsoleCommand(ServerCommandEvent event) {
        String msg = event.getCommand().toLowerCase();
        if(msg.startsWith("mute") || msg.startsWith("tempmute") || msg.startsWith("unmute")){
            Bukkit.getScheduler().runTaskLater(plugin, WordSyncListener::reloadMuteCache, 10L);
        }
    }

    public static void updateFilterWords() {
        File configFile = new File(Bukkit.getPluginManager().getPlugin("Core403").getDataFolder(), "config.yml");
        FileConfiguration core403Config = YamlConfiguration.loadConfiguration(configFile);

        blacklistedWords = core403Config.getStringList("blacklisted-words");
        whitelistedWords = core403Config.getStringList("whitelisted-words");

        Bukkit.getLogger().info("Synced word filters from Core403 config.");
    }

    // Optional getters if other classes need access
    public static List<String> getBlacklistedWords() {
        return blacklistedWords;
    }

    public static List<String> getWhitelistedWords() {
        return whitelistedWords;
    }
    public static void reloadMuteCache() {
        muteCache.clear();

        File muteFile = new File(Bukkit.getPluginManager().getPlugin("Core403").getDataFolder(), "mutes.yml");
        if (!muteFile.exists()) return;

        FileConfiguration mutesConfig = YamlConfiguration.loadConfiguration(muteFile);
        ConfigurationSection section = mutesConfig.getConfigurationSection("Mutes");

        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                if (mutesConfig.contains("Mutes." + key + ".time")) {
                    long time = mutesConfig.getLong("Mutes." + key + ".time");
                    muteCache.put(uuid, time);
                }
            } catch (IllegalArgumentException ignored) {
                // Invalid UUID key in file, ignore it
            }
        }

        Bukkit.getLogger().info("Mute cache loaded with " + muteCache.size() + " entries.");
    }

    /**
     * Returns whether the given player UUID is currently muted.
     */
    public static boolean isPlayerMuted(UUID uuid) {
        Long muteEnd = muteCache.get(uuid);
        return muteEnd != null && System.currentTimeMillis() < muteEnd;
    }
}
