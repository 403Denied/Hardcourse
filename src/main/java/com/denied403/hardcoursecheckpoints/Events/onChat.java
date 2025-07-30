package com.denied403.hardcoursecheckpoints.Events;

import com.denied403.hardcoursecheckpoints.Utils.CheckpointDatabase;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDiscordEnabled;
import com.transfemme.dev.core403.Punishments.Api.CustomEvents.ChatFilterEvent;

public class onChat implements Listener {

    private static CheckpointDatabase database;

    public static void initialize(CheckpointDatabase db) {
        database = db;
    }
    @EventHandler
    public void onChatFiltered(ChatFilterEvent event){
        String message = event.getMessage();
        if(isDiscordEnabled()) {
            sendMessage(event.getPlayer(), message, "logs", "true", null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncChat(AsyncChatEvent event) {
        String content = LegacyComponentSerializer.legacySection().serialize(event.message());
        if(!isDiscordEnabled()){
            return;
        }
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

        String season = database.getSeason(player.getUniqueId()).toString() + "-";
        if (content.startsWith("#") && player.hasPermission("hardcourse.jrmod")) {
            sendMessage(player, content.substring(1), "staffchat", null, null);
        } else {
            if (!player.hasPermission("hardcourse.jrmod")){
                sendMessage(player, content, "chat", season, null);
            } else {
                sendMessage(player, content, "staffmessage", season, player.getUniqueId().toString());
            }
        }
    }
}
