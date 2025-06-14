package com.denied403.hardcoursecheckpoints.Utils;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.util.List;

public class WordSyncListener implements Listener {

    public static List<String> blacklistedWords;
    public static List<String> whitelistedWords;

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
    }

    public static void updateFilterWords() {
        File configFile = new File(Bukkit.getPluginManager().getPlugin("Core403").getDataFolder(), "config.yml");
        FileConfiguration core403Config = YamlConfiguration.loadConfiguration(configFile);

        blacklistedWords = core403Config.getStringList("blacklisted-words");
        whitelistedWords = core403Config.getStringList("whitelisted-words");

        Bukkit.getLogger().info("[HardcourseCheckpoints] Synced word filters from Core403 config.");
    }

    // Optional getters if other classes need access
    public static List<String> getBlacklistedWords() {
        return blacklistedWords;
    }

    public static List<String> getWhitelistedWords() {
        return whitelistedWords;
    }
}
