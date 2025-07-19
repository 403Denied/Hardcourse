package com.denied403.hardcoursecheckpoints.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.denied403.hardcoursecheckpoints.Chat.ChatReactions.gameActive;
import static com.denied403.hardcoursecheckpoints.Chat.ChatReactions.runGame;
import static com.denied403.hardcoursecheckpoints.Utils.Colorize.Colorize;

public class RunChatGame {
    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .requires(source -> {
                    CommandSender sender = source.getSender();
                    return !(sender instanceof Player player) || player.isOp(); // OP or console
                })
                .then(Commands.argument("phrase", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            String phrase = StringArgumentType.getString(ctx, "phrase").trim();

                            if (phrase.length() < 2) {
                                sender.sendMessage(Colorize("&c&lHARDCOURSE &rThat string is too short. Please try again with a longer term."));
                                return Command.SINGLE_SUCCESS;
                            }

                            if (!gameActive) {
                                runGame(phrase);
                            } else {
                                sender.sendMessage(Colorize("&c&lHARDCOURSE &rA game is currently running. Please wait."));
                            }

                            return Command.SINGLE_SUCCESS;
                        }))
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage(Colorize("&c&lHARDCOURSE &rPlease enter a phrase to start the game."));
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
