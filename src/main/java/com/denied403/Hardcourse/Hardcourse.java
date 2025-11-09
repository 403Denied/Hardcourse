package com.denied403.Hardcourse;

import com.denied403.Hardcourse.Commands.*;
import com.denied403.Hardcourse.Commands.Trails.EndTrail;
import com.denied403.Hardcourse.Commands.Trails.OminousTrail;
import com.denied403.Hardcourse.Discord.*;
import com.denied403.Hardcourse.Discord.Commands.Info;
import com.denied403.Hardcourse.Events.*;
import com.denied403.Hardcourse.Chat.*;
import com.denied403.Hardcourse.Points.*;
import com.denied403.Hardcourse.Utils.*;
import com.denied403.Hardcourse.Utils.ColorUtil;

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

public final class Hardcourse extends JavaPlugin implements Listener {

    private File wordsFile;
    private FileConfiguration wordsConfig;
    private PointsManager pointsManager;
    public static Hardcourse plugin;
    public static boolean DiabolicalUnscrambles;

    @Override
    public void onEnable() {
        plugin = this;
        CheckpointDatabase checkpointDatabase = new CheckpointDatabase(this);
        loadConfigValues(this);

        if(isDev) {
            if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new Placeholders().register();
            }
            PointsManager.initialize(checkpointDatabase);
            Points.initialize(checkpointDatabase);
        }
        onWalk.initalize(checkpointDatabase);
        CheckpointCommand.initialize(checkpointDatabase);
        HardcourseDiscord.initialize(checkpointDatabase);
        onChat.initialize(checkpointDatabase);
        onJoin.initialize(checkpointDatabase);
        onQuit.initialize(checkpointDatabase);
        Info.initialize(checkpointDatabase);
        onSneak.initialize(checkpointDatabase);
        Placeholders.initialize(checkpointDatabase);

        CosmeticsShop cosmeticsShop = new CosmeticsShop();
        PointsShop pointsShop = new PointsShop(this, cosmeticsShop);

        if(DiscordEnabled) {
            HardcourseDiscord discordBot = new HardcourseDiscord(this);
            try {
                discordBot.InitJDA();
            } catch (Exception e) {
                getLogger().severe("Failed to initialize Discord bot: " + e.getMessage());
            }
            sendMessage(null, null, "starting", null, null);
        }

        saveDefaultConfig();

        pointsManager = new PointsManager();

        getServer().getPluginManager().registerEvents(new ChatReactions(this), this);
        getServer().getPluginManager().registerEvents(new onJoin(), this);
        getServer().getPluginManager().registerEvents(new onDrop(), this);
        getServer().getPluginManager().registerEvents(new onClick(), this);
        getServer().getPluginManager().registerEvents(new onWalk(), this);
        getServer().getPluginManager().registerEvents(new onChat(), this);
        getServer().getPluginManager().registerEvents(new onHunger(), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new BanListener(this), this);
        getServer().getPluginManager().registerEvents(new onQuit(), this);
        getServer().getPluginManager().registerEvents(new onSneak(), this);
        if(isDev) {
            getServer().getPluginManager().registerEvents(new OminousTrail(this), this);
            getServer().getPluginManager().registerEvents(new onPortalEnter(), this);
            getServer().getPluginManager().registerEvents(new EndTrail(this), this);
            getServer().getPluginManager().registerEvents(pointsShop, this);
            getServer().getPluginManager().registerEvents(new JumpBoost(), this);
            getServer().getPluginManager().registerEvents(new DoubleJump(), this);
            getServer().getPluginManager().registerEvents(new TempCheckpoint(), this);
            getServer().getPluginManager().registerEvents(new JumpBoost(), this);
        }
        getServer().getPluginManager().registerEvents(new GamemodeChange(), this);
        getServer().getPluginManager().registerEvents(new ReportListener(), this);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, cmd -> {
            cmd.registrar().register(Clock.createCommand("clock"));
            cmd.registrar().register(CheckpointCommand.createCommand(this, "checkpoint"));
            cmd.registrar().register(CheckpointCommand.createCommand(this, "checkpoints"));
            cmd.registrar().register(EndChatGame.createCommand("endchatgame"));
            cmd.registrar().register(EndChatGame.createCommand("ecg"));
            cmd.registrar().register(RunChatGame.createCommand("runchatgame"));
            cmd.registrar().register(RunChatGame.createCommand("rcg"));
            cmd.registrar().register(RestartForUpdate.createCommand(this, "restartforupdate"));
            cmd.registrar().register(ReloadHardcourse.createCommand(this, "reloadhardcourse"));
            cmd.registrar().register(ReloadHardcourse.createCommand(this, "hardcoursereload"));
            cmd.registrar().register(WinnerTP.createCommand("winnertp"));
            cmd.registrar().register(WinnerTP.createCommand("wtp"));
            cmd.registrar().register(ToggleDiabolicalUnscrambles.createCommand("togglediabolicalunscrambles"));
            if(isDev) {
                cmd.registrar().register(Clock.createCommand("shop"));
                cmd.registrar().register(Points.createCommand(this, pointsManager, "points"));
                cmd.registrar().register(Points.createCommand(this, pointsManager, "pts"));
                cmd.registrar().register(EndTrail.createCommand("endtrail"));
                cmd.registrar().register(OminousTrail.createCommand("ominoustrail"));
            }
        });

        setupWordsConfig();

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if(isBroadcastEnabled()){
                if(Bukkit.getOnlinePlayers().isEmpty()) return;
                String message = messages.get(random.nextInt(messages.size()));
                Bukkit.broadcast(ColorUtil.Colorize("\n&c&lHARDCOURSE &r" + message + "\n"));
            }}, 0L, 20 * 60 * 5);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(isUnscrambleEnabled()) {
                    if(Bukkit.getOnlinePlayers().isEmpty()) return;
                    ChatReactions.runGame(ChatReactions.getRandomWord());
                }
            }}.runTaskTimer(this, 0L, 20 * 60 * 4);
    }

    public PointsManager getPointsManager() {
        return this.pointsManager;
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

    public static boolean isDiscordEnabled(){return DiscordEnabled;}
    public static boolean isBroadcastEnabled(){return BroadcastEnabled;}
    public static boolean isUnscrambleEnabled(){return UnscrambleEnabled;}
    public static boolean isDev() {return isDev;}
    public void reloadWordsConfig() {wordsConfig = YamlConfiguration.loadConfiguration(wordsFile);}
    public List<String> getApplicationQuestions(){return getConfig().getStringList("application-questions");}

    private static List<String> messages = new ArrayList<>();
    private static boolean DiscordEnabled;
    private static boolean BroadcastEnabled;
    private static boolean UnscrambleEnabled;
    private static boolean isDev;
    private static List<String> exemptions;
    private final Random random = new Random();

    public static void loadConfigValues(Hardcourse plugin) {
        FileConfiguration config = plugin.getConfig();
        DiscordEnabled = config.getBoolean("discord-enabled");
        BroadcastEnabled = config.getBoolean("broadcast-enabled");
        UnscrambleEnabled = config.getBoolean("unscramble-enabled");
        isDev = config.getBoolean("is-dev");
        messages = config.getStringList("broadcast-messages");
        exemptions = config.getStringList("skip-alert-exemptions");
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