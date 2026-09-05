package com.denied403.Hardcourse.Commands;

import com.denied403.core403.Discord.DiscordService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import static com.denied403.Hardcourse.Hardcourse.loadConfigValues;
import static com.denied403.Hardcourse.Hardcourse.plugin;
import static com.denied403.core403.Util.ColorUtil.Colorize;

public class ReloadHardcourse {

    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .requires(source -> source.getSender().hasPermission("hardcourse.admin"))
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    plugin.reloadConfig();
                    loadConfigValues();
                    sender.sendMessage(Colorize("<prefix>Hardcourse config reloaded."));

                    if (DiscordService.owner() != null) {
                        boolean wasOnline = DiscordService.isEnabled();
                        if (wasOnline) {
                            sender.sendMessage(Colorize("<prefix>Restarting the Discord bot, this may take a moment..."));
                        }
                        DiscordService.restartAsync(success -> {
                            if (success) {
                                sender.sendMessage(Colorize(wasOnline ? "<prefix>Discord bot restarted." : "<prefix>Discord bot started."));
                            } else if (DiscordService.config() != null && DiscordService.config().isEnabled()) {
                                sender.sendMessage(Colorize("<prefix><error>Discord bot failed to restart, check the console."));
                            } else if (wasOnline) {
                                sender.sendMessage(Colorize("<prefix>Discord is disabled in discord-config.yml."));
                            }
                            // else: bot wasn't online before and still isn't - say nothing.
                        });
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
