package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.Points.PointsManager;
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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.denied403.hardcoursecheckpoints.Utils.Colorize.Colorize;

public class Points {

    public static LiteralCommandNode<CommandSourceStack> createCommand(PointsManager pointsManager, String commandName) {
        return Commands.literal(commandName)
                .requires(source -> source.getSender().hasPermission("hardcourse.points.manage"))

                // /points set <player> <amount>
                .then(Commands.literal("set")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Points::onlinePlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            return handlePoints(ctx, pointsManager, "set");
                                        })
                                )
                        )
                )

                // /points give <player> <amount>
                .then(Commands.literal("give")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Points::onlinePlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            return handlePoints(ctx, pointsManager, "give");
                                        })
                                )
                        )
                )

                // /points remove <player> <amount>
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(Points::onlinePlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            return handlePoints(ctx, pointsManager, "remove");
                                        })
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

                                    int currentPoints = pointsManager.getPoints(target.getUniqueId());

                                    if (sender.equals(target)) {
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rYou have &c" + currentPoints + "&r points."));
                                    } else {
                                        sender.sendMessage(Colorize("&c&lHARDCOURSE &r" + playerName + " has &c" + currentPoints + "&r points."));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )

                .build();
    }

    private static int handlePoints(CommandContext<CommandSourceStack> ctx, PointsManager pointsManager, String action) {
        CommandSender sender = ctx.getSource().getSender();
        String targetName = StringArgumentType.getString(ctx, "player");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(Colorize("&c&lHARDCOURSE &rPlayer '&c" + targetName + "&r' not found or not online."));
            return 0;
        }

        UUID uuid = target.getUniqueId();

        switch (action) {
            case "set" -> {
                pointsManager.setPoints(uuid, amount);
                sender.sendMessage(Colorize("&c&lHARDCOURSE &rSet &c" + targetName + "&r's points to &c" + amount + "&r."));
                target.sendMessage(Colorize("&c&lHARDCOURSE &rYour points have been set to &c" + amount + "&r by &c" + sender.getName() + "&r."));
            }
            case "give" -> {
                pointsManager.addPoints(uuid, amount);
                sender.sendMessage(Colorize("&c&lHARDCOURSE &rGave &c" + amount + "&r points to &c" + targetName + "&r."));
                target.sendMessage(Colorize("&c&lHARDCOURSE &rYou received &c" + amount + "&r points from &c" + sender.getName() + "&r."));
            }
            case "remove" -> {
                pointsManager.removePoints(uuid, amount);
                sender.sendMessage(Colorize("&c&lHARDCOURSE &rRemoved &c" + amount + "&r points from &c" + targetName + "&r."));
                target.sendMessage(Colorize("&c&lHARDCOURSE &r&c" + amount + "&r points were removed by &c" + sender.getName() + "&r."));
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
}
