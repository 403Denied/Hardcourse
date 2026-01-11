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

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.checkpointsChannel;
import static com.denied403.Hardcourse.Hardcourse.*;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;
import static com.transfemme.dev.core403.Util.ColorUtil.stripAllColors;

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

                .then(Commands.literal("resetall")
                        .requires(source -> source.getSender().isOp())
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            sender.sendMessage(Colorize("<prefix>This will erase &4ALL<main> checkpoint data."));
                            sender.sendMessage(Colorize("<main>Run <accent>/checkpoint resetall confirm <main>to confirm."));
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
                                                    "<main>\nDifficulty: <accent>" + stripAllColors(difficulty)
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
                                                            "<main>\nDifficulty: <accent>" + stripAllColors(difficulty)
                                            ));
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal("removedata")
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
                .then(Commands.literal("tp")
                        .requires(source -> source.getSender().isOp() || source.getSender().hasPermission("hardcourse.winner"))
                        .then(Commands.argument("level", DoubleArgumentType.doubleArg(0))
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    if (!(sender instanceof Player player)) {
                                        sender.sendMessage(Colorize("<prefix>This command can only be run by a player!"));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    double level = DoubleArgumentType.getDouble(ctx, "level");
                                    int season = checkpointDatabase.getSeason(player.getUniqueId());

                                    Location loc = checkpointDatabase.getCheckpointLocation(season, level);
                                    if (loc == null) {
                                        sender.sendMessage(Colorize("<prefix>No checkpoint location set for <accent>"
                                                + season + "-" + String.valueOf(level).replace(".0", "") + "<main>."));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    player.teleport(loc);
                                    sender.sendMessage(Colorize("<prefix>Teleported to checkpoint <accent>"
                                            + season + "-" + String.valueOf(level).replace(".0", "") + "<main>."));
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(Commands.argument("season", IntegerArgumentType.integer(1, 3))
                                        .executes(ctx -> {
                                            CommandSender sender = ctx.getSource().getSender();
                                            if (!(sender instanceof Player player)) {
                                                sender.sendMessage(Colorize("<prefix>This command can only be run by a player!"));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            double level = DoubleArgumentType.getDouble(ctx, "level");
                                            int season = IntegerArgumentType.getInteger(ctx, "season");

                                            Location loc = checkpointDatabase.getCheckpointLocation(season, level);
                                            if (loc == null) {
                                                sender.sendMessage(Colorize("<prefix>No checkpoint location set for <accent>"
                                                        + season + "-" + String.valueOf(level).replace(".0", "") + "<main>."));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            player.teleport(loc);
                                            sender.sendMessage(Colorize("<prefix>Teleported to checkpoint <accent>"
                                                    + season + "-" + String.valueOf(level).replace(".0", "") + "<main>."));
                                            return Command.SINGLE_SUCCESS;
                                        })
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

                                                    double level = DoubleArgumentType.getDouble(ctx, "level");
                                                    int season = IntegerArgumentType.getInteger(ctx, "season");

                                                    Location loc = checkpointDatabase.getCheckpointLocation(season, level);
                                                    if (loc == null) {
                                                        sender.sendMessage(Colorize("<prefix>No checkpoint location set for <accent>"
                                                                + season + "-" + String.valueOf(level).replace(".0", "") + "<main>."));
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    target.teleport(loc);
                                                    sender.sendMessage(Colorize("<prefix>Teleported <accent>"
                                                            + target.getName() + " <main>to checkpoint <accent>"
                                                            + season + "-" + String.valueOf(level).replace(".0", "") + "<main>."));
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                                .then(Commands.argument("setCheckpoint", BoolArgumentType.bool())
                                                        .executes(ctx -> {
                                                            CommandSender sender = ctx.getSource().getSender();

                                                            Player target = Bukkit.getPlayer(StringArgumentType.getString(ctx, "player"));
                                                            if (target == null) {
                                                                sender.sendMessage(Colorize("<prefix>Player not found."));
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            double level = DoubleArgumentType.getDouble(ctx, "level");
                                                            int season = IntegerArgumentType.getInteger(ctx, "season");
                                                            boolean setCheckpoint = BoolArgumentType.getBool(ctx, "setCheckpoint");

                                                            Location loc = checkpointDatabase.getCheckpointLocation(season, level);
                                                            if (loc == null) {
                                                                sender.sendMessage(Colorize("<prefix>No checkpoint location set for <accent>"
                                                                        + season + "-" + String.valueOf(level).replace(".0", "") + "<main>."));
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            if (setCheckpoint) {
                                                                checkpointDatabase.setLevel(target.getUniqueId(), level);
                                                                checkpointDatabase.setSeason(target.getUniqueId(), season);
                                                                if(DiscordEnabled) {
                                                                    final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
                                                                    f.setTimeZone(TimeZone.getTimeZone("UTC"));
                                                                    checkpointsChannel.sendMessage("`[" + f.format(new Date()) + "] " + target.getName() + " was set to level " + season + "-" + String.valueOf(level).replace(".0", "") + " by " + (sender instanceof Player ? sender.getName() + "`" : "CONSOLE`")).queue();
                                                                }
                                                            }

                                                            target.teleport(loc);

                                                            sender.sendMessage(Colorize("<prefix>Teleported <accent>" + target.getName() + (setCheckpoint ? " <main>and set checkpoint to <accent>" : " <main>to checkpoint <accent>") + season + "-" + String.valueOf(level).replace(".0", "") + "<main>."));
                                                            return Command.SINGLE_SUCCESS;
                                                        }))
                                        ))))

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

}
