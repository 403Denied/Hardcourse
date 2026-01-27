package com.denied403.Hardcourse.Commands;

import com.denied403.Hardcourse.Hardcourse;
import com.denied403.Hardcourse.Utils.CheckpointDatabase;
import com.denied403.Hardcourse.Utils.Luckperms;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
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
import org.bukkit.Location;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.denied403.Hardcourse.Commands.Clock.giveItems;
import static com.denied403.Hardcourse.Discord.HardcourseDiscord.checkpointsChannel;
import static com.denied403.Hardcourse.Hardcourse.*;
import static com.denied403.Hardcourse.Utils.CheckpointLevelTimer.resetForNewLevel;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class CheckpointCommand {
    private static final Set<UUID> restartCancelled = new HashSet<>();

    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .then(Commands.literal("set")
                        .requires(source -> source.getSender().isOp())
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(CheckpointCommand::playerSuggestions)
                                .then(Commands.argument("level", DoubleArgumentType.doubleArg(0))
                                        .executes(ctx -> {
                                            CommandSender sender = ctx.getSource().getSender();
                                            String playerName = StringArgumentType.getString(ctx, "player");
                                            double level = DoubleArgumentType.getDouble(ctx, "level");

                                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                                            UUID uuid = offlinePlayer.getUniqueId();

                                            resetForNewLevel(uuid);
                                            checkpointDatabase.setLevel(uuid, level);

                                            int season = checkpointDatabase.getSeason(uuid);
                                            String formattedLevel = (level % 1 == 0) ? String.valueOf((int) level) : String.valueOf(level);

                                            sender.sendMessage(Colorize("<prefix>The level of <accent>" + playerName + "<main> has been set to <accent>" + season + "-" + formattedLevel + "<main>!"));


                                            if (offlinePlayer.isOnline() && offlinePlayer != sender) {
                                                ((Player) offlinePlayer).sendMessage(Colorize("<prefix>Your level has been set to <accent>" + season + "-" + formattedLevel + "<main>!"));
                                            }
                                            if(DiscordEnabled) {
                                                final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
                                                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                                                checkpointsChannel.sendMessage("`[" + f.format(new Date()) + "] " + playerName + " was set to level " + season + "-" + String.valueOf(level).replace(".0", "") + " by " + (sender instanceof Player ? sender.getName() + "`" : "CONSOLE`")).queue();
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(Commands.argument("season", IntegerArgumentType.integer(1, 3))
                                                .executes(ctx -> {
                                                    CommandSender sender = ctx.getSource().getSender();
                                                    String playerName = StringArgumentType.getString(ctx, "player");
                                                    double level = DoubleArgumentType.getDouble(ctx, "level");
                                                    int season = IntegerArgumentType.getInteger(ctx, "season");

                                                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                                                    UUID uuid = offlinePlayer.getUniqueId();

                                                    checkpointDatabase.setSeason(uuid, season);
                                                    checkpointDatabase.setLevel(uuid, level);
                                                    resetForNewLevel(uuid);

                                                    String formattedLevel = (level % 1 == 0) ? String.valueOf((int) level) : String.valueOf(level);

                                                    sender.sendMessage(Colorize("<prefix>The level of <accent>" + playerName + "<main> has been set to <accent>" + season + "-" + formattedLevel + "<main>!"));

                                                    if (offlinePlayer.isOnline() && offlinePlayer != sender) {
                                                        ((Player) offlinePlayer).sendMessage(Colorize("<prefix>Your level has been set to <accent>" + season + "-" + formattedLevel + "<main>!"));
                                                    }
                                                    if(DiscordEnabled) {
                                                        final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
                                                        f.setTimeZone(TimeZone.getTimeZone("UTC"));
                                                        checkpointsChannel.sendMessage("`[" + f.format(new Date()) + "] " + playerName + " was set to level " + season + "-" + String.valueOf(level).replace(".0", "") + " by " + (sender instanceof Player ? sender.getName() + "`" : "CONSOLE`")).queue();
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                )
                .then(Commands.literal("reset")
                        .requires(source -> source.getSender().isOp())
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(CheckpointCommand::playerSuggestions)
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    String playerName = StringArgumentType.getString(ctx, "player");

                                    sender.sendMessage(Colorize("<prefix>This will reset <accent>" + playerName + "<main>'s checkpoint data."));
                                    sender.sendMessage(Colorize("<main>Run <accent>/checkpoint reset " + playerName + " confirm <main>to confirm."));
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(Commands.literal("confirm")
                                        .executes(ctx -> {
                                            CommandSender sender = ctx.getSource().getSender();
                                            String playerName = StringArgumentType.getString(ctx, "player");
                                            OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
                                            UUID uuid = player.getUniqueId();

                                            checkpointDatabase.setCheckpointData(uuid, 1, 0, 0);
                                            sender.sendMessage(Colorize("<prefix>Checkpoint for <accent>" + playerName + " <main>has been reset."));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )

                .then(Commands.literal("resetAll")
                        .requires(source -> source.getSender().isOp())
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            sender.sendMessage(Colorize("<prefix>This will erase &4ALL<main> checkpoint data."));
                            sender.sendMessage(Colorize("<main>Run <accent>/checkpoint resetAll confirm <main>to confirm."));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.literal("confirm")
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    checkpointDatabase.deleteAll();
                                    sender.sendMessage(Colorize("<prefix>All checkpoint data has been wiped."));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("get")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(CheckpointCommand::playerSuggestions)
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    String playerName = StringArgumentType.getString(ctx, "player");

                                    Player onlineTarget = Bukkit.getPlayerExact(playerName);
                                    OfflinePlayer p = (onlineTarget != null) ? onlineTarget : Bukkit.getOfflinePlayer(playerName);

                                    if (p.getName() == null) {
                                        sender.sendMessage(Colorize("<prefix>Player not found or has never played before!"));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    UUID uuid = p.getUniqueId();
                                    Integer season = checkpointDatabase.getSeason(uuid);
                                    Double level = checkpointDatabase.getLevel(uuid);

                                    if (season == null || level == null || level <= 0) {
                                        sender.sendMessage(Colorize("<prefix>Player not found or has no checkpoints!"));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    String levelString = season + "-" + level.toString().replace(".0", "");

                                    sender.sendMessage(Colorize("<prefix><accent>" + playerName + "<main>'s level is: <accent>" + levelString));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("leaderboard")
                        .executes(ctx -> executeLeaderboard(plugin, ctx.getSource().getSender(), 1)) // default page
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    int page = IntegerArgumentType.getInteger(ctx, "page");
                                    return executeLeaderboard(plugin, ctx.getSource().getSender(), page);
                                })
                        )
                )
                .then(Commands.literal("restart")
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            if (!(sender instanceof Player player)) {
                                sender.sendMessage(Colorize("<prefix>This command can only be run by a player!"));
                                return Command.SINGLE_SUCCESS;
                            }
                            if(isDev) {
                                player.sendMessage(Colorize("<prefix>Are you sure you want to restart? You will be reset at the beginning. Run <accent>/checkpoint restart confirm <main>to confirm. This &4cannot<main> be undone. This will also remove points and remove your rank."));
                            } else {
                                player.sendMessage(Colorize("<prefix>Are you sure you want to restart? You will be reset at the beginning. Run <accent>/checkpoint restart confirm <main>to confirm. This &4cannot<main> be undone. This will also reset your rank."));
                            }
                            return Command.SINGLE_SUCCESS;
                        }).then(Commands.literal("confirm").executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            if (!(sender instanceof Player player)) {
                                sender.sendMessage(Colorize("<prefix>This command can only be run by a player!"));
                                return Command.SINGLE_SUCCESS;
                            }
                            player.sendMessage(Colorize("<click:run_command:'checkpoint restart cancel'><prefix>Your checkpoint is about to be reset in <accent>10 seconds<main>. You may cancel by typing <accent>/checkpoint restart cancel<main>, or by clicking this message."));
                            UUID uuid = player.getUniqueId();
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                if (!restartCancelled.contains(player.getUniqueId())) {
                                    checkpointDatabase.setCheckpointData(uuid, 1, 0, 0);
                                    player.performCommand("spawn");
                                    giveItems(player);
                                    player.setRespawnLocation(player.getWorld().getSpawnLocation());
                                    player.sendMessage(Colorize("<prefix>You have been reset to the beginning."));
                                    Luckperms.removeRank(player.getUniqueId());
                                    player.setStatistic(Statistic.DEATHS, 0);
                                    if(DiscordEnabled) {
                                        final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
                                        f.setTimeZone(TimeZone.getTimeZone("UTC"));
                                        checkpointsChannel.sendMessage("`[" + f.format(new Date()) + "] " + player.getName() + " reset back to level 0!`").queue();
                                    }
                                }
                                restartCancelled.remove(player.getUniqueId());
                            }, 200L);
                            return Command.SINGLE_SUCCESS;
                        })).then(Commands.literal("cancel").executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            if (!(sender instanceof Player player)) {
                                sender.sendMessage(Colorize("<prefix>This command can only be run by a player!"));
                                return Command.SINGLE_SUCCESS;
                            }
                            player.sendMessage(Colorize("<prefix>Restart cancelled."));
                            restartCancelled.add(player.getUniqueId());
                            return Command.SINGLE_SUCCESS;
                        })))
                .then(Commands.literal("update")
                        .requires(source -> source.getSender().isOp())
                        .then(Commands.argument("season", IntegerArgumentType.integer(1, 3))
                                .then(Commands.argument("start", DoubleArgumentType.doubleArg(0))
                                        .then(Commands.argument("end", DoubleArgumentType.doubleArg(0))
                                                .then(Commands.argument("resetTo", DoubleArgumentType.doubleArg(0))
                                                        .executes(ctx -> {
                                                            CommandSender sender = ctx.getSource().getSender();

                                                            int season = IntegerArgumentType.getInteger(ctx, "season");
                                                            double start = DoubleArgumentType.getDouble(ctx, "start");
                                                            double end = DoubleArgumentType.getDouble(ctx, "end");
                                                            double resetTo = DoubleArgumentType.getDouble(ctx, "resetTo");

                                                            int createdId = checkpointUpdating.addLevelUpdate(season, start, end, resetTo);

                                                            if (createdId == -1) {
                                                                sender.sendMessage(Colorize("<prefix>Failed to create update."));
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            checkpointUpdating.applyUpdatesToAllOnlinePlayers();

                                                            sender.sendMessage(Colorize("<prefix>Update <accent>" + createdId +
                                                                    "<main> applied for Season <accent>" + season + "<main>, Levels <accent>" +
                                                                    String.valueOf(start).replace(".0", "") + "<main>-<accent>" +
                                                                    String.valueOf(end).replace(".0", "") +
                                                                    "<main>, reset to <accent>" + String.valueOf(resetTo).replace(".0", "")));

                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("info")
                        .requires(source -> source.getSender().isOp())
                        .then(Commands.argument("level", DoubleArgumentType.doubleArg(0))
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    double level = DoubleArgumentType.getDouble(ctx, "level");
                                    int season = 1;

                                    Location location = checkpointDatabase.getCheckpointLocation(season, level);
                                    if (location == null) {
                                        sender.sendMessage(Colorize("<prefix>This checkpoint has no data recorded. It may not have been entered into the database yet."));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    String locationString =
                                            "<main>World: <accent>" + location.getWorld().getName() +
                                                    "<main>, X: <accent>" + location.getX() +
                                                    "<main>, Y: <accent>" + location.getY() +
                                                    "<main>, Z: <accent>" + location.getZ();

                                    String difficulty = checkpointDatabase.getCheckpointDifficulty(season, level);

                                    sender.sendMessage(Colorize(
                                            "<prefix>Checkpoint Info for level <accent>1-" +
                                                    String.valueOf(level).replace(".0", "") +
                                                    "<main>: " + locationString +
                                                    "<main>\nDifficulty: <accent>" + difficulty.replaceAll("§", "&")
                                    ));
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(Commands.argument("season", IntegerArgumentType.integer(1, 3))
                                        .executes(ctx -> {
                                            CommandSender sender = ctx.getSource().getSender();
                                            double level = DoubleArgumentType.getDouble(ctx, "level");
                                            int season = IntegerArgumentType.getInteger(ctx, "season");

                                            Location location = checkpointDatabase.getCheckpointLocation(season, level);
                                            if (location == null) {
                                                sender.sendMessage(Colorize("<prefix>This checkpoint has no data recorded. It may not have been entered into the database yet."));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            String locationString =
                                                    "<main>World: <accent>" + location.getWorld().getName() +
                                                            "<main>, X: <accent>" + location.getX() +
                                                            "<main>, Y: <accent>" + location.getY() +
                                                            "<main>, Z: <accent>" + location.getZ();

                                            String difficulty = checkpointDatabase.getCheckpointDifficulty(season, level);

                                            sender.sendMessage(Colorize(
                                                    "<prefix>Checkpoint Info for level <accent>" +
                                                            season + "-" +
                                                            String.valueOf(level).replace(".0", "") +
                                                            "<main>: " + locationString +
                                                            "<main>\nDifficulty: <accent>" + difficulty.replaceAll("§", "&")
                                            ));
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal("removeData")
                        .requires(source -> source.getSender().isOp())
                        .then(Commands.argument("level", DoubleArgumentType.doubleArg(0))
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    double level = DoubleArgumentType.getDouble(ctx, "level");
                                    int season = 1;

                                    checkpointDatabase.removeCheckpointLocation(season, level);
                                    sender.sendMessage(Colorize("<prefix>Checkpoint data for <accent>1-" + String.valueOf(level).replace(".0", "") + "<main> has been removed from the database."));
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(Commands.argument("season", IntegerArgumentType.integer(1, 3))
                                        .executes(ctx -> {
                                            CommandSender sender = ctx.getSource().getSender();
                                            double level = DoubleArgumentType.getDouble(ctx, "level");
                                            int season = IntegerArgumentType.getInteger(ctx, "season");

                                            checkpointDatabase.removeCheckpointLocation(season, level);
                                            sender.sendMessage(Colorize("<prefix>Checkpoint data for <accent>" + season + "-" + String.valueOf(level).replace(".0", "") + "<main> has been removed from the database."));
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal("listPlayers")
                        .then(Commands.argument("level", DoubleArgumentType.doubleArg(0))
                                .executes(ctx -> executeListPlayers(
                                        ctx.getSource().getSender(),
                                        DoubleArgumentType.getDouble(ctx, "level"),
                                        1,
                                        1
                                ))
                                .then(Commands.argument("season", IntegerArgumentType.integer(1, 3))
                                        .executes(ctx -> executeListPlayers(
                                                ctx.getSource().getSender(),
                                                DoubleArgumentType.getDouble(ctx, "level"),
                                                IntegerArgumentType.getInteger(ctx, "season"),
                                                1
                                        ))
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes(ctx -> executeListPlayers(
                                                        ctx.getSource().getSender(),
                                                        DoubleArgumentType.getDouble(ctx, "level"),
                                                        IntegerArgumentType.getInteger(ctx, "season"),
                                                        IntegerArgumentType.getInteger(ctx, "page")
                                                ))
                                        )
                                )
                        )
                )
                .then(Commands.literal("tp")
                        .requires(source -> true) // anyone can use
                        .then(Commands.argument("level", DoubleArgumentType.doubleArg(0))
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    if (!(sender instanceof Player player)) {
                                        sender.sendMessage(Colorize("<prefix>This command can only be run by a player!"));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    double requestedLevel = DoubleArgumentType.getDouble(ctx, "level");
                                    int requestedSeason = checkpointDatabase.getSeason(player.getUniqueId()); // default to current season

                                    return handleTeleport(sender, player, requestedSeason, requestedLevel, false);
                                })
                                // Optional season argument
                                .then(Commands.argument("season", IntegerArgumentType.integer(1, 3))
                                        .executes(ctx -> {
                                            CommandSender sender = ctx.getSource().getSender();
                                            if (!(sender instanceof Player player)) {
                                                sender.sendMessage(Colorize("<prefix>This command can only be run by a player!"));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            double requestedLevel = DoubleArgumentType.getDouble(ctx, "level");
                                            int requestedSeason = IntegerArgumentType.getInteger(ctx, "season");

                                            return handleTeleport(sender, player, requestedSeason, requestedLevel, false);
                                        })
                                        // Player argument (only ops)
                                        .then(Commands.argument("player", StringArgumentType.word())
                                                .suggests(CheckpointCommand::playerSuggestions)
                                                .requires(source -> source.getSender().isOp())
                                                .executes(ctx -> {
                                                    CommandSender sender = ctx.getSource().getSender();
                                                    Player target = Bukkit.getPlayer(StringArgumentType.getString(ctx, "player"));
                                                    if (target == null) {
                                                        sender.sendMessage(Colorize("<prefix>Player not found."));
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    double requestedLevel = DoubleArgumentType.getDouble(ctx, "level");
                                                    int requestedSeason = IntegerArgumentType.getInteger(ctx, "season");

                                                    return handleTeleport(sender, target, requestedSeason, requestedLevel, false);
                                                })
                                                // Set checkpoint for player (only ops)
                                                .then(Commands.argument("setCheckpoint", BoolArgumentType.bool())
                                                        .executes(ctx -> {
                                                            CommandSender sender = ctx.getSource().getSender();
                                                            Player target = Bukkit.getPlayer(StringArgumentType.getString(ctx, "player"));
                                                            if (target == null) {
                                                                sender.sendMessage(Colorize("<prefix>Player not found."));
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            double requestedLevel = DoubleArgumentType.getDouble(ctx, "level");
                                                            int requestedSeason = IntegerArgumentType.getInteger(ctx, "season");
                                                            boolean setCheckpoint = BoolArgumentType.getBool(ctx, "setCheckpoint");

                                                            return handleTeleport(sender, target, requestedSeason, requestedLevel, setCheckpoint);
                                                        })
                                                )
                                        )
                                )
                        )
                )
                .build();
    }
    private static CompletableFuture<Suggestions> playerSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
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
        List<CheckpointDatabase.CheckpointData> all = checkpointDatabase.getAllSortedBySeasonLevel();
        int totalUnfiltered = all.size();

        if (page == 1) {
            sender.sendMessage(Colorize("<prefix>Sorting <accent>" + totalUnfiltered + "<main> players..."));
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> sendLeaderboard(sender, page, all));
            return 0;
        }

        return sendLeaderboard(sender, page, all);
    }
    private static int sendLeaderboard(CommandSender sender, int page, List<CheckpointDatabase.CheckpointData> all) {
        List<CheckpointDatabase.CheckpointData> filtered = all.stream()
                .filter(data -> {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(data.uuid());
                    return !p.isOp() && !(data.season() == 1 && data.level() == 9999);
                })
                .toList();

        int totalEntries = filtered.size();
        int entriesPerPage = 10;
        int totalPages = (totalEntries + entriesPerPage - 1) / entriesPerPage;

        int start = (page - 1) * entriesPerPage;
        int end = Math.min(start + entriesPerPage, totalEntries);

        if (start >= totalEntries) {
            sender.sendMessage(Colorize("<prefix>No leaderboard entries on this page."));
            return start;
        }

        sender.sendMessage(Colorize("<prefix>Checkpoints Leaderboard <accent>(Page " + page + " of " + totalPages + ")"));

        for (int i = start; i < end; i++) {
            var entry = filtered.get(i);
            String name = Optional.ofNullable(Bukkit.getOfflinePlayer(entry.uuid()).getName()).orElse("Unknown");
            sender.sendMessage(Colorize("<accent>#" + (i + 1) + ". <main>" + name + ": <accent>" + entry.season() + "-" + String.valueOf(entry.level()).replace(".0", "")));
        }
        String nav = "";
        if (page > 1) {
            nav += "<click:run_command:/checkpoints leaderboard " + (page - 1) + "><accent>[← Previous]</click> ";
        }
        if (page < totalPages) {
            nav += "<click:run_command:/checkpoints leaderboard " + (page + 1) + "><accent>[Next →]</click>";
        }
        if (!nav.isEmpty()) {
            sender.sendMessage(Colorize(nav));
        }

        return start;
    }
    private static int executeListPlayers(CommandSender sender, double level, int season, int page) {
        List<UUID> uuids = checkpointDatabase.getPlayersAtCheckpoint(season, level);

        if (uuids == null || uuids.isEmpty()) {
            sender.sendMessage(Colorize(
                    "<prefix>No player data found for <accent>" +
                            season + "-" + String.valueOf(level).replace(".0", "") +
                            "<main>."
            ));
            return 2;
        }

        int entriesPerPage = 10;
        int totalEntries = uuids.size();
        int totalPages = (totalEntries + entriesPerPage - 1) / entriesPerPage;

        int start = (page - 1) * entriesPerPage;
        int end = Math.min(start + entriesPerPage, totalEntries);

        if (start >= totalEntries) {
            sender.sendMessage(Colorize("<prefix>No players on this page."));
            return 2;
        }

        sender.sendMessage(Colorize(
                "<prefix><accent>" + totalEntries + "<main> Players on <accent>" +
                        season + "-" + String.valueOf(level).replace(".0", "") +
                        "<main> (Page " + page + " of " + totalPages + "):"
        ));

        boolean isOp = sender.isOp();

        for (int i = start; i < end; i++) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(uuids.get(i));
            String name = (p.getName() != null) ? p.getName() : "Unknown";

            if (isOp && !name.equals("Unknown")) {
                if(p.isOnline()) {
                    sender.sendMessage(Colorize(
                            "<click:run_command:/tp " + name + "><accent>- <main>" + name + "</click>"
                    ));
                } else {
                    sender.sendMessage(Colorize(
                            "<click:run_command:/tpo " + name + "><accent>- <main>" + name + "</click>"
                    ));
                }
            } else {
                sender.sendMessage(Colorize("<accent>- <main>" + name));
            }
        }

        String nav = "";
        if (page > 1) {
            nav += "<click:run_command:/checkpoint listPlayers "
                    + String.valueOf(level).replace(".0", "") + " "
                    + season + " " + (page - 1) +
                    "><accent>[← Previous]</click> ";
        }
        if (page < totalPages) {
            nav += "<click:run_command:/checkpoint listPlayers "
                    + String.valueOf(level).replace(".0", "") + " "
                    + season + " " + (page + 1) +
                    "><accent>[Next →]</click>";
        }

        if (!nav.isEmpty()) {
            sender.sendMessage(Colorize(nav));
        }
        return 1;
    }
    private static boolean isHigherCheckpoint(int currentSeason, double currentLevel, int targetSeason, double targetLevel) {
        return targetSeason > currentSeason || (targetSeason == currentSeason && targetLevel >= currentLevel);
    }
    private static int handleTeleport(CommandSender sender, Player target, int season, double level, boolean setCheckpoint) {
        int currentSeason = checkpointDatabase.getSeason(target.getUniqueId());
        double currentLevel = checkpointDatabase.getLevel(target.getUniqueId());

        boolean senderBypass = sender.isOp() || (sender instanceof Player && sender.hasPermission("hardcourse.winner"));

        if (!senderBypass) {
            if (target.equals(sender) && currentSeason == 1 && currentLevel == 9999) {
                sender.sendMessage(Colorize("<prefix>You cannot teleport while on this level."));
                return 0;
            }

            if (isHigherCheckpoint(currentSeason, currentLevel, season, level)) {
                sender.sendMessage(Colorize("<prefix>You can only teleport to checkpoints below your current one!"));
                return 0;
            }
        }

        Location loc = checkpointDatabase.getCheckpointLocation(season, level);
        if (loc == null) {
            sender.sendMessage(Colorize("<prefix>No checkpoint location set for <accent>" + season + "-" + String.valueOf(level).replace(".0", "") + "<main>."));
            return Command.SINGLE_SUCCESS;
        }

        if (setCheckpoint) {
            resetForNewLevel(target.getUniqueId());
            checkpointDatabase.setLevel(target.getUniqueId(), level);
            checkpointDatabase.setSeason(target.getUniqueId(), season);

            if (DiscordEnabled) {
                final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                checkpointsChannel.sendMessage("`[" + f.format(new Date()) + "] " + target.getName() + " was set to level " + season + "-" + String.valueOf(level).replace(".0", "") + " by " + (sender instanceof Player ? sender.getName() + "`" : "CONSOLE`")).queue();
            }
        }

        target.teleport(loc);

        if(!target.isOp() || !target.hasPermission("hardcourse.winner")) {
            giveItems(target);
        }

        if(!target.getName().equals(sender.getName())) {
            sender.sendMessage(Colorize("<prefix>Teleported <accent>" + target.getName() + (setCheckpoint ? " <main>and set checkpoint to <accent>" : " <main>to checkpoint <accent>") + season + "-" + String.valueOf(level).replace(".0", "") + "<main>."));
        } else {
            sender.sendMessage(Colorize("<prefix>Teleported to checkpoint <accent>" + season + "-" + String.valueOf(level).replace(".0", "") + "<main>."));
        }
        return Command.SINGLE_SUCCESS;
    }

}
