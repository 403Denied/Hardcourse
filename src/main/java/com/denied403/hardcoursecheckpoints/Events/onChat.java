package com.denied403.hardcoursecheckpoints.Events;

import com.denied403.hardcoursecheckpoints.Utils.WordSyncListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.regex.Pattern;

import static com.denied403.hardcoursecheckpoints.Chat.ChatReactions.*;
import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDiscordEnabled;
import static com.denied403.hardcoursecheckpoints.Utils.WordSyncListener.isPlayerMuted;

public class onChat implements Listener {
    private static final Map<Character, String> replacements = Map.of(
            'a', "[a4@9]",
            'e', "[e3]",
            'i', "[i1!\\|]",
            'o', "[o0]",
            's', "[s$5]",
            't', "[t7+]",
            'g', "[g96]"
    );


    private String buildPattern(String word) {
        StringBuilder pattern = new StringBuilder();
        for (char c : word.toLowerCase().toCharArray()) {
            String charClass = replacements.getOrDefault(c, Pattern.quote(String.valueOf(c)));
            pattern.append(charClass).append("\\s*[_\\-]*\\s*");
        }
        return ".*" + pattern + ".*";
    }


    private boolean isBlocked(String message) {
        List<String> whitelist = WordSyncListener.getWhitelistedWords();
        List<String> blacklist = WordSyncListener.getBlacklistedWords();

        if (whitelist == null || blacklist == null) {
            Bukkit.getLogger().warning("[Hardcourse] Word filters not initialized, allowing message.");
            return false;
        }

        for (String allow : whitelist) {
            if (Pattern.compile(buildPattern(allow), Pattern.CASE_INSENSITIVE).matcher(message).find()) {
                return false;
            }
        }
        for (String deny : blacklist) {
            if (Pattern.compile(buildPattern(deny), Pattern.CASE_INSENSITIVE).matcher(message).find()) {
                return true;
            }
        }
        return false;
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onChatEvent(AsyncPlayerChatEvent event) {
        if (isDiscordEnabled()) {
            String content = event.getMessage()
                    .replaceAll("@everyone", "`@everyone`")
                    .replaceAll("@here", "`@here`")
                    .replaceAll("<@", "`<@`")
                    .replaceAll("https://", "`https://`")
                    .replaceAll("http://", "`http://`");

            if (isPlayerMuted(event.getPlayer().getUniqueId())) {
                return;
            }
            if (isBlocked(content)) {
                return;
            }
            if (gameActive && event.getMessage().equalsIgnoreCase(getCurrentWord())) {
                return;
            }
            if(event.isCancelled()){
                return;
            }

            if (content.startsWith("#")) {
                if (event.getPlayer().hasPermission("hardcourse.jrmod")) {
                    sendMessage(event.getPlayer(), content.substring(1), "staffchat", null, null);
                } else {
                    sendMessage(event.getPlayer(), content, "chat", null, null);
                }
            } else {
                Player p = event.getPlayer();
                String level;
                if (p.hasPermission("hardcourse.season1")) {
                    level = "1-";
                }
                if (p.hasPermission("hardcourse.season2")) {
                    level = "2-";
                }
                if (p.hasPermission("hardcourse.season3")) {
                    level = "3-";
                } else {
                    level = "";
                }
                if (event.getPlayer().hasPermission("hardcourse.jrmod")) {
                    sendMessage(event.getPlayer(), content, "staffmessage", level, null);
                } else {
                    sendMessage(event.getPlayer(), content, "chat", level, null);
                }
            }
        }
    }
}
