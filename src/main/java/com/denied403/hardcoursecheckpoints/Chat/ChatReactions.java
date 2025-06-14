package com.denied403.hardcoursecheckpoints.Chat;

import com.denied403.hardcoursecheckpoints.Chat.Shuffler;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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

public class ChatReactions implements Listener {

    private static File wordFile;
    private static FileConfiguration wordConfig;
    private static Random random;
    private static String currentWord;
    private static String scrambledWord;
    public static boolean gameActive = false;
    private static Plugin plugin;

    public ChatReactions(Plugin plugin) {
        ChatReactions.plugin = plugin;
        wordFile = new File(plugin.getDataFolder(), "words.yml");
        wordConfig = YamlConfiguration.loadConfiguration(wordFile);
        random = new Random();

        if (!wordFile.exists()) {
            try {
                wordFile.createNewFile();
                wordConfig.set("words", List.of("Apple", "Banana", "Cherry", "Date", "Elderberry", "Grape", "Strawberry", "Hardcourse", "Pneumonoultramicroscopicsilicovolcanoconiosis", "Word", "Parkour", "Supercalifragilisticexpialidocious", "Scrambled", "Jump", "Leap", "Antidisestablishmentarianism", "Hippopotomonstrosesquipedaliophobia", "Floccinaucinihilipilification", "Sesquipedalian", "Uncharacteristically", "Incomprehensibilities"));
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
        currentWord = word;  // Store the passed word in the instance variable
        scrambledWord = Shuffler.shuffleWord(currentWord);

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            TextComponent shuffled = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rHover here for a word to unscramble."));
            shuffled.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(scrambledWord)));
            p.spigot().sendMessage(shuffled);
        }
        gameActive = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameActive) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rTime's Up! The correct word was &c" + currentWord));
                    gameActive = false;
                }
            }
        }.runTaskLater(plugin, 600L); // Run after 30 seconds (600 ticks)
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (gameActive && event.getMessage().equalsIgnoreCase(currentWord)) {
            Player p = event.getPlayer();
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &r&c" + p.getDisplayName() + "&r successfully unscrambled the word! It was &c" + currentWord));
            event.setCancelled(true);
            gameActive = false;
        }
    }

    public static String getCurrentWord() {
        return currentWord;
    }
}
