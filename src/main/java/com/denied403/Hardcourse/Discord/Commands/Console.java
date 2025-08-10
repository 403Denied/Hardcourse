package com.denied403.Hardcourse.Discord.Commands;

import com.denied403.Hardcourse.Hardcourse;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

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

        event.reply("✅ Ran command: `" + command + "`").setEphemeral(true).queue();
    }
    public static OptionData toRunCommandOption() {
        return new OptionData(OptionType.STRING, "command", "The command to run on the server console")
                .setRequired(true);
    }
}


