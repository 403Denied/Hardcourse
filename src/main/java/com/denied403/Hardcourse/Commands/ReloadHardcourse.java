package com.denied403.Hardcourse.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.InitJDA;
import static com.denied403.Hardcourse.Discord.HardcourseDiscord.jda;
import static com.denied403.Hardcourse.Hardcourse.*;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class ReloadHardcourse {

    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .requires(source -> source.getSender().hasPermission("hardcourse.admin"))
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    plugin.reloadConfig();
                    loadConfigValues();
                    boolean wasEnabled = jda != null;
                    if (DiscordEnabled) {
                        if (wasEnabled) {
                            jda.shutdown();
                        }
                        InitJDA();
                    } else {
                        if (wasEnabled) {
                            jda.shutdown();
                            jda = null;
                        }
                    }
                    sender.sendMessage(Colorize("<prefix>Hardcourse config reloaded."));
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
