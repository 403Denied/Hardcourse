package com.denied403.hardcoursecheckpoints.Commands;

import com.mojang.brigadier.Command;
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

import java.util.concurrent.CompletableFuture;

import static com.denied403.hardcoursecheckpoints.Utils.ColorUtil.Colorize;

public class WinnerTP {

    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .requires(source -> source.getSender() instanceof Player &&
                        source.getSender().hasPermission("hardcourse.winner"))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(WinnerTP::onlinePlayerSuggestions)
                        .executes(WinnerTP::executeTP))
                .build();
    }

    private static int executeTP(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Colorize("&c&lHARDCOURSE &rThis command can only be used by players."));
            return 0;
        }

        String targetName = StringArgumentType.getString(context, "player");
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            player.sendMessage(Colorize("&c&lHARDCOURSE &rPlayer not found or not online!"));
            return 0;
        }

        player.teleport(target);
        player.sendMessage(Colorize("&c&lHARDCOURSE &rTeleported to &c" + targetName));
        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> onlinePlayerSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().toLowerCase().startsWith(input)) {
                builder.suggest(online.getName());
            }
        }
        return builder.buildFuture();
    }
}
