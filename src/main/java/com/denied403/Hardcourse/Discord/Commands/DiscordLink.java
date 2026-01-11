package com.denied403.Hardcourse.Discord.Commands;

import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.linkedRole;
import static com.denied403.Hardcourse.Discord.HardcourseDiscord.linksChannel;
import static com.denied403.Hardcourse.Hardcourse.*;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class DiscordLink extends ListenerAdapter {
    public static void run(SlashCommandInteractionEvent event){
        String code =  event.getOption("code").getAsString();
        if(code.length() != 6 || !code.matches("\\d{6}")){
            event.reply("❌ Invalid code!").setEphemeral(true).queue();
            return;
        }
        UUID uuid = linkManager.getUUIDFromCode(code);
        if(uuid == null){
            event.reply("❌ Invalid or expired code!").setEphemeral(true).queue();
            return;
        }
        checkpointDatabase.linkDiscord(uuid, event.getUser().getId());
        linkManager.clearCode(uuid);
        event.getGuild().addRoleToMember(UserSnowflake.fromId(event.getUser().getIdLong()), linkedRole)
                .queue(
                        success -> {},
                        error -> event.getHook().sendMessage("⚠️ Could not assign role. Please notify an administrator: ```" + error.getMessage() + "```").setEphemeral(true).queue()
                );
        Player player = Bukkit.getPlayer(uuid);
        event.reply("✅ Successfully linked your Discord account to `" + player.getName() + "`!").setEphemeral(true).queue();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if(player.isOnline()){
                player.sendMessage(Colorize("&c&lHARDCOURSE &rSuccessfully linked your Minecraft account to &c" + event.getUser().getName()));
            }
            if(isDev){
                pointsManager.addPoints(player.getUniqueId(), 500);
            }
            Bukkit.broadcast(Colorize("&c&lHARDCOURSE &r&c" + player.getName() + " &rjust linked their Discord account and gained access to the Minecraft <-> Discord chat" + (isDev ? " and &c500&r points!" : "!")));
        });
        final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        linksChannel.sendMessage("`[" + f.format(new Date()) + "] " + player.getName() + " linked to` <@" + event.getUser().getId() + ">").queue();
    }
}
