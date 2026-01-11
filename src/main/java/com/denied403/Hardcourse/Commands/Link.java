package com.denied403.Hardcourse.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.denied403.Hardcourse.Hardcourse.*;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class Link {
    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName){
        return Commands.literal(commandName)
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    if(!(sender instanceof Player player)) return 0;
                    if(checkpointDatabase.isLinked(player.getUniqueId())) {
                        player.sendMessage(Colorize("<prefix>You're already linked! Use <accent>/unlink<main> if you want to unlink your account."));
                        return 1;
                    }
                    if(!DiscordEnabled){
                        player.sendMessage(Colorize("<prefix>Discord functionality is currently disabled. Please try again later."));
                        return 1;
                    }
                    String code = linkManager.createLinkCode(player.getUniqueId());
                    player.sendMessage(Colorize("<prefix>Your link code is: <accent><click:copy_to_clipboard:'" + code + "'><accent>" + code + "<other> (click to copy)<reset>\nUse the <accent>/link<main> command on Discord to link your account."));
                    return Command.SINGLE_SUCCESS;
                }).build();
    }
}
