package com.denied403.Hardcourse.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.*;
import static com.denied403.Hardcourse.Hardcourse.*;
import static com.denied403.core403.Util.ColorUtil.Colorize;

public class Unlink {
    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName){
        return Commands.literal(commandName)
                .requires(source -> source.getSender().hasPermission("hardcourse.staff"))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(WinnerTP::onlinePlayerSuggestions)
                        .executes(ctx -> {
                            OfflinePlayer target = Bukkit.getOfflinePlayer(StringArgumentType.getString(ctx, "player"));
                            CommandSender sender = ctx.getSource().getSender();
                            if(!target.hasPlayedBefore() && !target.isOnline()){
                                ctx.getSource().getSender().sendMessage(Colorize("<prefix><error>Player not found!"));
                                return 1;
                            }
                            if(!checkpointDatabase.isLinked(target.getUniqueId())){
                                sender.sendMessage(Colorize("<prefix><error>Player is not linked!"));
                                return Command.SINGLE_SUCCESS;
                            }
                            if (guild != null) {
                                if (linkedRole != null) {
                                    String discordId = checkpointDatabase.getDiscordId(target.getUniqueId());
                                    if (discordId != null) {
                                        guild.removeRoleFromMember(UserSnowflake.fromId(discordId), linkedRole).queue();
                                        final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
                                        f.setTimeZone(TimeZone.getTimeZone("UTC"));
                                        linksChannel.sendMessage("`[" + f.format(new Date()) + "] " + target.getName() + " unlinked from` <@" + discordId + "> `(forced by " + ctx.getSource().getSender().getName() + ")`").queue();
                                    }
                                }
                            }
                            checkpointDatabase.unlinkDiscord(target.getUniqueId());
                            linkManager.clearCode(target.getUniqueId());
                            ctx.getSource().getSender().sendMessage(Colorize("<prefix>Unlinked <accent>" + target.getName() + "'s <main>account from Discord."));
                            return Command.SINGLE_SUCCESS;
                    })).build();
    }
}
