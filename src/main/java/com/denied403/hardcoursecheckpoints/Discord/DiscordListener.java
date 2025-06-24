package com.denied403.hardcoursecheckpoints.Discord;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.chatChannel;
import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.staffChatChannel;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDiscordEnabled;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.plugin;

public final class DiscordListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(isDiscordEnabled()) {
            if (event.getChannel().equals(chatChannel)) {
                Member member = event.getMember();
                if (member == null || member.getUser().isBot()) return;

                String message = event.getMessage().getContentRaw();
                String name = member.getEffectiveName();
                String formattedMessage = null;
                String firstFormattedMessage = null;

                // Initialize default role color
                String hexColor = "#FFFFFF";  // Default color (white)

                // Check if the member has roles and access the first role safely
                if (member.getRoles().isEmpty()) {
                    firstFormattedMessage = "§a§lDC §7" + name + " §f: ";
                } else {
                    Role firstRole = member.getRoles().get(0);
                    if (firstRole.getColor() != null) {
                        String hex = Integer.toHexString(firstRole.getColor().getRGB()).toUpperCase();
                        if (hex.length() == 8) hex = hex.substring(2);  // Strip alpha if present
                        hexColor = "#" + hex;
                    }
                    TextComponent nameComponent = new TextComponent(name);
                    nameComponent.setColor(net.md_5.bungee.api.ChatColor.of(hexColor));
                    firstFormattedMessage = "§a§lDC " + nameComponent.toLegacyText() + "§f: ";
                }

                // Check if the member has the "Staff" role
                if (member.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase("Staff"))) {
                    formattedMessage = firstFormattedMessage + "§f" + message;
                } else {
                    formattedMessage = firstFormattedMessage + "§7" + message;
                }

                String finalFormattedMessage = formattedMessage;
                Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(EmojiParser.parseToAliases(finalFormattedMessage.replaceAll("%message", ChatColor.stripColor(message)))));
            }

            // Staff chat handling remains the same
            if (event.getChannel().equals(staffChatChannel)) {
                Member member = event.getMember();
                if (member == null || member.getUser().isBot()) return;
                String message = event.getMessage().getContentRaw();
                String name = member.getEffectiveName();
                String formattedMessage = "§a§lSC §f" + name + ": " + message;
                String finalFormattedMessage = formattedMessage;
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (player.hasPermission("hardcourse.jrmod")) {
                        player.sendMessage(EmojiParser.parseToAliases(finalFormattedMessage.replaceAll("%message", ChatColor.stripColor(message))));
                    }
                }
            }
        }
    }
}