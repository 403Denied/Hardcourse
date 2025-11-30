package com.denied403.Hardcourse.Discord;

import com.denied403.Hardcourse.Utils.CheckpointDatabase;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.chatChannel;
import static com.denied403.Hardcourse.Discord.HardcourseDiscord.staffChatChannel;
import static com.denied403.Hardcourse.Hardcourse.isDiscordEnabled;
import static com.denied403.Hardcourse.Utils.ColorUtil.Colorize;
import static com.denied403.Hardcourse.Utils.Luckperms.getLuckPermsPrefix;
import static com.denied403.Hardcourse.Utils.Luckperms.hasLuckPermsPermission;

public final class DiscordListener extends ListenerAdapter {
    private static CheckpointDatabase database;
    public static void initialize(CheckpointDatabase db) {database = db;}

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!isDiscordEnabled()) return;

        Member member = event.getMember();
        if (member == null || member.getUser().isBot()) return;

        String discordMessage = event.getMessage().getContentRaw();
        boolean staffChat = event.getChannel().equals(staffChatChannel);

        if (!staffChat && !event.getChannel().equals(chatChannel)) return;

        String discordId = member.getId();
        String linkedUuidString = database.getUUIDFromDiscord(discordId);
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

                broadcastMessage(displayName, discordMessage, staffChat, hasPermission);
            });
        } else {
            String displayName = getBestDiscordRoleColor(member) + member.getEffectiveName();
            broadcastMessage(displayName, discordMessage, staffChat, false);
        }
    }

    private void broadcastMessage(String displayName, String message, boolean staffChat, boolean hasPermission) {
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
