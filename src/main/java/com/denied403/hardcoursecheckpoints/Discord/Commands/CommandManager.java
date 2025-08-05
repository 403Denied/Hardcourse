package com.denied403.hardcoursecheckpoints.Discord.Commands;

import com.denied403.hardcoursecheckpoints.Discord.Tickets.BlockFromTickets;
import com.denied403.hardcoursecheckpoints.Discord.Tickets.SendTicketPanel;
import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDev;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDiscordEnabled;

public class CommandManager extends ListenerAdapter {

    private final Console consoleCommand;
    private final SendTicketPanel setupTicketsCommand;
    private final BlockFromTickets blockFromTicketsCommand;

    public CommandManager(HardcourseCheckpoints plugin) {
        this.consoleCommand = new Console(plugin);
        this.setupTicketsCommand = new SendTicketPanel();
        this.blockFromTicketsCommand = new BlockFromTickets();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event){
        if (isDiscordEnabled()) {
            String command = event.getName();
            switch (command.toLowerCase()) {
                case "list" -> List.run(event);
                case "info" -> Info.run(event);
                case "console" -> consoleCommand.run(event);
                case "setuptickets" -> setupTicketsCommand.run(event);
                case "blockfromtickets" -> blockFromTicketsCommand.run(event);
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
            if(isDev()) {
                commandData.add(Commands.slash("setuptickets", "Setup the ticket system")
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                        .addOptions(SendTicketPanel.channel())
                );
                commandData.add(Commands.slash("blockfromtickets", "Block a user from creating tickets")
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                        .addOptions(BlockFromTickets.userToBlock()));
            }
            event.getGuild().updateCommands().addCommands(commandData).queue();
        }
    }
}
