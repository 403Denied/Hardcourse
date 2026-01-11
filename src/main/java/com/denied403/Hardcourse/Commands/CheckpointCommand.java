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

                                            sender.sendMessage(Colorize("&c&lHARDCOURSE &rThe level of &c" + playerName + "&f has been set to &c" + season + "-" + formattedLevel + "&f!"));


                                            if (offlinePlayer.isOnline() && offlinePlayer != sender) {
                                                ((Player) offlinePlayer).sendMessage(Colorize("&c&lHARDCOURSE &rYour level has been set to &c" + season + "-" + formattedLevel + "&f!"));
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

                                                    sender.sendMessage(Colorize("&c&lHARDCOURSE &rThe level of &c" + playerName + "&f has been set to &c" + season + "-" + formattedLevel + "&f!"));

                                                    if (offlinePlayer.isOnline() && offlinePlayer != sender) {
                                                        ((Player) offlinePlayer).sendMessage(Colorize("&c&lHARDCOURSE &rYour level has been set to &c" + season + "-" + formattedLevel + "&f!"));
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

                                    sender.sendMessage(Colorize("&c&lHARDCOURSE &rThis will reset &c" + playerName + "&r's checkpoint data."));
                                    sender.sendMessage(Colorize("&fRun &c/checkpoint reset " + playerName + " confirm &fto confirm."));
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(Commands.literal("confirm")
                                        .executes(ctx -> {
                                            CommandSender sender = ctx.getSource().getSender();
                                            String playerName = StringArgumentType.getString(ctx, "player");
                                            OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
                                            UUID uuid = player.getUniqueId();

                                            checkpointDatabase.setCheckpointData(uuid, 1, 0, 0);
                                            sender.sendMessage(Colorize("&c&lHARDCOURSE &rCheckpoint for &c" + playerName + " &fhas been reset."));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )

                .then(Commands.literal("resetall")
                        .requires(source -> source.getSender().isOp())
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            sender.sendMessage(Colorize("&c&lHARDCOURSE &rThis will erase &4ALL&r checkpoint data."));
                            sender.sendMessage(Colorize("&fRun &c/checkpoint resetall confirm &fto confirm."));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.literal("confirm")
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    checkpointDatabase.deleteAll();
                                    sender.sendMessage(Colorize("&c&lHARDCOURSE &rAll checkpoint data has been wiped."));
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
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rPlayer not found or has never played before!"));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    UUID uuid = p.getUniqueId();
                                    Integer season = checkpointDatabase.getSeason(uuid);
                                    Double level = checkpointDatabase.getLevel(uuid);

                                    if (season == null || level == null || level <= 0) {
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rPlayer not found or has no checkpoints!"));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    String levelString = season + "-" + level.toString().replace(".0", "");

                                    sender.sendMessage(Colorize("&c&lHARDCOURSE &r&c" + playerName + "&f's level is: &c" + levelString));
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
                                sender.sendMessage(Colorize("&c&lHARDCOURSE &rThis command can only be run by a player!"));
                                return Command.SINGLE_SUCCESS;
                            }
                            if(isDev) {
                                player.sendMessage(Colorize("&c&lHARDCOURSE &rAre you sure you want to restart? You will be reset at the beginning. Run &c/checkpoint restart confirm &fto confirm. This &4cannot&f be undone. This will also remove points and remove your rank."));
                            } else {
                                player.sendMessage(Colorize("&c&lHARDCOURSE &rAre you sure you want to restart? You will be reset at the beginning. Run &c/checkpoint restart confirm &fto confirm. This &4cannot&f be undone. This will also reset your rank."));
                            }
                            return Command.SINGLE_SUCCESS;
                        }).then(Commands.literal("confirm").executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            if (!(sender instanceof Player player)) {
                                sender.sendMessage(Colorize("&c&lHARDCOURSE &rThis command can only be run by a player!"));
                                return Command.SINGLE_SUCCESS;
                            }
                            player.sendMessage(Colorize("<click:run_command:'checkpoint restart cancel'>&c&lHARDCOURSE </bold>&fYour checkpoint is about to be reset in &c10 seconds&f. You may cancel by typing &c/checkpoint restart cancel&f, or by clicking this message."));
                            UUID uuid = player.getUniqueId();
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                if (!restartCancelled.contains(player.getUniqueId())) {
                                    checkpointDatabase.setCheckpointData(uuid, 1, 0, 0);
                                    player.performCommand("spawn");
                                    player.setRespawnLocation(player.getWorld().getSpawnLocation());
                                    player.sendMessage(Colorize("&c&lHARDCOURSE &rYou have been reset to the beginning."));
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
                                sender.sendMessage(Colorize("&c&lHARDCOURSE &rThis command can only be run by a player!"));
                                return Command.SINGLE_SUCCESS;
                            }
                            player.sendMessage(Colorize("&c&lHARDCOURSE &rRestart cancelled."));
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
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rThis checkpoint has no data recorded. It may not have been entered into the database yet."));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    String locationString =
                                            "&fWorld: &c" + location.getWorld().getName() +
                                                    "&f, X: &c" + location.getX() +
                                                    "&f, Y: &c" + location.getY() +
                                                    "&f, Z: &c" + location.getZ();

                                    String difficulty = checkpointDatabase.getCheckpointDifficulty(season, level);

                                    sender.sendMessage(Colorize(
                                            "&c&lHARDCOURSE &rCheckpoint Info for level &c1-" +
                                                    String.valueOf(level).replace(".0", "") +
                                                    "&f: " + locationString +
                                                    "&f\nDifficulty: &c" + stripAllColors(difficulty)
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
                                                sender.sendMessage(Colorize("&c&lHARDCOURSE &rThis checkpoint has no data recorded. It may not have been entered into the database yet."));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            String locationString =
                                                    "&fWorld: &c" + location.getWorld().getName() +
                                                            "&f, X: &c" + location.getX() +
                                                            "&f, Y: &c" + location.getY() +
                                                            "&f, Z: &c" + location.getZ();

                                            String difficulty = checkpointDatabase.getCheckpointDifficulty(season, level);

                                            sender.sendMessage(Colorize(
                                                    "&c&lHARDCOURSE &rCheckpoint Info for level &c" +
                                                            season + "-" +
                                                            String.valueOf(level).replace(".0", "") +
                                                            "&f: " + locationString +
                                                            "&f\nDifficulty: &c" + stripAllColors(difficulty)
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
                                    sender.sendMessage(Colorize("&c&lHARDCOURSE &rCheckpoint data for &c1-" + String.valueOf(level).replace(".0", "") + "&f has been removed from the database."));
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(Commands.argument("season", IntegerArgumentType.integer(1, 3))
                                        .executes(ctx -> {
                                            CommandSender sender = ctx.getSource().getSender();
                                            double level = DoubleArgumentType.getDouble(ctx, "level");
                                            int season = IntegerArgumentType.getInteger(ctx, "season");

                                            checkpointDatabase.removeCheckpointLocation(season, level);
                                            sender.sendMessage(Colorize("&c&lHARDCOURSE &rCheckpoint data for &c" + season + "-" + String.valueOf(level).replace(".0", "") + "&f has been removed from the database."));
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal("tp")
                        .requires(source -> source.getSender().isOp() || source.getSender().hasPermission("hardcourse.winner"))
                        .then(Commands.argument("level", DoubleArgumentType.doubleArg(0))
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    if (!(sender instanceof Player player)) {
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rThis command can only be run by a player!"));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    double level = DoubleArgumentType.getDouble(ctx, "level");
                                    int season = checkpointDatabase.getSeason(player.getUniqueId());

                                    Location loc = checkpointDatabase.getCheckpointLocation(season, level);
                                    if (loc == null) {
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rNo checkpoint location set for &c"
                                                + season + "-" + String.valueOf(level).replace(".0", "") + "&f."));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    player.teleport(loc);
                                    sender.sendMessage(Colorize("&c&lHARDCOURSE &rTeleported to checkpoint &c"
                                            + season + "-" + String.valueOf(level).replace(".0", "") + "&f."));
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(Commands.argument("season", IntegerArgumentType.integer(1, 3))
                                        .executes(ctx -> {
                                            CommandSender sender = ctx.getSource().getSender();
                                            if (!(sender instanceof Player player)) {
                                                sender.sendMessage(Colorize("&c&lHARDCOURSE &rThis command can only be run by a player!"));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            double level = DoubleArgumentType.getDouble(ctx, "level");
                                            int season = IntegerArgumentType.getInteger(ctx, "season");

                                            Location loc = checkpointDatabase.getCheckpointLocation(season, level);
                                            if (loc == null) {
                                                sender.sendMessage(Colorize("&c&lHARDCOURSE &rNo checkpoint location set for &c"
                                                        + season + "-" + String.valueOf(level).replace(".0", "") + "&f."));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            player.teleport(loc);
                                            sender.sendMessage(Colorize("&c&lHARDCOURSE &rTeleported to checkpoint &c"
                                                    + season + "-" + String.valueOf(level).replace(".0", "") + "&f."));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(Commands.argument("player", StringArgumentType.word())
                                                .suggests(CheckpointCommand::playerSuggestions)
                                                .requires(source -> source.getSender().isOp())
                                                .executes(ctx -> {
                                                    CommandSender sender = ctx.getSource().getSender();

                                                    Player target = Bukkit.getPlayer(StringArgumentType.getString(ctx, "player"));
                                                    if (target == null) {
                                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rPlayer not found."));
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    double level = DoubleArgumentType.getDouble(ctx, "level");
                                                    int season = IntegerArgumentType.getInteger(ctx, "season");

                                                    Location loc = checkpointDatabase.getCheckpointLocation(season, level);
                                                    if (loc == null) {
                                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rNo checkpoint location set for &c"
                                                                + season + "-" + String.valueOf(level).replace(".0", "") + "&f."));
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    target.teleport(loc);
                                                    sender.sendMessage(Colorize("&c&lHARDCOURSE &rTeleported &c"
                                                            + target.getName() + " &rto checkpoint &c"
                                                            + season + "-" + String.valueOf(level).replace(".0", "") + "&f."));
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                                .then(Commands.argument("setCheckpoint", BoolArgumentType.bool())
                                                        .executes(ctx -> {
                                                            CommandSender sender = ctx.getSource().getSender();

                                                            Player target = Bukkit.getPlayer(StringArgumentType.getString(ctx, "player"));
                                                            if (target == null) {
                                                                sender.sendMessage(Colorize("&c&lHARDCOURSE &rPlayer not found."));
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            double level = DoubleArgumentType.getDouble(ctx, "level");
                                                            int season = IntegerArgumentType.getInteger(ctx, "season");
                                                            boolean setCheckpoint = BoolArgumentType.getBool(ctx, "setCheckpoint");

                                                            Location loc = checkpointDatabase.getCheckpointLocation(season, level);
                                                            if (loc == null) {
                                                                sender.sendMessage(Colorize("&c&lHARDCOURSE &rNo checkpoint location set for &c"
                                                                        + season + "-" + String.valueOf(level).replace(".0", "") + "&f."));
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

                                                            sender.sendMessage(Colorize("&c&lHARDCOURSE &rTeleported &c" + target.getName() + (setCheckpoint ? " &rand set checkpoint to &c" : " &rto checkpoint &c") + season + "-" + String.valueOf(level).replace(".0", "") + "&f."));
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
            sender.sendMessage(Colorize("&c&lHARDCOURSE &rSorting &c" + totalUnfiltered + "&f players..."));
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
            sender.sendMessage(Colorize("&c&lHARDCOURSE &rNo leaderboard entries on this page."));
            return start;
        }

        sender.sendMessage(Colorize("&c&lHARDCOURSE&r Checkpoints Leaderboard &c(Page " + page + " of " + totalPages + ")"));

        for (int i = start; i < end; i++) {
            var entry = filtered.get(i);
            String name = Optional.ofNullable(Bukkit.getOfflinePlayer(entry.uuid()).getName()).orElse("Unknown");
            sender.sendMessage(Colorize("&c#" + (i + 1) + ". &f" + name + ": &c" + entry.season() + "-" + String.valueOf(entry.level()).replace(".0", "")));
        }
        String nav = "";
        if (page > 1) {
            nav += "<click:run_command:/checkpoints leaderboard " + (page - 1) + "><red>[← Previous]</red></click> ";
        }
        if (page < totalPages) {
            nav += "<click:run_command:/checkpoints leaderboard " + (page + 1) + "><red>[Next →]</red></click>";
        }
        if (!nav.isEmpty()) {
            sender.sendMessage(Colorize(nav));
        }

        return start;
    }

}
