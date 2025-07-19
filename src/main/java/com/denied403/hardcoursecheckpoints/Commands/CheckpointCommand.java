package com.denied403.hardcoursecheckpoints.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.highestCheckpoint;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.setHighestCheckpoint;
import static com.denied403.hardcoursecheckpoints.Utils.ColorUtil.Colorize;
import static com.denied403.hardcoursecheckpoints.Utils.PermissionChecker.playerHasPermission;

public class CheckpointCommand {
    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
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

                                            setHighestCheckpoint(uuid, level);

                                            String formattedLevel = (level % 1 == 0)
                                                    ? String.valueOf((int) level)
                                                    : String.valueOf(level);

                                            if (offlinePlayer.isOnline()) {
                                                ((Player) offlinePlayer).sendMessage(Colorize("&c&lHARDCOURSE &rYour level has been set to &c" + formattedLevel + "&f!"));
                                            }

                                            sender.sendMessage(Colorize("&c&lHARDCOURSE &rThe level of &c" + playerName + "&f has been set to &c" + formattedLevel + "&f!"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )

                .then(Commands.literal("reset")
                        .requires(source -> source.getSender().isOp() && Commands.restricted(s -> true).test(source))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(CheckpointCommand::playerSuggestions)
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    String playerName = StringArgumentType.getString(ctx, "player");
                                    OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
                                    UUID uuid = player.getUniqueId();

                                    if (highestCheckpoint.containsKey(uuid)) {
                                        highestCheckpoint.remove(uuid);
                                        Bukkit.getPluginManager().getPlugin("Hardcourse").saveConfig();
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rCheckpoint for player &c" + playerName + " &fhas been reset."));
                                    } else {
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &r&c" + playerName + " &fdoes not have a recorded checkpoint."));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )

                .then(Commands.literal("resetall")
                        .requires(source -> source.getSender().isOp() && Commands.restricted(s -> true).test(source))
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            highestCheckpoint.clear();
                            Bukkit.getPluginManager().getPlugin("Hardcourse").saveConfig();
                            sender.sendMessage(Colorize("&c&lHARDCOURSE &rAll player checkpoints have been reset."));
                            return Command.SINGLE_SUCCESS;
                        })
                )

                .then(Commands.literal("purgeinactive")
                        .requires(source -> source.getSender().isOp() && Commands.restricted(s -> true).test(source))
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            highestCheckpoint.entrySet().removeIf(entry -> entry.getValue() == 1.0 || entry.getValue() == 0.0);
                            Bukkit.getPluginManager().getPlugin("Hardcourse").saveConfig();
                            sender.sendMessage(Colorize("&c&lHARDCOURSE &rAll inactive checkpoints have been purged."));
                            return Command.SINGLE_SUCCESS;
                        })
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
                                    Double level = highestCheckpoint.get(uuid);

                                    if (level == null) {
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rPlayer not found or has no checkpoints!"));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    String levelString = level.toString().replace(".0", "");

                                    if (!p.isOnline()) {
                                        if (playerHasPermission(p.getName(), "hardcourse.season3")) {
                                            levelString = "3-" + levelString;
                                        } else if (playerHasPermission(p.getName(), "hardcourse.season2")) {
                                            levelString = "2-" + levelString;
                                        } else if (playerHasPermission(p.getName(), "hardcourse.season1")) {
                                            levelString = "1-" + levelString;
                                        }
                                    } else {
                                        switch (onlineTarget.getWorld().getName().toLowerCase()) {
                                            case "season1" -> levelString = "1-" + levelString;
                                            case "season2" -> levelString = "2-" + levelString;
                                            case "season3", "season4" -> levelString = "3-" + levelString;
                                        }
                                    }

                                    sender.sendMessage(Colorize("&c&lHARDCOURSE &r&c" + playerName + "&f's level is: &c" + levelString));
                                    return Command.SINGLE_SUCCESS;
                                })
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

}
