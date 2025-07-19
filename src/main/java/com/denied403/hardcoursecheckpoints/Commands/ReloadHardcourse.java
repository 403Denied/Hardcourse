package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.loadConfigValues;
import static com.denied403.hardcoursecheckpoints.Utils.Colorize.Colorize;

public class ReloadHardcourse {

    public static LiteralCommandNode<CommandSourceStack> createCommand(HardcourseCheckpoints plugin, String commandName) {
        return Commands.literal(commandName)
                .requires(source -> source.getSender().hasPermission("hardcourse.admin"))
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();

                    loadConfigValues(plugin);
                    plugin.saveCheckpoints();
                    plugin.reloadConfig();
                    plugin.reloadCheckpointsConfig();
                    plugin.reloadPointsConfig();
                    plugin.reloadWordsConfig();

                    sender.sendMessage(Colorize("&c&lHARDCOURSE &fHardcourse config reloaded."));
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
