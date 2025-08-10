package com.denied403.Hardcourse.Commands;

import com.denied403.Hardcourse.Discord.HardcourseDiscord;
import com.denied403.Hardcourse.Hardcourse;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.jda;
import static com.denied403.Hardcourse.Hardcourse.isDiscordEnabled;
import static com.denied403.Hardcourse.Hardcourse.loadConfigValues;
import static com.denied403.Hardcourse.Utils.ColorUtil.Colorize;

public class ReloadHardcourse {

    public static LiteralCommandNode<CommandSourceStack> createCommand(Hardcourse plugin, String commandName) {
        return Commands.literal(commandName)
                .requires(source -> source.getSender().hasPermission("hardcourse.admin"))
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();

                    loadConfigValues(plugin);
                    plugin.reloadConfig();
                    plugin.reloadWordsConfig();
                    HardcourseDiscord hardcourseDiscord = new HardcourseDiscord(plugin);
                    if(isDiscordEnabled()) {
                        jda.shutdown();
                        hardcourseDiscord.InitJDA();
                    }
                    sender.sendMessage(Colorize("&c&lHARDCOURSE &rHardcourse config reloaded."));
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
