package com.denied403.Hardcourse.Events;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.Hardcourse.Hardcourse.DiscordEnabled;
import static com.denied403.Hardcourse.Hardcourse.checkpointDatabase;
import com.transfemme.dev.core403.Punishments.Api.CustomEvents.ChatFilterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class onChat implements Listener {
    @EventHandler
    public void onChatFiltered(ChatFilterEvent event){
        String message = event.getMessage();
        if(DiscordEnabled) {
            sendMessage(event.getPlayer(), message, "logs", "true", null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncChat(AsyncChatEvent event) {
        String content = LegacyComponentSerializer.legacySection().serialize(event.message());
        if(!DiscordEnabled) return;

        if (event.isCancelled()){
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

        String season = checkpointDatabase.getSeason(player.getUniqueId()).toString() + "-";
        if (content.startsWith("#") && player.hasPermission("core403.staffchat")) {
            sendMessage(player, content.substring(1), "staffchat", null, null);
        } else {
            if (!player.hasPermission("hardcourse.jrmod")){
                sendMessage(player, content, "chat", season, null);
            } else {
                sendMessage(player, content, "staffmessage", season, player.getUniqueId().toString());
            }
        }
    }
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player player = event.getPlayer();
        if(!player.hasPermission("core403.staffchat") || !DiscordEnabled) return;
        if (command.startsWith("/sc ") || command.startsWith("/staffchat ")) {
            String message = command.replaceFirst("(?i)^/sc|^/staffchat", "").trim();
            sendMessage(player, message, "staffchat", null, null);
        }
    }
}
