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
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
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
import static com.denied403.Hardcourse.Discord.HardcourseDiscord.jda;
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

                                    if (target == null || !target.hasPlayedBefore()) {
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rPlayer &c" + playerName + "&r not found."));
                                        return 0;
                                    }

                                    int currentDeaths = target.getStatistic(Statistic.DEATHS);

                                    if (sender.equals(target)) {
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rYou have &c" + currentDeaths + "&r deaths."));
                                    } else {
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &r&c" + playerName + " &rhas &c" + currentDeaths + "&r deaths."));
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
            sender.sendMessage(Colorize("&c&lHARDCOURSE &rPlayer '&c" + targetName + "&r' not found or not online."));
            return 0;
        }

        int currentDeaths = target.getStatistic(Statistic.DEATHS);

        switch (action) {
            case "set" -> {
                target.setStatistic(Statistic.DEATHS, amount);
                sender.sendMessage(Colorize("&c&lHARDCOURSE &rSet &c" + targetName + "&r's deaths to &c" + amount + "&r."));
                if(target.isOnline() && sender != target) {
                    Player onlineTarget = target.getPlayer();
                    onlineTarget.sendMessage(Colorize("&c&lHARDCOURSE &rYour deaths have been set to &c" + amount + "&r by &c" + sender.getName() + "&r."));
                }
            }
            case "give" -> {
                target.setStatistic(Statistic.DEATHS, currentDeaths + amount);
                sender.sendMessage(Colorize("&c&lHARDCOURSE &rGave &c" + amount + "&r deaths to &c" + targetName + "&r."));
                if(target.isOnline() && sender != target) {
                    Player onlineTarget = target.getPlayer();
                    onlineTarget.sendMessage(Colorize("&c&lHARDCOURSE &rYou received &c" + amount + "&r deaths from &c" + sender.getName() + "&r."));
                }
            }
            case "remove" -> {
                target.setStatistic(Statistic.DEATHS, currentDeaths - amount);
                sender.sendMessage(Colorize("&c&lHARDCOURSE &rRemoved &c" + amount + "&r deaths from &c" + targetName + "&r."));
                if(target.isOnline() && sender != target) {
                    Player onlineTarget = target.getPlayer();
                    onlineTarget.sendMessage(Colorize("&c&lHARDCOURSE &r&c" + amount + "&r deaths were removed by &c" + sender.getName() + "&r."));
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
