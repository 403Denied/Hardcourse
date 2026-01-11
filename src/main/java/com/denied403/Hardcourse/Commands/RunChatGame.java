package com.denied403.Hardcourse.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.denied403.Hardcourse.Chat.ChatReactions.gameActive;
import static com.denied403.Hardcourse.Chat.ChatReactions.runGame;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class RunChatGame {
    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .requires(source -> {
                    CommandSender sender = source.getSender();
                    return !(sender instanceof Player player) || player.isOp();
                })
                .then(Commands.argument("phrase", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            String phrase = StringArgumentType.getString(ctx, "phrase").trim();

                            if (phrase.length() < 2) {
                                sender.sendMessage(Colorize("<prefix>That string is too short. Please try again with a longer term."));
                                return Command.SINGLE_SUCCESS;
                            }

                            if (!gameActive) {
                                runGame(phrase);
                            } else {
                                sender.sendMessage(Colorize("<prefix>A game is currently running. Please wait or click <accent><click:run_command:'/ecg'>here<reset><main> to end it."));
                            }

                            return Command.SINGLE_SUCCESS;
                        }))
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage(Colorize("<prefix>Please enter a phrase to start the game."));
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
