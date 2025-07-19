package com.denied403.hardcoursecheckpoints.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.denied403.hardcoursecheckpoints.Chat.ChatReactions.gameActive;
import static com.denied403.hardcoursecheckpoints.Chat.ChatReactions.getCurrentWord;
import static com.denied403.hardcoursecheckpoints.Utils.ColorUtil.Colorize;

public class EndChatGame {
    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .requires(source -> {
                    CommandSender sender = source.getSender();
                    return !(sender instanceof Player player) || player.isOp(); // Allow console and OP players
                })
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();

                    if (gameActive) {
                        gameActive = false;
                        Bukkit.broadcast(Colorize("&c&lHARDCOURSE &rThe chat game has been ended early. The word was &c" + getCurrentWord()));
                    } else {
                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rThere is no game running currently."));
                    }

                    return Command.SINGLE_SUCCESS;
                }).build();
    }
}
