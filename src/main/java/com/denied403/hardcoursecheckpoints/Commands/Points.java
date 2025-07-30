package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import com.denied403.hardcoursecheckpoints.Points.PointsManager;
import com.denied403.hardcoursecheckpoints.Utils.CheckpointDatabase;
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

import static com.denied403.hardcoursecheckpoints.Utils.ColorUtil.Colorize;

public class Points {
    private static CheckpointDatabase database;
    public static void initialize(CheckpointDatabase db) {
        database = db;
    }

    public static LiteralCommandNode<CommandSourceStack> createCommand(HardcourseCheckpoints plugin, PointsManager pointsManager, String commandName) {
        return Commands.literal(commandName)
                .requires(source -> source.getSender().hasPermission("hardcourse.points.manage"))

                // /points set <player> <amount>
                .then(Commands.literal("set")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Points::onlinePlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> handlePoints(ctx, pointsManager, "set"))
                                )
                        )
                )

                // /points give <player> <amount>
                .then(Commands.literal("give")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Points::onlinePlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> handlePoints(ctx, pointsManager, "give"))
                                )
                        )
                )

                // /points remove <player> <amount>
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Points::onlinePlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> handlePoints(ctx, pointsManager, "remove"))
                                )
                        )
                )

                // /points view <player>
                .then(Commands.literal("view")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Points::onlinePlayerSuggestions)
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    String playerName = StringArgumentType.getString(ctx, "player");
                                    Player target = Bukkit.getPlayerExact(playerName);

                                    if (target == null) {
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rPlayer '&c" + playerName + "&r' not found or not online."));
                                        return 0;
                                    }

                                    int currentPoints = PointsManager.getPoints(target.getUniqueId());

                                    if (sender.equals(target)) {
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rYou have &c" + currentPoints + "&r points."));
                                    } else {
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &r" + playerName + " has &c" + currentPoints + "&r points."));
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
            sender.sendMessage(Colorize("&c&lHARDCOURSE &rPlayer '&c" + targetName + "&r' not found or not online."));
            return 0;
        }

        UUID uuid = target.getUniqueId();

        switch (action) {
            case "set" -> {
                pointsManager.setPoints(uuid, amount);
                sender.sendMessage(Colorize("&c&lHARDCOURSE &rSet &c" + targetName + "&r's points to &c" + amount + "&r."));
                if(target.isOnline() && sender != target) {
                    Player onlineTarget = target.getPlayer();
                    onlineTarget.sendMessage(Colorize("&c&lHARDCOURSE &rYour points have been set to &c" + amount + "&r by &c" + sender.getName() + "&r."));
                }
            }
            case "give" -> {
                pointsManager.addPoints(uuid, amount);
                sender.sendMessage(Colorize("&c&lHARDCOURSE &rGave &c" + amount + "&r points to &c" + targetName + "&r."));
                if(target.isOnline() && sender != target) {
                    Player onlineTarget = target.getPlayer();
                    onlineTarget.sendMessage(Colorize("&c&lHARDCOURSE &rYou received &c" + amount + "&r points from &c" + sender.getName() + "&r."));
                }
            }
            case "remove" -> {
                pointsManager.removePoints(uuid, amount);
                sender.sendMessage(Colorize("&c&lHARDCOURSE &rRemoved &c" + amount + "&r points from &c" + targetName + "&r."));
                if(target.isOnline() && sender != target) {
                    Player onlineTarget = target.getPlayer();
                    onlineTarget.sendMessage(Colorize("&c&lHARDCOURSE &r&c" + amount + "&r points were removed by &c" + sender.getName() + "&r."));
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


    private static int executeLeaderboard(HardcourseCheckpoints plugin, CommandSender sender, int page) {
        List<CheckpointDatabase.CheckpointData> all = database.getAllSortedByPoints();

        if (page == 1) {
            sender.sendMessage(Colorize("&c&lHARDCOURSE &rSorting &c" + all.size() + "&f players..."));
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> sendLeaderboard(sender, page, all));
            return 0;
        }
        return sendLeaderboard(sender, page, all);
    }

    private static int sendLeaderboard(CommandSender sender, int page, List<CheckpointDatabase.CheckpointData> all) {
        List<CheckpointDatabase.CheckpointData> filtered = all.stream().filter(data -> {
            OfflinePlayer p = Bukkit.getOfflinePlayer(data.uuid());
            return !p.isOp() && !(database.getPoints(p.getUniqueId()) == 0);
        }).toList();

        int totalEntries = filtered.size();
        int entriesPerPage = 10;
        int totalPages = (totalEntries + entriesPerPage - 1) / entriesPerPage;

        int start = (page - 1) * entriesPerPage;
        int end = Math.min(start + entriesPerPage, totalEntries);

        if (start >= totalEntries) {
            sender.sendMessage(Colorize("&c&lHARDCOURSE &rNo points entries on this page."));
            return start;
        }

        sender.sendMessage(Colorize("&c&lHARDCOURSE&r Points Leaderboard &c(Page " + page + " of " + totalPages + ")"));

        for (int i = start; i < end; i++) {
            var entry = filtered.get(i);
            String name = Optional.ofNullable(Bukkit.getOfflinePlayer(entry.uuid()).getName()).orElse("Unknown");

            sender.sendMessage(Colorize("&c#" + (i + 1) + ". &f" + name + ": &c" + entry.points()));
        }

        String nav = "";
        if (page > 1) {
            nav += "<click:run_command:/points leaderboard " + (page - 1) + "><red>[← Previous]</red></click> ";
        }
        if (page < totalPages) {
            nav += "<click:run_command:/points leaderboard " + (page + 1) + "><red>[Next →]</red></click>";
        }
        if (!nav.isEmpty()) {
            sender.sendMessage(Colorize(nav));
        }
        return start;
    }
}
