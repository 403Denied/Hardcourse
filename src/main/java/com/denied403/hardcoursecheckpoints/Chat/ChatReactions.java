package com.denied403.hardcoursecheckpoints.Chat;

import com.denied403.hardcoursecheckpoints.Points.PointsManager;
import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
        if(!gameActive) {
            currentWord = word;
            scrambledWord = Shuffler.shuffleWord(currentWord);
            MiniMessage mm = MiniMessage.miniMessage();

            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                Component message = mm.deserialize("<red><bold>HARDCOURSE<reset> <hover:show_text:'" + scrambledWord + "'>Hover here for a word to unscramble.</hover>");
                p.sendMessage(message);
            }
            Audience audience = Bukkit.getConsoleSender();
            audience.sendMessage(Component.text()
                    .append(Component.text("HARDCOURSE ", NamedTextColor.RED, TextDecoration.BOLD))
                    .append(Component.text("Unscramble: ", NamedTextColor.WHITE))
                    .append(Component.text(scrambledWord + " ", NamedTextColor.RED))
                    .append(Component.text("(" + currentWord + ")", NamedTextColor.WHITE)));


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
            }.runTaskLater(plugin, 600L);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        String message = LegacyComponentSerializer.legacySection().serialize(event.message());
        if (gameActive && message.equalsIgnoreCase(currentWord)) {
            Player p = event.getPlayer();
            int points = 5 + random.nextInt(11);

            PointsManager pointsManager = ((HardcourseCheckpoints) plugin).getPointsManager();
            pointsManager.addPoints(p.getUniqueId(), points);

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
