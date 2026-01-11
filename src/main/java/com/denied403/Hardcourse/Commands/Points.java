package com.denied403.Hardcourse.Commands;

import com.denied403.Hardcourse.Hardcourse;
import com.denied403.Hardcourse.Points.PointsManager;
import com.denied403.Hardcourse.Utils.CheckpointDatabase;
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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.denied403.Hardcourse.Hardcourse.checkpointDatabase;
import static com.denied403.Hardcourse.Hardcourse.plugin;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class Points {
    public static LiteralCommandNode<CommandSourceStack> createCommand(PointsManager pointsManager, String commandName) {
        return Commands.literal(commandName)
                .requires(source -> source.getSender().hasPermission("hardcourse.points.manage"))

                .then(Commands.literal("set")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Points::onlinePlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> handlePoints(ctx, pointsManager, "set"))
                                )
                        )
                )

                .then(Commands.literal("give")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Points::onlinePlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> handlePoints(ctx, pointsManager, "give"))
                                )
                        )
                )

                .then(Commands.literal("remove")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Points::onlinePlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> handlePoints(ctx, pointsManager, "remove"))
                                )
                        )
                )

                .then(Commands.literal("get")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Points::onlinePlayerSuggestions)
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    String playerName = StringArgumentType.getString(ctx, "player");
                                    OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

                                    if (!target.hasPlayedBefore()) {
                                        sender.sendMessage(Colorize("<prefix>Player <accent>" + playerName + "<main> not found."));
                                        return 0;
                                    }

                                    int currentPoints = PointsManager.getPoints(target.getUniqueId());

                                    if (sender.equals(target)) {
                                        sender.sendMessage(Colorize("<prefix>You have <accent>" + currentPoints + "<main> points."));
                                    } else {
                                        sender.sendMessage(Colorize("<prefix><accent>" + playerName + "<main> has <accent>" + currentPoints + "&r points."));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("leaderboard")
                        .executes(ctx -> executeLeaderboard(plugin, ctx.getSource().getSender(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    int page = IntegerArgumentType.getInteger(ctx, "page");
                                    return executeLeaderboard(plugin, ctx.getSource().getSender(), page);
                                })
                        )
                )
                .build();
    }

    private static int handlePoints(CommandContext<CommandSourceStack> ctx, PointsManager pointsManager, String action) {
        CommandSender sender = ctx.getSource().getSender();
        String targetName = StringArgumentType.getString(ctx, "player");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(Colorize("<prefix>Player <accent>" + targetName + "<main> not found."));
            return 0;
        }

        UUID uuid = target.getUniqueId();

        switch (action) {
            case "set" -> {
                pointsManager.setPoints(uuid, amount);
                sender.sendMessage(Colorize("<prefix>Set <accent>" + targetName + "<main>'s points to <accent>" + amount + "<main>."));
                if(target.isOnline() && sender != target) {
                    Player onlineTarget = target.getPlayer();
                    onlineTarget.sendMessage(Colorize("<prefix>Your points have been set to <accent>" + amount + "<main> by <accent>" + sender.getName() + "<main>."));
                }
            }
            case "give" -> {
                pointsManager.addPoints(uuid, amount);
                sender.sendMessage(Colorize("<prefix>Gave <accent>" + amount + "<main> points to <accent>" + targetName + "<main>."));
                if(target.isOnline() && sender != target) {
                    Player onlineTarget = target.getPlayer();
                    onlineTarget.sendMessage(Colorize("<prefix>You received <accent>" + amount + "<main> points from <accent>" + sender.getName() + "<main>."));
                }
            }
            case "remove" -> {
                pointsManager.removePoints(uuid, amount);
                sender.sendMessage(Colorize("<accent>Removed <accent>" + amount + "<main> points from <accent>" + targetName + "<main>."));
                if(target.isOnline() && sender != target) {
                    Player onlineTarget = target.getPlayer();
                    onlineTarget.sendMessage(Colorize("<prefix><accent>" + amount + "<main> points were removed by <accent>" + sender.getName() + "<main>."));
                }
            }
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


    private static int executeLeaderboard(Hardcourse plugin, CommandSender sender, int page) {
        List<CheckpointDatabase.CheckpointData> all = checkpointDatabase.getAllSortedByPoints();

        if (page == 1) {
            sender.sendMessage(Colorize("<prefix>Sorting <accent>" + all.size() + "<main> players..."));
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> sendLeaderboard(sender, page, all));
            return 0;
        }
        return sendLeaderboard(sender, page, all);
    }

    private static int sendLeaderboard(CommandSender sender, int page, List<CheckpointDatabase.CheckpointData> all) {
        List<CheckpointDatabase.CheckpointData> filtered = all.stream().filter(data -> {
            OfflinePlayer p = Bukkit.getOfflinePlayer(data.uuid());
            return !p.isOp() && !(checkpointDatabase.getPoints(p.getUniqueId()) == 0);
        }).toList();

        int totalEntries = filtered.size();
        int entriesPerPage = 10;
        int totalPages = (totalEntries + entriesPerPage - 1) / entriesPerPage;

        int start = (page - 1) * entriesPerPage;
        int end = Math.min(start + entriesPerPage, totalEntries);

        if (start >= totalEntries) {
            sender.sendMessage(Colorize("<prefix>No points entries on this page."));
            return start;
        }

        sender.sendMessage(Colorize("<prefix>Points Leaderboard <accent>(Page " + page + " of " + totalPages + ")"));

        for (int i = start; i < end; i++) {
            var entry = filtered.get(i);
            String name = Optional.ofNullable(Bukkit.getOfflinePlayer(entry.uuid()).getName()).orElse("Unknown");

            sender.sendMessage(Colorize("&c#" + (i + 1) + ". &f" + name + ": &c" + entry.points()));
        }

        String nav = "";
        if (page > 1) {
            nav += "<click:run_command:/points leaderboard " + (page - 1) + "><accent>[← Previous]</click> ";
        }
        if (page < totalPages) {
            nav += "<click:run_command:/points leaderboard " + (page + 1) + "><accent>[Next →]</click>";
        }
        if (!nav.isEmpty()) {
            sender.sendMessage(Colorize(nav));
        }
        return start;
    }
}
