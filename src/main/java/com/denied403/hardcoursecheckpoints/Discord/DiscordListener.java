package com.denied403.hardcoursecheckpoints.Discord;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.chatChannel;
import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.staffChatChannel;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDiscordEnabled;
import static com.denied403.hardcoursecheckpoints.Utils.ColorUtil.Colorize;

public final class DiscordListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(isDiscordEnabled()) {
            if (event.getChannel().equals(chatChannel)) {
                Member member = event.getMember();
                if (member == null || member.getUser().isBot()) return;

                String message = event.getMessage().getContentRaw();
                String name = member.getEffectiveName();
                Component formattedMessage;
                String firstFormattedMessage;

                String hexColor = "#FFFFFF";

                if (member.getRoles().isEmpty()) {
                    firstFormattedMessage = "&a&lDC &r&7" + name + " &f: ";
                } else {
                    Role firstRole = member.getRoles().getFirst();
                    if (firstRole.getColor() != null) {
                        String hex = Integer.toHexString(firstRole.getColor().getRGB()).toUpperCase();
                        if (hex.length() == 8) hex = hex.substring(2);
                        hexColor = "#" + hex;
                    }
                    firstFormattedMessage = "&a&lDC &r<" + hexColor + ">" + name + "&f: ";
                }

                if (member.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase("Staff"))) {
                    formattedMessage = Colorize(firstFormattedMessage + "&f" + message);
                } else {
                    formattedMessage = Colorize(firstFormattedMessage + "&7" + message);
                }
                Bukkit.broadcast(formattedMessage);
            }

            if (event.getChannel().equals(staffChatChannel)) {
                Member member = event.getMember();
                if (member == null || member.getUser().isBot()) return;
                String message = event.getMessage().getContentRaw();
                String name = member.getEffectiveName();
                String finalFormattedMessage = "&a&lSC &r" + name + ": " + message;
                Bukkit.broadcast(Colorize(finalFormattedMessage), "hardcourse.jrmod");
            }
        }
    }
}