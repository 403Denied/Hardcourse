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
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.deathsChannel;
import static com.denied403.Hardcourse.Hardcourse.DiscordEnabled;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class Deaths {
    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .requires(source -> source.getSender().hasPermission("hardcourse.deaths.manage"))

                .then(Commands.literal("set")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Deaths::onlinePlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> handleDeaths(ctx, "set"))
                                )
                        )
                )

                .then(Commands.literal("give")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Deaths::onlinePlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> handleDeaths(ctx, "give"))
                                )
                        )
                )

                .then(Commands.literal("remove")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Deaths::onlinePlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> handleDeaths(ctx, "remove"))
                                )
                        )
                )

                .then(Commands.literal("get")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Deaths::onlinePlayerSuggestions)
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    String playerName = StringArgumentType.getString(ctx, "player");
                                    OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

                                    if (!target.hasPlayedBefore()) {
                                        sender.sendMessage(Colorize("<prefix>Player <accent>" + playerName + "<main> not found."));
                                        return 0;
                                    }

                                    int currentDeaths = target.getStatistic(Statistic.DEATHS);

                                    if (sender.equals(target)) {
                                        sender.sendMessage(Colorize("<prefix>You have <accent>" + currentDeaths + "<main> deaths."));
                                    } else {
                                        sender.sendMessage(Colorize("<prefix><accent>" + playerName + " <main>has <accent>" + currentDeaths + "<main> deaths."));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .build();
    }

    private static int handleDeaths(CommandContext<CommandSourceStack> ctx, String action) {
        CommandSender sender = ctx.getSource().getSender();
        String targetName = StringArgumentType.getString(ctx, "player");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(Colorize("<prefix>Player <accent>" + targetName + "<main> not found."));
            return 0;
        }

        int currentDeaths = target.getStatistic(Statistic.DEATHS);

        switch (action) {
            case "set" -> {
                target.setStatistic(Statistic.DEATHS, amount);
                sender.sendMessage(Colorize("<prefix>Set <accent>" + targetName + "<main>'s deaths to <accent>" + amount + "<main>."));
                if(target.isOnline() && sender != target) {
                    Player onlineTarget = target.getPlayer();
                    onlineTarget.sendMessage(Colorize("<prefix>Your deaths have been set to <accent>" + amount + "<main> by <accent>" + sender.getName() + "<main>."));
                }
            }
            case "give" -> {
                target.setStatistic(Statistic.DEATHS, currentDeaths + amount);
                sender.sendMessage(Colorize("<prefix>Gave <accent>" + amount + "<main> deaths to <accent>" + targetName + "<main>."));
                if(target.isOnline() && sender != target) {
                    Player onlineTarget = target.getPlayer();
                    onlineTarget.sendMessage(Colorize("<prefix>You received <accent>" + amount + "<main> deaths from <accent>" + sender.getName() + "<main>."));
                }
            }
            case "remove" -> {
                target.setStatistic(Statistic.DEATHS, currentDeaths - amount);
                sender.sendMessage(Colorize("<prefix>Removed <accent>" + amount + "<main> deaths from <accent>" + targetName + "<main>."));
                if(target.isOnline() && sender != target) {
                    Player onlineTarget = target.getPlayer();
                    onlineTarget.sendMessage(Colorize("<prefix><accent>" + amount + "<main> deaths were removed by <accent>" + sender.getName() + "<main>."));
                }
            }
        }
        if(DiscordEnabled){
            final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            deathsChannel.sendMessage("`[" + f.format(new Date()) + "] " + target.getName() + " had their deaths set to " + target.getStatistic(Statistic.DEATHS) + " by " + sender.getName() + "`").queue();
        }

        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> onlinePlayerSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
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
