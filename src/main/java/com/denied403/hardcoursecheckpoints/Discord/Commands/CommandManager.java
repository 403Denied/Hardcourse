package com.denied403.hardcoursecheckpoints.Discord.Commands;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDiscordEnabled;

public class CommandManager extends ListenerAdapter {

    private final Console consoleCommand;
    private final List listCommand;
    private final Info infoCommand;

    public CommandManager(HardcourseCheckpoints plugin) {
        this.consoleCommand = new Console(plugin);
        this.listCommand = new List(); // If needed, pass plugin here too
        this.infoCommand = new Info(); // Same here
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event){
        if (isDiscordEnabled()) {
            String command = event.getName();
            switch (command.toLowerCase()) {
                case "list" -> listCommand.run(event);
                case "info" -> infoCommand.run(event);
                case "console" -> consoleCommand.run(event);
            }
        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event){
        if (isDiscordEnabled()) {
            ArrayList<CommandData> commandData = new ArrayList<>();
            commandData.add(Commands.slash("list", "Get a list of online players"));
            commandData.add(Commands.slash("info", "Get server or player info")
                    .addOptions(Info.infoType, Info.playerName));
            commandData.add(Commands.slash("console", "Run a console command")
                    .addOptions(Console.toRunCommandOption()));
            event.getGuild().updateCommands().addCommands(commandData).queue();
        }
    }
}
