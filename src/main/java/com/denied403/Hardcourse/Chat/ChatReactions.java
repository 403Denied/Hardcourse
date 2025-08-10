package com.denied403.Hardcourse.Chat;

import com.denied403.Hardcourse.Points.PointsManager;
import com.denied403.Hardcourse.Hardcourse;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import static com.denied403.Hardcourse.Hardcourse.isDev;
import static com.denied403.Hardcourse.Hardcourse.isUnscrambleEnabled;
import static com.denied403.Hardcourse.Utils.ColorUtil.Colorize;
import static com.denied403.Hardcourse.Utils.ColorUtil.stripAllColors;

public class ChatReactions implements Listener {

    private static FileConfiguration wordConfig;
    private static final Random random = new Random();
    private static String currentWord;
    public static boolean gameActive = false;
    private static Plugin plugin;

    public ChatReactions(Plugin plugin) {
        ChatReactions.plugin = plugin;
        File wordFile = new File(plugin.getDataFolder(), "words.yml");
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
        if(!gameActive) {
            currentWord = word;
            String scrambledWord = Shuffler.shuffleWord(currentWord);
            Bukkit.broadcast(Colorize("&c&lHARDCOURSE&r <hover:show_text:'" + scrambledWord + "'>Hover here for a word to unscramble."));
            Bukkit.getConsoleSender().sendMessage(Colorize("&c&lHARDCOURSE &r&cUnscramble: &f" + scrambledWord + " &c(" + currentWord + ")"));

            gameActive = true;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (gameActive) {
                        Bukkit.broadcast(Colorize("&c&lHARDCOURSE &rTime's Up! The correct word was &c" + currentWord));
                        gameActive = false;
                    }
                }
            }.runTaskLater(plugin, 600L);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        String message = LegacyComponentSerializer.legacySection().serialize(event.message());
        if (gameActive && message.equalsIgnoreCase(currentWord)) {
            Player p = event.getPlayer();
            if(isDev()) {
                int points = 5 + random.nextInt(11);
                PointsManager pointsManager = ((Hardcourse) plugin).getPointsManager();
                pointsManager.addPoints(p.getUniqueId(), points);
                Bukkit.broadcast(Colorize("&c&lHARDCOURSE &r&c" + stripAllColors(p.getName()) + "&r successfully unscrambled the word and earned &c" + points + "&f points! It was &c" + currentWord));
            } else {
                Bukkit.broadcast(Colorize("&c&lHARDCOURSE &r&c" + stripAllColors(p.getName()) + "&r sucessfully unscrambled the word! It was &c" + currentWord));
            }

            event.setCancelled(true);
            gameActive = false;
        }
    }

    public static String getCurrentWord() {
        return currentWord;
    }
}
