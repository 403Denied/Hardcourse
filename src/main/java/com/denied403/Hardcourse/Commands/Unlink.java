package com.denied403.Hardcourse.Commands;

import com.denied403.Hardcourse.Utils.CheckpointDatabase;
import com.denied403.Hardcourse.Utils.LinkManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.denied403.Hardcourse.Hardcourse.isDev;
import static com.denied403.Hardcourse.Utils.ColorUtil.Colorize;

public class Unlink {
    private static LinkManager linkManager;
    private static CheckpointDatabase database;
    private static JDA jda;

    public static void initialize(LinkManager mgr, CheckpointDatabase db, JDA jdaInstance){
        linkManager = mgr;
        database = db;
        jda = jdaInstance;
    }
    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName){
        return Commands.literal(commandName)
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    if(!(sender instanceof Player player)) return 0;
                    if(!database.isLinked(player.getUniqueId())){
                        player.sendMessage(Colorize("&c&lHARDCOURSE &rYou are not linked to a discord account."));
                        return Command.SINGLE_SUCCESS;
                    }
                    Guild guild;
                    Role role;
                    if(isDev()) {
                        guild = jda.getGuildById("715526616721391647");
                    } else {
                        guild = jda.getGuildById("715009722713112738");
                    }
                    if (guild != null) {
                        if(isDev()) {
                            role = guild.getRoleById("1443771050780262472");
                        } else {
                            role = guild.getRoleById("1443756921487364159");
                        }
                        if (role != null) {
                            String discordId = database.getDiscordId(player.getUniqueId());
                            if (discordId != null) {
                                guild.removeRoleFromMember(UserSnowflake.fromId(discordId), role).queue();
                            }
                        }
                    }
                    database.unlinkDiscord(player.getUniqueId());
                    linkManager.clearCode(player.getUniqueId());
                    player.sendMessage(Colorize("&c&lHARDCOURSE &rYour account has been &cunlinked&r successfully."));
                    return Command.SINGLE_SUCCESS;
                }).build();
    }
}
