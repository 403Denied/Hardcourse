package com.denied403.Hardcourse.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.denied403.Hardcourse.Chat.ChatReactions.gameActive;
import static com.denied403.Hardcourse.Chat.ChatReactions.getCurrentWord;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class EndChatGame {
    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .requires(source -> {
                    CommandSender sender = source.getSender();
                    return !(sender instanceof Player player) || player.isOp();
                })
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();

                    if (gameActive) {
                        gameActive = false;
                        Bukkit.broadcast(Colorize("<prefix>The chat game has been ended early. The word was <accent>" + getCurrentWord()));
                    } else {
                        sender.sendMessage(Colorize("<prefix>There is no game running currently."));
                    }

                    return Command.SINGLE_SUCCESS;
                }).build();
    }
}
