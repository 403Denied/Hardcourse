package com.denied403.hardcoursecheckpoints.Events;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import static com.denied403.hardcoursecheckpoints.Chat.ChatReactions.getCurrentWord;
import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDiscordEnabled;

public class onChat implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncChat(AsyncChatEvent event) {
        String content = LegacyComponentSerializer.legacySection().serialize(event.message());
        if(!isDiscordEnabled()){
            return;
        }
        if (event.isCancelled()){
            if(content.startsWith("#") && event.getPlayer().hasPermission("hardcourse.jrmod")){
                return;
            }
            if(content.equalsIgnoreCase(getCurrentWord())){
                return;
            }
            sendMessage(event.getPlayer(), content, "logs", "true", null);
            return;
        }
        sendMessage(event.getPlayer(), content, "logs", "false", null);
        Player player = event.getPlayer();
        content = content
                .replaceAll("@everyone", "`@everyone`")
                .replaceAll("@here", "`@here`")
                .replaceAll("<@", "`<@`")
                .replaceAll("https://", "`https://`")
                .replaceAll("http://", "`http://`");

        String world = player.getWorld().getName().toLowerCase();
        String level = switch (world) {
            case "season1" -> "1-";
            case "season2" -> "2-";
            case "season3" -> "3-";
            case "season4" -> "4-";
            default -> "";
        };
        if (content.startsWith("#") && player.hasPermission("hardcourse.jrmod")) {
            sendMessage(player, content.substring(1), "staffchat", null, null);
        } else {
            if (!player.hasPermission("hardcourse.jrmod")){
                sendMessage(player, content, "chat", level, null);
            } else {
                sendMessage(player, content, "staffmessage", level, player.getUniqueId().toString());
            }
        }
    }
}
