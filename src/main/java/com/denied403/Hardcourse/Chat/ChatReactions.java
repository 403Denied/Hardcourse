package com.denied403.Hardcourse.Chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import static com.denied403.Hardcourse.Hardcourse.*;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;
import static com.transfemme.dev.core403.Util.ColorUtil.stripAllColors;

public class ChatReactions implements Listener {

    private static FileConfiguration wordConfig;
    private static final Random random = new Random();
    private static String currentWord;
    public static boolean gameActive = false;

    public ChatReactions() {
        File wordFile = new File(plugin.getDataFolder(), "words.yml");
        wordConfig = YamlConfiguration.loadConfiguration(wordFile);

        if (UnscrambleEnabled && !wordFile.exists()) {
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

    public static String randomizeCapitalization(String word) {
        StringBuilder randomized = new StringBuilder();
        for (char c : word.toCharArray()) {
            if (random.nextBoolean()) {
                randomized.append(Character.toUpperCase(c));
            } else {
                randomized.append(Character.toLowerCase(c));
            }
        }
        return randomized.toString();
    }

    public static void runGame(String word) {
        if(!gameActive) {
            currentWord = word;
            if(DiabolicalUnscrambles) {
                currentWord = randomizeCapitalization(currentWord);
            }
            String scrambledWord = Shuffler.shuffleWord(currentWord);
            Bukkit.broadcast(Colorize("<prefix><hover:show_text:'" + scrambledWord + "'>Hover here for a word to unscramble."));
            Bukkit.getConsoleSender().sendMessage(Colorize("<prefix>Unscramble: <accent>" + scrambledWord + " <main>(<accent>" + currentWord + "<main>)"));

            gameActive = true;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (gameActive) {
                        Bukkit.broadcast(Colorize("<prefix>Time's Up! The correct word was <accent>" + currentWord));
                        gameActive = false;
                    }
                }
            }.runTaskLater(plugin, 600L);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        String message = LegacyComponentSerializer.legacySection().serialize(event.message());
        if (gameActive) {
            if ((!DiabolicalUnscrambles && message.equalsIgnoreCase(currentWord)) || (DiabolicalUnscrambles && message.equals(currentWord))) {
                Player p = event.getPlayer();
                if (isDev) {
                    int points = 5 + random.nextInt(11);
                    pointsManager.addPoints(p.getUniqueId(), points);
                    Bukkit.broadcast(Colorize("<prefix><accent>" + stripAllColors(p.getName()) + "<main> successfully unscrambled the word and earned <accent>" + points + "<main> points! It was <accent>" + currentWord));
                } else {
                    Bukkit.broadcast(Colorize("<prefix><accent>" + stripAllColors(p.getName()) + "<main> successfully unscrambled the word! It was <accent>" + currentWord));
                }
                event.setCancelled(true);
                gameActive = false;
            }
        }
    }

    public static String getCurrentWord() {
        return currentWord;
    }
}
