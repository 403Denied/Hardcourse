package com.denied403.Hardcourse.Discord.Commands;

import com.denied403.Hardcourse.Hardcourse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import java.awt.*;
import java.time.Instant;

public class Console {
    private final Hardcourse plugin;

    public Console(Hardcourse plugin) {
        this.plugin = plugin;
    }

    public void run(SlashCommandInteractionEvent event) {
        if (!event.getUser().getId().equals("401582030506295308")) {
            event.reply("❌ You are not authorized to use this command.").setEphemeral(true).queue();
            return;
        }

        String command = event.getOption("command").getAsString();

        Bukkit.getScheduler().runTask(plugin, () -> {
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            Bukkit.dispatchCommand(console, command);
        });
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.GREEN);
        embed.setDescription("✅ Executed console command: `/" + command + "`");
        embed.setTimestamp(Instant.now());
        embed.setFooter("Hardcourse", event.getGuild().getIconUrl());
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}


