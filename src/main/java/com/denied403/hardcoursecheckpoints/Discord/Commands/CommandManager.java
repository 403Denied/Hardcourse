package com.denied403.hardcoursecheckpoints.Discord.Commands;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDiscordEnabled;

public class CommandManager extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event){
        if(isDiscordEnabled()) {
            super.onSlashCommandInteraction(event);
            String command = event.getName();
            if (command.equalsIgnoreCase("list")) {
                List.run(event);
            }
            if (command.equalsIgnoreCase("info")) {
                Info.run(event);
            }
        }
    }
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event){
        if(isDiscordEnabled()) {
            ArrayList<CommandData> commandData = new ArrayList<>();
            commandData.add(Commands.slash("list", "Get a list of online players"));
            commandData.add(Commands.slash("info", "Get information about either the server or a specific player").addOptions(Info.infoType, Info.playerName));
            event.getGuild().updateCommands().addCommands(commandData).queue();
        }
    }
}
