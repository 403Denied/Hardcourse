package com.denied403.Hardcourse.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

import static com.denied403.Hardcourse.Hardcourse.*;
import static com.denied403.Hardcourse.Utils.CheckpointLevelTimer.resetForNewLevel;
import static com.denied403.core403.Util.ColorUtil.Colorize;

public class Skip {

    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(Colorize("<prefix>This command can only be run by a player!"));
                        return Command.SINGLE_SUCCESS;
                    }
                    return handleUseSkip(player);
                })
                .then(Commands.literal("grant")
                        .requires(source -> source.getSender().hasPermission("hardcourse.skip.manage"))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Skip::playerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            CommandSender sender = ctx.getSource().getSender();
                                            String playerName = StringArgumentType.getString(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
                                            if (!target.hasPlayedBefore() && !target.isOnline()) {
                                                sender.sendMessage(Colorize("<prefix>Player <accent>" + playerName + "<main> not found."));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            int current = checkpointDatabase.getSkips(target.getUniqueId());
                                            checkpointDatabase.setSkips(target.getUniqueId(), current + amount);

                                            sender.sendMessage(Colorize("<prefix>Granted <accent>" + amount + "<main> skip" + (amount != 1 ? "s" : "")  + " to <accent>" + playerName + "<main>. They now have <accent>" + (current + amount) + "<main>."));
                                            if (target.isOnline()) {
                                                ((Player) target).sendMessage(Colorize("<prefix>You have been granted <accent>" + amount + "<main> skip" + (amount != 1 ? "s" : "") + ". You now have <accent>" + (current + amount) + "<main>."));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .requires(source -> source.getSender().hasPermission("hardcourse.skip.manage"))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Skip::playerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            CommandSender sender = ctx.getSource().getSender();
                                            String playerName = StringArgumentType.getString(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
                                            if (!target.hasPlayedBefore() && !target.isOnline()) {
                                                sender.sendMessage(Colorize("<prefix>Player <accent>" + playerName + "<main> not found."));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            int current = checkpointDatabase.getSkips(target.getUniqueId());
                                            int newAmount = Math.max(0, current - amount);
                                            checkpointDatabase.setSkips(target.getUniqueId(), newAmount);

                                            sender.sendMessage(Colorize("<prefix>Removed <accent>" + (current - newAmount) + "<main> skip" + (amount != 1 ? "s" : "") + " from <accent>" + playerName + "<main>. They now have <accent>" + newAmount + "<main>."));
                                            if (target.isOnline()) {
                                                ((Player) target).sendMessage(Colorize("<prefix><accent>" + (current - newAmount) + "<main> skip" + (amount != 1 ? "s" : "") + (amount != 1 ? " were " : " was ") + "removed. You now have <accent>" + newAmount + "<main>."));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("set")
                        .requires(source -> source.getSender().hasPermission("hardcourse.skip.manage"))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Skip::playerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            CommandSender sender = ctx.getSource().getSender();
                                            String playerName = StringArgumentType.getString(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
                                            if (!target.hasPlayedBefore() && !target.isOnline()) {
                                                sender.sendMessage(Colorize("<prefix>Player <accent>" + playerName + "<main> not found."));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            checkpointDatabase.setSkips(target.getUniqueId(), amount);

                                            sender.sendMessage(Colorize("<prefix><accent>" + playerName + "<main> now has <accent>" + amount + "<main> skip" + (amount != 1 ? "s" : "") + "."));
                                            if (target.isOnline()) {
                                                ((Player) target).sendMessage(Colorize("<prefix>Your skip count has been set to <accent>" + amount + "<main>."));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("get")
                        .requires(source -> source.getSender().hasPermission("hardcourse.skip.manage"))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Skip::playerSuggestions)
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    String playerName = StringArgumentType.getString(ctx, "player");

                                    OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
                                    if (!target.hasPlayedBefore() && !target.isOnline()) {
                                        sender.sendMessage(Colorize("<prefix>Player <accent>" + playerName + "<main> not found."));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    int skips = checkpointDatabase.getSkips(target.getUniqueId());
                                    sender.sendMessage(Colorize("<prefix><accent>" + playerName + "<main> has <accent>" + skips + "<main> skip" + (skips != 1 ? "s" : "") + "."));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .build();
    }

    private static int handleUseSkip(Player player) {
        int skips = checkpointDatabase.getSkips(player.getUniqueId());
        if (skips <= 0) {
            player.sendMessage(Colorize("<prefix>You have no skips available."));
            return 0;
        }

        int season = checkpointDatabase.getSeason(player.getUniqueId());
        double currentLevel = checkpointDatabase.getLevel(player.getUniqueId());

        double nextLevel;
        Location nextLoc = checkpointDatabase.getCheckpointLocation(season, currentLevel + 0.5);
        if (nextLoc != null) {
            nextLevel = currentLevel + 0.5;
        } else {
            nextLevel = currentLevel + 1;
            nextLoc = checkpointDatabase.getCheckpointLocation(season, nextLevel);
        }

        if (nextLoc == null) {
            player.sendMessage(Colorize("<prefix>No checkpoint location found for the next level (<accent>" + season + "-" + String.valueOf(nextLevel).replace(".0", "") + "<main>). Contact a staff member."));
            return 1;
        }

        checkpointDatabase.setSkips(player.getUniqueId(), skips - 1);
        checkpointDatabase.setCheckpointData(player.getUniqueId(), season, nextLevel);
        resetForNewLevel(player.getUniqueId());
        player.teleport(nextLoc);
        player.setRespawnLocation(nextLoc.clone().add(0, 1, 0), true);

        player.sendMessage(Colorize("<prefix>Skipped to level <accent>" + season + "-" + String.valueOf(nextLevel).replace(".0", "") + "<main>! You have <accent>" + (skips - 1) + "<main> skip" + ((skips - 1) != 1 ? "s" : "") + " remaining."));
        return 1;
    }

    private static CompletableFuture<Suggestions> playerSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String name = player.getName();
            if (name.toLowerCase().startsWith(input)) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }
}