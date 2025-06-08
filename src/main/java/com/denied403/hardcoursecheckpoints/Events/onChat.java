package com.denied403.hardcoursecheckpoints.Events;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static com.denied403.hardcoursecheckpoints.Chat.ChatReactions.getCurrentWord;
import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.sendMessage;

public class onChat implements Listener {
    @EventHandler
    private void onChatEvent(AsyncPlayerChatEvent event) {
        String content = event.getMessage().replaceAll("@everyone", "`@everyone`").replaceAll("@here", "`@here`");
        if (content.startsWith("#")) {
            if (event.getPlayer().hasPermission("hardcourse.jrmod")) {
                String staffChat = content.substring(1);
                sendMessage(event.getPlayer(), staffChat, "staffchat", null);
            }
            if (!event.getPlayer().hasPermission("hardcourse.jrmod")) {
                sendMessage(event.getPlayer(), content, "chat", null);
            }
        }
        if (!content.startsWith("#")) {
            if (!event.isCancelled()) {
                sendMessage(event.getPlayer(), content, "chat", null);
            } else {
                if(!event.getMessage().equalsIgnoreCase(getCurrentWord())) {
                    event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &fYour message was not sent to the discord. Was it filtered? If you believe this to be an error, please contact a staff member."));
                }
                else {
                    return;
                }
            }
        }
    }
}
