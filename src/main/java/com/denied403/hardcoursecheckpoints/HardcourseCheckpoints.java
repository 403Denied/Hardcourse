package com.denied403.hardcoursecheckpoints;

import com.denied403.hardcoursecheckpoints.Commands.*;
import com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord;
import com.denied403.hardcoursecheckpoints.Events.*;
import com.denied403.hardcoursecheckpoints.Chat.ChatReactions;
import com.denied403.hardcoursecheckpoints.Utils.PermissionChecker;
import com.denied403.hardcoursecheckpoints.Utils.WordSyncListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;


import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.jda;
import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.sendMessage;

public final class HardcourseCheckpoints extends JavaPlugin implements Listener {
    public static HashMap<UUID, Double> highestCheckpoint = new HashMap<>();
    private File checkpointFile;
    private FileConfiguration checkpointConfig;
    private File wordsFile;
    private FileConfiguration wordsConfig;
    public static Double getHighestCheckpoint(UUID player) {
        if(highestCheckpoint.containsKey(player)) {
            return highestCheckpoint.get(player);
        }
        else {
            highestCheckpoint.put(player, 0.0);
            return 1.0;
        }
    }
    public static void setHighestCheckpoint(UUID player, Double checkpoint) {
        highestCheckpoint.put(player, checkpoint);
    }
    public static HardcourseCheckpoints plugin;
    private HardcourseDiscord discordBot;
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        if(DiscordEnabled) {
            discordBot = new HardcourseDiscord(this);
            try {
                discordBot.InitJDA();
            } catch (Exception e) {
                getLogger().severe("Failed to initialize Discord bot: " + e.getMessage());
            }
        }
        checkpointFile = new File(getDataFolder(), "checkpoints.yml");
        if(!checkpointFile.exists()) {
            saveResource("checkpoints.yml", false);
        }
        if(DiscordEnabled) {
            sendMessage(null, null, "starting", null, null);
        }
        checkpointConfig = YamlConfiguration.loadConfiguration(checkpointFile);
        loadCheckpoints();
        saveDefaultConfig();
        messages = getConfig().getStringList("broadcast-messages");
        DiscordEnabled = getConfig().getBoolean("discord-enabled");
        BroadcastEnabled = getConfig().getBoolean("broadcast-enabled");
        UnscrambleEnabled = getConfig().getBoolean("unscramble-enabled");
        WordSyncListener wordSyncListener = new WordSyncListener(this);
        WordSyncListener.reloadMuteCache();
        ChatReactions chatReactions = new ChatReactions(this);
        getServer().getPluginManager().registerEvents(chatReactions, this);
        getServer().getPluginManager().registerEvents(new onJoin(), this);
        getServer().getPluginManager().registerEvents(new onDrop(), this);
        getServer().getPluginManager().registerEvents(new onClick(), this);
        getServer().getPluginManager().registerEvents(new onWalk(this), this);
        getServer().getPluginManager().registerEvents(new onChat(), this);
        getServer().getPluginManager().registerEvents(new onHunger(), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(wordSyncListener, this);
        getServer().getPluginManager().registerEvents(new BanListener(this), this);
        WordSyncListener.updateFilterWords();
        getCommand("resetcheckpoint").setExecutor(new CheckpointCommands(this));
        getCommand("resetallcheckpoints").setExecutor(new CheckpointCommands(this));
        getCommand("discord").setExecutor(new Discord());
        getCommand("apply").setExecutor(new Apply());
        getCommand("clock").setExecutor(new Clock());
        getCommand("runchatgame").setExecutor(new runChatGame());
        getCommand("endchatgame").setExecutor(new endChatGame());
        getCommand("getlevel").setExecutor(new getLevel());
        getCommand("restartforupdate").setExecutor(new restartForUpdate(this));
        getCommand("setlevel").setExecutor(new setLevel());
        getCommand("reloadhardcourseconfig").setExecutor(new reloadHardcourseConfig(this));
        setupWordsConfig();
        setupCheckpointsConfig();
        if(BroadcastEnabled) {
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                String message = messages.get(random.nextInt(messages.size()));
                Bukkit.broadcastMessage(" ");
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &r" + message));
                Bukkit.broadcastMessage(" ");
            }, 0L, 20 * 60 * 5);
        }//Runs every 5 mins
        if(UnscrambleEnabled) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    ChatReactions.runGame(ChatReactions.getRandomWord());
                }
            }.runTaskTimer(this, 0L, 4800L);
        }
        new PermissionChecker(this);

    }


    @Override
    public void onDisable() {
        if(DiscordEnabled) {
            sendMessage(null, null, "stopping", null, null);
            if(jda != null){
                jda.shutdownNow();
            }
        }
        saveCheckpoints();
    }

    public void loadCheckpoints(){
        for (String key : checkpointConfig.getKeys(false)){
            UUID uuid = UUID.fromString(key);
            Double checkpoint = checkpointConfig.getDouble(key);
            highestCheckpoint.put(uuid, checkpoint);
        }
    }
    public void setupCheckpointsConfig() {
        checkpointFile = new File(getDataFolder(), "checkpoints.yml");
        if (!checkpointFile.exists()) {
            saveResource("checkpoints.yml", false);
        }
        checkpointConfig = YamlConfiguration.loadConfiguration(checkpointFile);
    }
    public void saveCheckpoints(){
        try {
            checkpointConfig.getKeys(false).forEach(key -> checkpointConfig.set(key, null));
            for (UUID uuid : highestCheckpoint.keySet()){
                checkpointConfig.set(uuid.toString(), highestCheckpoint.get(uuid));
            }
            checkpointConfig.save(checkpointFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setupWordsConfig() {
        wordsFile = new File(getDataFolder(), "words.yml");
        if (!wordsFile.exists()) {
            saveResource("words.yml", false);
        }
        wordsConfig = YamlConfiguration.loadConfiguration(wordsFile);
    }
    public static boolean isDiscordEnabled(){
        return DiscordEnabled;
    }
    public static boolean isBroadcastEnabled(){
        return BroadcastEnabled;
    }
    public static boolean isUnscrambleEnabled(){
        return UnscrambleEnabled;
    }
    public FileConfiguration getCheckpointsConfig() {
        return checkpointConfig;
    }

    public FileConfiguration getWordsConfig() {
        return wordsConfig;
    }
    public void reloadCheckpointsConfig() {
        checkpointConfig = YamlConfiguration.loadConfiguration(checkpointFile);
    }

    public void reloadWordsConfig() {
        wordsConfig = YamlConfiguration.loadConfiguration(wordsFile);
    }
    public void loadBroadcastMessages() {
        messages = getConfig().getStringList("broadcast-messages");
    }



    private List<String> messages = new ArrayList<>();
    private static boolean DiscordEnabled;
    private static boolean BroadcastEnabled;
    private static boolean UnscrambleEnabled;
    private final Random random = new Random();
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        if(DiscordEnabled) {
            sendMessage(e.getPlayer(), null, "leave", null, null);
        }
    }
}

