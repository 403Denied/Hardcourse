package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import com.denied403.hardcoursecheckpoints.Utils.CheckpointDatabase;
import com.mojang.brigadier.Command;
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
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.denied403.hardcoursecheckpoints.Utils.ColorUtil.Colorize;
public class CheckpointCommand {

    private static CheckpointDatabase database;

    public static void initialize(CheckpointDatabase db) {
        database = db;
    }

    public static LiteralCommandNode<CommandSourceStack> createCommand(HardcourseCheckpoints plugin, String commandName) {
        return Commands.literal(commandName)

                .then(Commands.literal("set")
                        .requires(source -> source.getSender().isOp())
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(CheckpointCommand::playerSuggestions)
                                .then(Commands.argument("level", DoubleArgumentType.doubleArg(1))
                                        .executes(ctx -> {
                                            CommandSender sender = ctx.getSource().getSender();
                                            String playerName = StringArgumentType.getString(ctx, "player");
                                            double level = DoubleArgumentType.getDouble(ctx, "level");


                                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                                            UUID uuid = offlinePlayer.getUniqueId();

                                            database.setLevel(uuid, level);

                                            String formattedLevel = (level % 1 == 0)
                                                    ? String.valueOf((int) level)
                                                    : String.valueOf(level);

                                            if (offlinePlayer.isOnline() && offlinePlayer != sender) {
                                                ((Player) offlinePlayer).sendMessage(Colorize("&c&lHARDCOURSE &rYour level has been set to &c" + formattedLevel + "&f!"));
                                            }

                                            sender.sendMessage(Colorize("&c&lHARDCOURSE &rThe level of &c" + playerName + "&f has been set to &c" + formattedLevel + "&f!"));
                                            return Command.SINGLE_SUCCESS;
                                        })
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

                                            database.setCheckpointData(uuid, 1, 0, 0);
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
                                    database.deleteAll();
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
                                    Integer season = database.getSeason(uuid);
                                    Double level = database.getLevel(uuid);

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
                /*.then(Commands.literal("migrate")
                        .requires(source -> source.getSender().isOp())
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                File file = new File(plugin.getDataFolder(), "checkpoints.yml");
                                if (!file.exists()) {
                                    sender.sendMessage(Colorize("&c&lHARDCOURSE &rData file not found."));
                                    return;
                                }
                                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                                int total = 0;
                                int migrated = 0;
                                for (String key : config.getKeys(false)) {
                                    try {
                                        UUID uuid = UUID.fromString(key);
                                        double level = config.getDouble(key);
                                        if (!database.hasData(uuid) && level >= 300) {
                                            database.setCheckpointData(uuid, 1, level, 0);
                                            migrated++;
                                        }
                                        total++;
                                    } catch (IllegalArgumentException e) {
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rInvalid UUID in data file: " + key));
                                    }
                                }
                                sender.sendMessage(Colorize("&c&lHARDCOURSE &rMigrated &c" + migrated + "&f checkpoint entries to the database. Cut &c" + (total - migrated) + " &fof total &c" + total + " &fentries."));
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                )*/
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
    private static int executeLeaderboard(HardcourseCheckpoints plugin, CommandSender sender, int page) {
        List<CheckpointDatabase.CheckpointData> all = database.getAllSortedBySeasonLevel();
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
            sender.sendMessage(Colorize("&c#" + (i + 1) + ". &f" + name + ": &c" + entry.season() + "-" + String.valueOf(entry.level()).replaceAll(".0", "")));
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
