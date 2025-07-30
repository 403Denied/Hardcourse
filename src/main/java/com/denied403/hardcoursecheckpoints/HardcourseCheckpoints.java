package com.denied403.hardcoursecheckpoints;

import com.denied403.hardcoursecheckpoints.Commands.*;
import com.denied403.hardcoursecheckpoints.Commands.Trails.EndTrail;
import com.denied403.hardcoursecheckpoints.Commands.Trails.OminousTrail;
import com.denied403.hardcoursecheckpoints.Discord.*;
import com.denied403.hardcoursecheckpoints.Discord.Commands.Info;
import com.denied403.hardcoursecheckpoints.Events.*;
import com.denied403.hardcoursecheckpoints.Chat.*;
import com.denied403.hardcoursecheckpoints.Points.*;
import com.denied403.hardcoursecheckpoints.Scoreboard.ScoreboardMain;
import com.denied403.hardcoursecheckpoints.Utils.*;
import com.denied403.hardcoursecheckpoints.Utils.ColorUtil;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.*;
import static com.denied403.hardcoursecheckpoints.Scoreboard.ScoreboardMain.updateScoreboard;

public final class HardcourseCheckpoints extends JavaPlugin implements Listener {

    private File wordsFile;
    private FileConfiguration wordsConfig;

    private PointsManager pointsManager;

    public static HardcourseCheckpoints plugin;

    @Override
    public void onEnable() {
        plugin = this;
        CheckpointDatabase checkpointDatabase = new CheckpointDatabase(this);
        loadConfigValues(this);

        PointsManager.initialize(checkpointDatabase);
        onWalk.initalize(checkpointDatabase);
        CheckpointCommand.initialize(checkpointDatabase);
        HardcourseDiscord.initialize(checkpointDatabase);
        onChat.initialize(checkpointDatabase);
        onJoin.initialize(checkpointDatabase);
        onQuit.initialize(checkpointDatabase);
        ScoreboardMain.initialize(checkpointDatabase);
        Info.initialize(checkpointDatabase);
        Points.initialize(checkpointDatabase);

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
        getServer().getPluginManager().registerEvents(new JumpBoost(), this);
        getServer().getPluginManager().registerEvents(new DoubleJump(), this);
        getServer().getPluginManager().registerEvents(new TempCheckpoint(), this);
        getServer().getPluginManager().registerEvents(pointsShop, this);
        getServer().getPluginManager().registerEvents(new JumpBoost(), this);
        getServer().getPluginManager().registerEvents(new onPortalEnter(), this);
        getServer().getPluginManager().registerEvents(new EndTrail(this), this);
        getServer().getPluginManager().registerEvents(new OminousTrail(this), this);
        getServer().getPluginManager().registerEvents(new ReportListener(), this);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, cmd -> {
            cmd.registrar().register(Clock.createCommand("clock"));
            cmd.registrar().register(Clock.createCommand("shop"));
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
            cmd.registrar().register(Points.createCommand(this, pointsManager, "points"));
            cmd.registrar().register(Points.createCommand(this, pointsManager, "pts"));
            cmd.registrar().register(EndTrail.createCommand("endtrail"));
            cmd.registrar().register(OminousTrail.createCommand("ominoustrail"));
        });

        setupWordsConfig();

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if(isBroadcastEnabled()){
                String message = messages.get(random.nextInt(messages.size()));
                Bukkit.broadcast(ColorUtil.Colorize(" "));
                Bukkit.broadcast(ColorUtil.Colorize("&c&lHARDCOURSE &r" + message));
                Bukkit.broadcast(ColorUtil.Colorize(" "));
            }}, 0L, 20 * 60 * 5);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(isUnscrambleEnabled()) {
                    ChatReactions.runGame(ChatReactions.getRandomWord());
                }
            }}.runTaskTimer(this, 0L, 20 * 60 * 4);

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

    public void reloadWordsConfig() {
        wordsConfig = YamlConfiguration.loadConfiguration(wordsFile);
    }
    public List<String> getApplicationQuestions(){return getConfig().getStringList("application-questions");}

    private static List<String> messages = new ArrayList<>();
    private static boolean DiscordEnabled;
    private static boolean BroadcastEnabled;
    private static boolean UnscrambleEnabled;
    private static List<String> exemptions;
    private final Random random = new Random();

    public static void loadConfigValues(HardcourseCheckpoints plugin) {
        FileConfiguration config = plugin.getConfig();
        DiscordEnabled = config.getBoolean("discord-enabled");
        BroadcastEnabled = config.getBoolean("broadcast-enabled");
        UnscrambleEnabled = config.getBoolean("unscramble-enabled");
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