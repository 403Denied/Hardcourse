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

import com.denied403.core403.Commands.Economy.Vault;
import com.denied403.core403.Core403;
import com.denied403.core403.Punishments.Database.PunishmentDatabase;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.*;
import static com.denied403.Hardcourse.Utils.CheckpointLevelTimer.shutdown;

public final class Hardcourse extends JavaPlugin implements Listener {
    public static Hardcourse plugin;
    public static CheckpointDatabase checkpointDatabase;
    public static LinkManager linkManager;
    public static PunishmentDatabase punishmentDatabase;
    public static CheckpointUpdating checkpointUpdating;
    public static Economy econ;
    public static PluginManager eventRegistrar;

    @Override
    public void onEnable() {
        plugin = this;
        checkpointDatabase = new CheckpointDatabase();
        linkManager = new LinkManager();
        punishmentDatabase = Core403.database;
        checkpointUpdating = new CheckpointUpdating();
        eventRegistrar = Bukkit.getPluginManager();
        saveDefaultConfig();
        loadConfigValues();
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {new Placeholders().register();}
        if(Bukkit.getPluginManager().getPlugin("Vault") != null) {Vault.register();}
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp != null) {econ = rsp.getProvider();}

        try {
            InitJDA();
        } catch (Exception e) {
            getLogger().severe("Failed to initialize Discord bot: " + e.getMessage());
        }

        sendMessage(null, null, "starting", null, null);

        eventRegistrar.registerEvents(new MiscEvents(), this);
        eventRegistrar.registerEvents(new ChatReactions(), this);
        eventRegistrar.registerEvents(new onJoin(), this);
        eventRegistrar.registerEvents(new onClick(), this);
        eventRegistrar.registerEvents(new onWalk(), this);
        eventRegistrar.registerEvents(new onChat(), this);
        eventRegistrar.registerEvents(new PunishmentListener(), this);
        eventRegistrar.registerEvents(new onQuit(), this);
        eventRegistrar.registerEvents(new onSneak(), this);
        eventRegistrar.registerEvents(new onDeath(), this);
        eventRegistrar.registerEvents(new CheckpointLevelTimer(), this);
        eventRegistrar.registerEvents(new OminousTrail(this), this);
        eventRegistrar.registerEvents(new EndTrail(this), this);
        eventRegistrar.registerEvents(new PointsShop(), this);
        eventRegistrar.registerEvents(new JumpBoost(), this);
        eventRegistrar.registerEvents(new DoubleJump(), this);
        eventRegistrar.registerEvents(new TempCheckpoint(), this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands registrar = event.registrar();
            registrar.register(Clock.createCommand("clock"));
            registrar.register(CheckpointCommand.createCommand("checkpoint"));
            registrar.register(CheckpointCommand.createCommand("checkpoints"));
            registrar.register(EndChatGame.createCommand("endChatGame"));
            registrar.register(EndChatGame.createCommand("ecg"));
            registrar.register(RunChatGame.createCommand("runChatGame"));
            registrar.register(RunChatGame.createCommand("rcg"));
            registrar.register(ReloadHardcourse.createCommand("reloadHardcourse"));
            registrar.register(WinnerTP.createCommand("winnerTp"));
            registrar.register(WinnerTP.createCommand("wtp"));
            registrar.register(ToggleDiabolicalUnscrambles.createCommand("toggleDiabolicalUnscrambles"));
            registrar.register(Deaths.createCommand("deaths"));
            registrar.register(EndTrail.createCommand("endTrail"));
            registrar.register(OminousTrail.createCommand("ominousTrail"));
            registrar.register(Skip.createCommand("skip"));
            registrar.register(Skip.createCommand("skips"));
            if(DiscordEnabled) {
                registrar.register(Link.createCommand("link"));
                registrar.register(Unlink.createCommand("unlink"));
            }
        });

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if(UnscrambleEnabled) {
                if(Bukkit.getOnlinePlayers().isEmpty()) return;
                ChatReactions.runGame(ChatReactions.getRandomWord());
            }}, 0L, 4800);
    }

    @Override
    public void onDisable() {
        sendMessage(null, null, "stopping", null, null);
        if(jda != null){
            jda.shutdown();
        }
        shutdown();
    }

    public static boolean DiscordEnabled;
    public static boolean UnscrambleEnabled;
    public static boolean isDev;
    public static List<String> exemptions;
    public static List<String> applicationQuestions;

    public static void loadConfigValues() {
        FileConfiguration config = plugin.getConfig();
        DiscordEnabled = config.getBoolean("discord-enabled");
        UnscrambleEnabled = config.getBoolean("unscramble-enabled");
        isDev = config.getBoolean("is-dev");
        exemptions = config.getStringList("skip-alert-exemptions");
        applicationQuestions = config.getStringList("application-questions");
    }

    public static boolean isSkipExempted(int from, int to) {
        for (String entry : exemptions) {
            String[] parts = entry.split("-");
            if (parts.length != 2) continue;

            String rawFrom = parts[0].trim();
            String rawTo = parts[1].trim();

            boolean fromMatches = rawFrom.equals("*") || Integer.toString(from).equals(rawFrom);
            boolean toMatches = rawTo.equals("*") || Integer.toString(to).equals(rawTo);

            if (fromMatches && toMatches) {
                return false;
            }
        }
        return true;
    }
}