package com.denied403.Hardcourse.Discord.Commands;

import com.denied403.Hardcourse.Utils.CheckpointDatabase;
import com.denied403.Hardcourse.Utils.LinkManager;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.denied403.Hardcourse.Hardcourse.isDev;
import static com.denied403.Hardcourse.Hardcourse.plugin;
import static com.denied403.Hardcourse.Utils.ColorUtil.Colorize;

public class DiscordLink extends ListenerAdapter {
    private static LinkManager linkManager;
    private static CheckpointDatabase database;

    public static void initalize(LinkManager mgr, CheckpointDatabase db) {
        linkManager = mgr;
        database = db;
    }
    public static void run(SlashCommandInteractionEvent event){
        String username = event.getOption("username").getAsString();
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
        database.linkDiscord(uuid, event.getUser().getId());
        linkManager.clearCode(uuid);
        Role role;
        if(isDev()) {
            role = event.getGuild().getRoleById("1443771050780262472");
        } else {
            role = event.getGuild().getRoleById("1443756921487364159");
        }
        event.getGuild().addRoleToMember(UserSnowflake.fromId(event.getUser().getIdLong()), role)
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
        });
    }
}
