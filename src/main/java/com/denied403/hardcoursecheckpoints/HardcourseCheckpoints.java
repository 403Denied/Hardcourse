package com.denied403.hardcoursecheckpoints;

import com.denied403.hardcoursecheckpoints.Commands.*;
import com.denied403.hardcoursecheckpoints.Discord.*;
import com.denied403.hardcoursecheckpoints.Events.*;
import com.denied403.hardcoursecheckpoints.Chat.*;
import com.denied403.hardcoursecheckpoints.Points.*;
import com.denied403.hardcoursecheckpoints.Utils.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.*;
import static com.denied403.hardcoursecheckpoints.Scoreboard.ScoreboardMain.updateScoreboard;

public final class HardcourseCheckpoints extends JavaPlugin implements Listener {
    public static HashMap<UUID, Double> highestCheckpoint = new HashMap<>();

    public static HashMap<UUID, Integer> playerPoints = new HashMap<>();

    private File checkpointFile;
    private FileConfiguration checkpointConfig;

    private File wordsFile;
    private FileConfiguration wordsConfig;

    private File pointsFile;
    private FileConfiguration pointsConfig;

    private PointsManager pointsManager;

    public static Double getHighestCheckpoint(UUID player) {
        if(highestCheckpoint.containsKey(player)) {
            return highestCheckpoint.get(player);
        }
        else {
            highestCheckpoint.put(player, 0.0);
            return 0.0;
        }
    }
    public static void setHighestCheckpoint(UUID player, Double checkpoint) {
        highestCheckpoint.put(player, checkpoint);
    }


    public static HardcourseCheckpoints plugin;

    @Override
    public void onEnable() {

        plugin = this;

        DiscordEnabled = getConfig().getBoolean("discord-enabled");
        BroadcastEnabled = getConfig().getBoolean("broadcast-enabled");
        UnscrambleEnabled = getConfig().getBoolean("unscramble-enabled");
        messages = getConfig().getStringList("broadcast-messages");


        this.pointsManager = new PointsManager(this);

        CosmeticsShop cosmeticsShop = new CosmeticsShop();
        PointsShop pointsShop = new PointsShop(this, cosmeticsShop);

        if(DiscordEnabled) {
            HardcourseDiscord discordBot = new HardcourseDiscord(this);
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
        checkpointConfig = YamlConfiguration.loadConfiguration(checkpointFile);
        loadCheckpoints();

        setupPointsConfig();
        loadPoints();

        saveDefaultConfig();

        pointsManager = new PointsManager(this);

        getServer().getPluginManager().registerEvents(new ChatReactions(this), this);
        getServer().getPluginManager().registerEvents(new onJoin(), this);
        getServer().getPluginManager().registerEvents(new onDrop(), this);
        getServer().getPluginManager().registerEvents(new onClick(), this);
        getServer().getPluginManager().registerEvents(new onWalk(this), this);
        getServer().getPluginManager().registerEvents(new onChat(), this);
        getServer().getPluginManager().registerEvents(new onHunger(), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new BanListener(this), this);
        getServer().getPluginManager().registerEvents(new JumpBoost(), this);
        getServer().getPluginManager().registerEvents(new DoubleJump(), this);
        getServer().getPluginManager().registerEvents(new TempCheckpoint(), this);
        getServer().getPluginManager().registerEvents(pointsShop, this);
        Bukkit.getPluginManager().registerEvents(new JumpBoost(), this);
        Bukkit.getPluginManager().registerEvents(new Portal(), this);
        getCommand("resetcheckpoint").setExecutor(new CheckpointCommands(this));
        getCommand("resetallcheckpoints").setExecutor(new CheckpointCommands(this));
        getCommand("purgeinactive").setExecutor(new CheckpointCommands(this));
        getCommand("clock").setExecutor(new Clock());
        getCommand("runchatgame").setExecutor(new runChatGame());
        getCommand("endchatgame").setExecutor(new endChatGame());
        getCommand("getlevel").setExecutor(new getLevel());
        getCommand("restartforupdate").setExecutor(new restartForUpdate(this));
        getCommand("setlevel").setExecutor(new setLevel());
        getCommand("reloadhardcourseconfig").setExecutor(new reloadHardcourseConfig(this));
        getCommand("points").setExecutor(new PointsCommand(new PointsManager(this)));
        getCommand("points").setTabCompleter(new PointsTabCompleter());
        getCommand("stuck").setExecutor(new Stuck());
        getCommand("winnertp").setExecutor(new WinnerTP());
        getCommand("endtrail").setExecutor(new Endrod(this));
        getCommand("ominoustrail").setExecutor(new Ominous(this));

        setupWordsConfig();
        setupCheckpointsConfig();

        if(isBroadcastEnabled()) {
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                String message = messages.get(random.nextInt(messages.size()));
                Bukkit.broadcastMessage(" ");
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &r" + message));
                Bukkit.broadcastMessage(" ");
            }, 0L, 20 * 60 * 5);
        }

        if(UnscrambleEnabled) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    ChatReactions.runGame(ChatReactions.getRandomWord());
                }
            }.runTaskTimer(this, 0L, 4800L);
        }

        new PermissionChecker(this);


        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(player);
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    public PointsManager getPointsManager() {
        return this.pointsManager;
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

        savePoints();
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

    // --- New: Setup points config ---
    public void setupPointsConfig() {
        pointsFile = new File(getDataFolder(), "points.yml");
        if (!pointsFile.exists()) {
            saveResource("points.yml", false);
        }
        pointsConfig = YamlConfiguration.loadConfiguration(pointsFile);
    }

    public void loadPoints() {
        if(pointsConfig == null) return;
        for (String key : pointsConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            int points = pointsConfig.getInt(key);
            playerPoints.put(uuid, points);
        }
    }

    public void savePoints() {
        try {
            pointsConfig.getKeys(false).forEach(key -> pointsConfig.set(key, null));
            for (UUID uuid : playerPoints.keySet()) {
                pointsConfig.set(uuid.toString(), playerPoints.get(uuid));
            }
            pointsConfig.save(pointsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reloadPointsConfig() {
        pointsConfig = YamlConfiguration.loadConfiguration(pointsFile);
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

    public FileConfiguration getPointsConfig() {
        return pointsConfig;
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
    public List<String> getApplicationQuestions(){return getConfig().getStringList("application-questions");}

    private List<String> messages = new ArrayList<>();
    private static boolean DiscordEnabled;
    private static boolean BroadcastEnabled;
    private static boolean UnscrambleEnabled;
    private final Random random = new Random();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        if(getHighestCheckpoint(e.getPlayer().getUniqueId()) < 2){
            highestCheckpoint.remove(e.getPlayer().getUniqueId());
        }
        if(DiscordEnabled) {
            sendMessage(e.getPlayer(), null, "leave", null, null);
        }
    }
}