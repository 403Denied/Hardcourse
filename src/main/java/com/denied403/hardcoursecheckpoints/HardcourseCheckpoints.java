package com.denied403.hardcoursecheckpoints;

import com.denied403.hardcoursecheckpoints.Commands.*;
import com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord;
import com.denied403.hardcoursecheckpoints.Events.*;
import com.denied403.hardcoursecheckpoints.Chat.ChatReactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.WorldCreator;
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
    public static Double getHighestCheckpoint(UUID player) {
        if(highestCheckpoint.containsKey(player)) {
            return highestCheckpoint.get(player);
        }
        else {
            highestCheckpoint.put(player, 0.0);
            return 1.0;
        }
    }
    public static HardcourseCheckpoints plugin;
    private HardcourseDiscord discordBot;
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        discordBot = new HardcourseDiscord(this);
        try{
            discordBot.InitJDA();
        } catch (Exception e){
            getLogger().severe("Failed to initialize Discord bot: " + e.getMessage());
        }
        checkpointFile = new File(getDataFolder(), "checkpoints.yml");
        if(!checkpointFile.exists()) {
            saveResource("checkpoints.yml", false);
        }
        sendMessage(null, null, "starting", null);
        checkpointConfig = YamlConfiguration.loadConfiguration(checkpointFile);
        loadCheckpoints();
        ChatReactions chatReactions = new ChatReactions(this);
        getServer().getPluginManager().registerEvents(chatReactions, this);
        getServer().getPluginManager().registerEvents(new onJoin(), this);
        getServer().getPluginManager().registerEvents(new onDrop(), this);
        getServer().getPluginManager().registerEvents(new onClick(), this);
        getServer().getPluginManager().registerEvents(new onWalk(this), this);
        getServer().getPluginManager().registerEvents(new onChat(), this);
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("resetcheckpoint").setExecutor(new CheckpointCommands(this));
        getCommand("resetallcheckpoints").setExecutor(new CheckpointCommands(this));
        getCommand("discord").setExecutor(new Discord());
        getCommand("apply").setExecutor(new Apply());
        getCommand("playtime").setExecutor(new Playtime());
        getCommand("clock").setExecutor(new Clock());
        getCommand("runchatgame").setExecutor(new runChatGame());
        getCommand("endchatgame").setExecutor(new endChatGame());

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            String message = messages.get(random.nextInt(messages.size()));
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &r" + message));
            Bukkit.broadcastMessage(" ");
        }, 0L, 20 * 60 * 5); //Runs every 5 mins

        new BukkitRunnable(){
            @Override
            public void run(){
                ChatReactions.runGame(ChatReactions.getRandomWord());
            }
        }.runTaskTimer(this, 0L, 4800L);

    }

    @Override
    public void onDisable() {
        sendMessage(null, null, "stopping", null);
        saveCheckpoints();
        if(jda != null){
            jda.shutdownNow();
        }
    }

    public void loadCheckpoints(){
        for (String key : checkpointConfig.getKeys(false)){
            UUID uuid = UUID.fromString(key);
            Double checkpoint = checkpointConfig.getDouble(key);
            highestCheckpoint.put(uuid, checkpoint);
        }
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


    private List<String> messages = Arrays.asList(
            "See someone hacking? Don't call them out in chat, instead use &c/report <player> <reason>",
            "Join our discord server at &c/discord",
            "See an issue? Please notify a staff member to have it fixed!",
            "Need help? Feel free to ask, or make a ticket in our discord!",
            "All jumps have been tested and &c&lAre Possible&r.",
            "While all of our levels have undergone testing, that was many years ago in another version. If something doesn't work, please alert a staff member and it will be fixed promptly.",
            "Lost your kill item? Do &c/clock&r for a new one."

    );
    private Random random = new Random();
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        sendMessage(e.getPlayer(), null, "leave", null);
    }
}

