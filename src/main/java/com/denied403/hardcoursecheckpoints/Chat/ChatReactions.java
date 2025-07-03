package com.denied403.hardcoursecheckpoints.Chat;

import com.denied403.hardcoursecheckpoints.Points.PointsManager;
import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isUnscrambleEnabled;

public class ChatReactions implements Listener {

    private static File wordFile;
    private static FileConfiguration wordConfig;
    private static final Random random = new Random();
    private static String currentWord;
    private static String scrambledWord;
    public static boolean gameActive = false;
    private static Plugin plugin;

    public ChatReactions(Plugin plugin) {
        ChatReactions.plugin = plugin;
        wordFile = new File(plugin.getDataFolder(), "words.yml");
        wordConfig = YamlConfiguration.loadConfiguration(wordFile);

        if (isUnscrambleEnabled() && !wordFile.exists()) {
            try {
                wordFile.createNewFile();
                wordConfig.set("words", List.of("Word 1", "Word 2"));
                wordConfig.save(wordFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getRandomWord() {
        List<String> words = wordConfig.getStringList("words");
        if (words.isEmpty()) {
            return "Error";
        }
        return words.get(random.nextInt(words.size()));
    }

    public static void runGame(String word) {
        currentWord = word;
        scrambledWord = Shuffler.shuffleWord(currentWord);
        MiniMessage mm = MiniMessage.miniMessage();

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            Component message = mm.deserialize("<red><bold>HARDCOURSE<reset> <hover:show_text:'" + scrambledWord + "'>Hover here for a word to unscramble.</hover>");
            p.sendMessage(message);
        }
        Bukkit.getServer().getLogger().info("&c&lHARDCOURSE &fHover here for a word to unscramble: &c" + scrambledWord + "&f(" + currentWord + ")");

        gameActive = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameActive) {
                    Component endMsg = mm.deserialize("<red><bold>HARDCOURSE</bold></red> <reset>Time's Up! The correct word was <red>" + currentWord + "</red>");
                    Bukkit.broadcast(endMsg);
                    gameActive = false;
                }
            }
        }.runTaskLater(plugin, 600L); // Run after 30 seconds (600 ticks)
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (gameActive && event.getMessage().equalsIgnoreCase(currentWord)) {
            Player p = event.getPlayer();
            int points = 50 + random.nextInt(51); // Random between 50 and 100 inclusive

            PointsManager pointsManager = ((HardcourseCheckpoints) plugin).getPointsManager();
            pointsManager.addPoints(p.getUniqueId(), points);
            pointsManager.sendPointsActionBar(p);

            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                    "&c&lHARDCOURSE &r&c" + p.getDisplayName() +
                            "&r successfully unscrambled the word! It was &c" + currentWord +
                            "&r and earned &a" + points + " points&r!"));

            event.setCancelled(true);
            gameActive = false;
        }
    }

    public static String getCurrentWord() {
        return currentWord;
    }
}
