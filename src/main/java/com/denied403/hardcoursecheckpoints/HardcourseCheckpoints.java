package com.denied403.hardcoursecheckpoints;

import com.denied403.hardcoursecheckpoints.Commands.*;
import com.denied403.hardcoursecheckpoints.Commands.Trails.EndTrail;
import com.denied403.hardcoursecheckpoints.Commands.Trails.OminousTrail;
import com.denied403.hardcoursecheckpoints.Discord.*;
import com.denied403.hardcoursecheckpoints.Events.*;
import com.denied403.hardcoursecheckpoints.Chat.*;
import com.denied403.hardcoursecheckpoints.Points.*;
import com.denied403.hardcoursecheckpoints.Utils.*;
import com.denied403.hardcoursecheckpoints.Utils.Colorize;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
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
        loadConfigValues(this);
        this.pointsManager = new PointsManager(this);

        CosmeticsShop cosmeticsShop = new CosmeticsShop();
        PointsShop pointsShop = new PointsShop(this, cosmeticsShop);

        if(DiscordEnabled) {
            Bukkit.broadcast(Colorize.Colorize("Meow!!!! Discord is enabled!"));
            HardcourseDiscord discordBot = new HardcourseDiscord(this);
            try {
                discordBot.InitJDA();
            } catch (Exception e) {
                getLogger().severe("Failed to initialize Discord bot: " + e.getMessage());
            }
        } else {
            Bukkit.broadcast(Colorize.Colorize("Meow!!!! Discord is disabled!"));
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
        Bukkit.getPluginManager().registerEvents(new EndTrail(this), this);
        Bukkit.getPluginManager().registerEvents(new OminousTrail(this), this);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, cmd -> {
            cmd.registrar().register(Clock.createCommand("clock"));
            cmd.registrar().register(Clock.createCommand("shop"));
            cmd.registrar().register(CheckpointCommand.createCommand("checkpoint"));
            cmd.registrar().register(EndChatGame.createCommand("endchatgame"));
            cmd.registrar().register(EndChatGame.createCommand("ecg"));
            cmd.registrar().register(RunChatGame.createCommand("runchatgame"));
            cmd.registrar().register(RunChatGame.createCommand("rcg"));
            cmd.registrar().register(RestartForUpdate.createCommand(this, "restartforupdate"));
            cmd.registrar().register(ReloadHardcourse.createCommand(this, "reloadhardcourse"));
            cmd.registrar().register(ReloadHardcourse.createCommand(this, "hardcoursereload"));
            cmd.registrar().register(WinnerTP.createCommand("winnertp"));
            cmd.registrar().register(WinnerTP.createCommand("wtp"));
            cmd.registrar().register(Points.createCommand(pointsManager, "points"));
            cmd.registrar().register(Points.createCommand(pointsManager, "pts"));
            cmd.registrar().register(EndTrail.createCommand("endtrail"));
            cmd.registrar().register(OminousTrail.createCommand("ominoustrail"));
        });

        setupWordsConfig();
        setupCheckpointsConfig();

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if(isBroadcastEnabled()){
                String message = messages.get(random.nextInt(messages.size()));
                Bukkit.broadcast(Colorize.Colorize(" "));
                Bukkit.broadcast(Colorize.Colorize("&c&lHARDCOURSE &r" + message));
                Bukkit.broadcast(Colorize.Colorize(" "));
            }}, 0L, 20 * 60 * 5);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(isUnscrambleEnabled()) {
                    ChatReactions.runGame(ChatReactions.getRandomWord());
                }
            }}.runTaskTimer(this, 0L, 4800L);

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
    public List<String> getApplicationQuestions(){return getConfig().getStringList("application-questions");}

    private static List<String> messages = new ArrayList<>();
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
    public static void loadConfigValues(HardcourseCheckpoints plugin) {
        FileConfiguration config = plugin.getConfig();
        DiscordEnabled = config.getBoolean("discord-enabled");
        BroadcastEnabled = config.getBoolean("broadcast-enabled");
        UnscrambleEnabled = config.getBoolean("unscramble-enabled");
        messages = config.getStringList("broadcast-messages");
    }
}