package com.denied403.Hardcourse;

import com.denied403.Hardcourse.Commands.*;
import com.denied403.Hardcourse.Commands.Trails.EndTrail;
import com.denied403.Hardcourse.Commands.Trails.OminousTrail;
import com.denied403.Hardcourse.Discord.*;
import com.denied403.Hardcourse.Events.*;
import com.denied403.Hardcourse.Chat.*;
import com.denied403.Hardcourse.Points.*;
import com.denied403.Hardcourse.Points.Shop.PointsShop;
import com.denied403.Hardcourse.Utils.*;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.*;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public final class Hardcourse extends JavaPlugin implements Listener {

    private static File wordsFile;
    private static FileConfiguration wordsConfig;
    public static Hardcourse plugin;
    public static boolean DiabolicalUnscrambles;
    public static CheckpointDatabase checkpointDatabase;
    public static LinkManager linkManager;
    public static PointsManager pointsManager;

    @Override
    public void onEnable() {
        plugin = this;
        checkpointDatabase = new CheckpointDatabase();
        linkManager = new LinkManager();
        pointsManager = new PointsManager();
        saveDefaultConfig();
        setupWordsConfig();
        loadConfigValues();

        if(isDev) {
            if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new Placeholders().register();
            }
        }

        if(DiscordEnabled) {
            try {
                InitJDA();
            } catch (Exception e) {
                getLogger().severe("Failed to initialize Discord bot: " + e.getMessage());
            }
            sendMessage(null, null, "starting", null, null);
        }

        getServer().getPluginManager().registerEvents(new ChatReactions(), this);
        getServer().getPluginManager().registerEvents(new onJoin(), this);
        getServer().getPluginManager().registerEvents(new onDrop(), this);
        getServer().getPluginManager().registerEvents(new onClick(), this);
        getServer().getPluginManager().registerEvents(new onWalk(), this);
        getServer().getPluginManager().registerEvents(new onChat(), this);
        getServer().getPluginManager().registerEvents(new onHunger(), this);
        getServer().getPluginManager().registerEvents(new PunishmentListener(), this);
        getServer().getPluginManager().registerEvents(new onQuit(), this);
        getServer().getPluginManager().registerEvents(new onSneak(), this);
        getServer().getPluginManager().registerEvents(new onVanish(), this);
        getServer().getPluginManager().registerEvents(new onDeath(), this);
        if(isDev) {
            getServer().getPluginManager().registerEvents(new PointsShop(), this);
            getServer().getPluginManager().registerEvents(new OminousTrail(this), this);
            getServer().getPluginManager().registerEvents(new onPortalEnter(), this);
            getServer().getPluginManager().registerEvents(new EndTrail(this), this);
            getServer().getPluginManager().registerEvents(new JumpBoost(), this);
            getServer().getPluginManager().registerEvents(new DoubleJump(), this);
            getServer().getPluginManager().registerEvents(new TempCheckpoint(), this);
        }
        getServer().getPluginManager().registerEvents(new onCommand(), this);
        getServer().getPluginManager().registerEvents(new ReportListener(), this);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, cmd -> {
            cmd.registrar().register(Clock.createCommand("clock"));
            cmd.registrar().register(CheckpointCommand.createCommand("checkpoint"));
            cmd.registrar().register(CheckpointCommand.createCommand("checkpoints"));
            cmd.registrar().register(EndChatGame.createCommand("endchatgame"));
            cmd.registrar().register(EndChatGame.createCommand("ecg"));
            cmd.registrar().register(RunChatGame.createCommand("runchatgame"));
            cmd.registrar().register(RunChatGame.createCommand("rcg"));
            cmd.registrar().register(RestartForUpdate.createCommand("restartforupdate"));
            cmd.registrar().register(RestartForUpdate.createCommand("restartforupdates"));
            cmd.registrar().register(ReloadHardcourse.createCommand("reloadhardcourse"));
            cmd.registrar().register(ReloadHardcourse.createCommand("hardcoursereload"));
            cmd.registrar().register(WinnerTP.createCommand("winnertp"));
            cmd.registrar().register(WinnerTP.createCommand("wtp"));
            cmd.registrar().register(ToggleDiabolicalUnscrambles.createCommand("togglediabolicalunscrambles"));
            cmd.registrar().register(Deaths.createCommand("deaths"));
            if(DiscordEnabled) {
                cmd.registrar().register(Link.createCommand("link"));
                cmd.registrar().register(Unlink.createCommand("unlink"));
            }
            if(isDev) {
                cmd.registrar().register(Clock.createCommand("shop"));
                cmd.registrar().register(Points.createCommand(pointsManager, "points"));
                cmd.registrar().register(Points.createCommand(pointsManager, "pts"));
                cmd.registrar().register(EndTrail.createCommand("endtrail"));
                cmd.registrar().register(OminousTrail.createCommand("ominoustrail"));
            }
        });

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if(BroadcastEnabled){
                if(Bukkit.getOnlinePlayers().isEmpty()) return;
                String message = messages.get(random.nextInt(messages.size()));
                Bukkit.broadcast(Colorize("\n<prefix>" + message + "\n"));
            }}, 0L, 20 * 60 * 5);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(UnscrambleEnabled) {
                    if(!Bukkit.getOnlinePlayers().isEmpty()) {
                        ChatReactions.runGame(ChatReactions.getRandomWord());
                    }
                }
            }}.runTaskTimer(this, 0L, 20 * 60 * 4);
    }

    @Override
    public void onDisable() {
        if(DiscordEnabled) {
            sendMessage(null, null, "stopping", null, null);
            if(jda != null){
                jda.shutdown();
            }
        }
    }

    public void setupWordsConfig() {
        wordsFile = new File(getDataFolder(), "words.yml");
        if (!wordsFile.exists()) {
            saveResource("words.yml", false);
        }
        wordsConfig = YamlConfiguration.loadConfiguration(wordsFile);
    }

    public static List<String> messages = new ArrayList<>();
    public static boolean DiscordEnabled;
    public static boolean BroadcastEnabled;
    public static boolean UnscrambleEnabled;
    public static boolean isDev;
    public static List<String> exemptions;
    public static List<String> applicationQuestions;
    private final Random random = new Random();

    public static void loadConfigValues() {
        FileConfiguration config = plugin.getConfig();
        DiscordEnabled = config.getBoolean("discord-enabled");
        BroadcastEnabled = config.getBoolean("broadcast-enabled");
        UnscrambleEnabled = config.getBoolean("unscramble-enabled");
        isDev = config.getBoolean("is-dev");
        messages = config.getStringList("broadcast-messages");
        exemptions = config.getStringList("skip-alert-exemptions");
        applicationQuestions = config.getStringList("application-questions");
        wordsConfig = YamlConfiguration.loadConfiguration(wordsFile);
    }
    public boolean isSkipExempted(int from, int to) {
        for (String entry : exemptions) {
            String[] parts = entry.split("-");
            if (parts.length != 2) continue;

            String rawFrom = parts[0].trim();
            String rawTo = parts[1].trim();

            boolean fromMatches = rawFrom.equals("*") || Integer.toString(from).equals(rawFrom);
            boolean toMatches = rawTo.equals("*") || Integer.toString(to).equals(rawTo);

            if (fromMatches && toMatches) {
                return true;
            }
        }
        return false;
    }
}