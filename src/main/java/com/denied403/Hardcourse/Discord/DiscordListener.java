package com.denied403.Hardcourse.Discord;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.*;
import static com.denied403.Hardcourse.Hardcourse.DiscordEnabled;
import static com.denied403.Hardcourse.Hardcourse.checkpointDatabase;
import static com.denied403.Hardcourse.Utils.Luckperms.getLuckPermsPrefix;
import static com.denied403.Hardcourse.Utils.Luckperms.hasLuckPermsPermission;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public final class DiscordListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!DiscordEnabled) return;

        Member member = event.getMember();
        if (member == null || member.getUser().isBot()) return;

        String discordMessage = event.getMessage().getContentRaw();
        boolean staffChat = event.getChannel().equals(staffChatChannel);

        if (!staffChat && !event.getChannel().equals(chatChannel)) return;

        String discordId = member.getId();
        String linkedUuidString = checkpointDatabase.getUUIDFromDiscord(discordId);
        UUID linkedUUID;
        if (linkedUuidString != null) {
            linkedUUID = UUID.fromString(linkedUuidString);
        } else {
            linkedUUID = null;
        }

        if (linkedUUID != null) {
            getLuckPermsPrefix(linkedUUID).thenAccept(prefix -> {
                String mcName = Bukkit.getOfflinePlayer(linkedUUID).getName();
                if (mcName == null) mcName = "Unknown";

                String displayName = (prefix != null) ? prefix + mcName : mcName;

                boolean hasPermission = hasLuckPermsPermission(linkedUUID, "hardcourse.jrmod");

                broadcastMessage(mcName, displayName, discordMessage, staffChat, hasPermission);
            });
        } else {
            String displayName = getBestDiscordRoleColor(member) + member.getEffectiveName();
            broadcastMessage(member.getEffectiveName(), displayName, discordMessage, staffChat, false);
        }
    }

    private void broadcastMessage(String name, String displayName, String message, boolean staffChat, boolean hasPermission) {
        String prefix = staffChat ? "&a&lSC &r" : "&a&lDC &r";
        String coloredMessage;

        if (staffChat) {
            if (!hasPermission) return;
            coloredMessage = "&f" + message;
        } else {
            coloredMessage = hasPermission ? "&f" + message : "&7" + message;
        }

        Component finalMessage = Colorize(prefix + displayName + "&f: " + coloredMessage);

        if (staffChat) {
            Bukkit.broadcast(finalMessage, "hardcourse.jrmod");
        } else {
            Bukkit.broadcast(finalMessage);
            final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            logsChannel.sendMessage("`[DISCORD] [" + f.format(new Date()) + "] " + name + ": " + message.replaceAll("`", "'") + "`").queue();
        }
    }

    private String getBestDiscordRoleColor(Member member) {
        if (member.getRoles().isEmpty()) return "<#FFFFFF>";

        for (Role role : member.getRoles()) {
            if (role.getColor() == null) continue;

            String hex = Integer.toHexString(role.getColor().getRGB()).toUpperCase();
            if (hex.length() == 8) hex = hex.substring(2);
            String foundColor = "#" + hex;

            if (!foundColor.equalsIgnoreCase("#FFFFFF")) {
                return "<" + foundColor + ">";
            }
        }

        return "<#FFFFFF>";
    }
}
