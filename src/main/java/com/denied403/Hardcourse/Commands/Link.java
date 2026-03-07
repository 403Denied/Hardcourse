package com.denied403.Hardcourse.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.*;
import static com.denied403.Hardcourse.Hardcourse.*;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class Link {

    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    if (!(sender instanceof Player player)) return 0;
                    if (checkpointDatabase.isLinked(player.getUniqueId())) {
                        player.sendMessage(Colorize("<prefix>You're already linked! Use <accent>/unlink<main> if you want to unlink your account."));
                        return 1;
                    }
                    if (!DiscordEnabled) {
                        player.sendMessage(Colorize("<prefix>Discord functionality is currently disabled. Please try again later."));
                        return 1;
                    }
                    String code = linkManager.createLinkCode(player.getUniqueId());
                    player.sendMessage(Colorize("<prefix>Your link code is: <accent><click:copy_to_clipboard:'" + code + "'><accent>" + code + "<other> (click to copy)<reset>\n<main>Use the <accent>/link<main> command on Discord to link your account."));
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("player", StringArgumentType.word())
                        .requires(source -> source.getSender().hasPermission("hardcourse.staff"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();

                                    if (!DiscordEnabled) {
                                        sender.sendMessage(Colorize("<prefix>Discord functionality is currently disabled. Please try again later."));
                                        return 1;
                                    }

                                    String playerName = StringArgumentType.getString(ctx, "player");
                                    String discordId = StringArgumentType.getString(ctx, "id");
                                    OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

                                    if (!target.hasPlayedBefore() && !target.isOnline()) {
                                        sender.sendMessage(Colorize("<prefix>Player <accent>" + playerName + "<main> has never joined this server."));
                                        return 1;
                                    }

                                    if (checkpointDatabase.isLinked(target.getUniqueId())) {
                                        sender.sendMessage(Colorize("<prefix><accent>" + target.getName() + "<main> is already linked to a Discord account."));
                                        return 1;
                                    }

                                    guild.retrieveMemberById(discordId).queue(
                                            member -> {
                                                if (member.getRoles().contains(linkedRole)) {
                                                    sender.sendMessage(Colorize("<prefix>That Discord user already has the linked role."));
                                                    return;
                                                }

                                                checkpointDatabase.linkDiscord(target.getUniqueId(), discordId);

                                                guild.addRoleToMember(member, linkedRole).queue(
                                                        success -> {},
                                                        error -> sender.sendMessage(Colorize("<prefix>⚠️ Could not assign role: <accent>" + error.getMessage()))
                                                );

                                                final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
                                                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                                                linksChannel.sendMessage("`[" + f.format(new Date()) + "] " + target.getName() + " linked to` <@" + discordId + "> (forced by " + sender.getName() + ")").queue();

                                                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcast(Colorize("<prefix><accent>" + target.getName() + " <main>just linked their Discord account and gained access to the Minecraft <-> Discord chat!")));

                                                sender.sendMessage(Colorize("<prefix>Successfully linked <accent>" + target.getName() + "<main> to Discord ID <accent>" + discordId + "<main>."));
                                            },
                                            error -> sender.sendMessage(Colorize("<prefix>No member with that Discord ID was found in the guild."))
                                    );

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .build();
    }
}