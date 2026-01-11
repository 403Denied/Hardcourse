package com.denied403.Hardcourse.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import static com.denied403.Hardcourse.Discord.HardcourseDiscord.guild;
import static com.denied403.Hardcourse.Discord.HardcourseDiscord.linkedRole;
import static com.denied403.Hardcourse.Hardcourse.*;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class Unlink {
    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName){
        return Commands.literal(commandName)
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    if(!(sender instanceof Player player)) return 0;
                    if(!checkpointDatabase.isLinked(player.getUniqueId())){
                        player.sendMessage(Colorize("&c&lHARDCOURSE &rYou are not linked to a discord account."));
                        return Command.SINGLE_SUCCESS;
                    }
                    if(!DiscordEnabled){
                        player.sendMessage(Colorize("<prefix>Discord functionality is currently disabled. Please try again later."));
                        return Command.SINGLE_SUCCESS;
                    }
                    if (guild != null) {
                        if (linkedRole != null) {
                            String discordId = checkpointDatabase.getDiscordId(player.getUniqueId());
                            if (discordId != null) {
                                guild.removeRoleFromMember(UserSnowflake.fromId(discordId), linkedRole).queue();
                            }
                        }
                    }
                    checkpointDatabase.unlinkDiscord(player.getUniqueId());
                    linkManager.clearCode(player.getUniqueId());
                    player.sendMessage(Colorize("&c&lHARDCOURSE &rYour account has been &cunlinked&r successfully."));
                    return Command.SINGLE_SUCCESS;
                }).build();
    }
}
