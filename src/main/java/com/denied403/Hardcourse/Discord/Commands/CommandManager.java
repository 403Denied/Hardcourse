package com.denied403.Hardcourse.Discord.Commands;

import com.denied403.Hardcourse.Hardcourse;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import com.transfemme.dev.core403.Punishments.PunishmentReason;

import java.util.ArrayList;

import static com.denied403.Hardcourse.Hardcourse.isDiscordEnabled;

public class CommandManager extends ListenerAdapter {

    private final Console consoleCommand;
    private final SendTicketPanel setupTicketsCommand;

    public CommandManager(Hardcourse plugin) {
        this.consoleCommand = new Console(plugin);
        this.setupTicketsCommand = new SendTicketPanel();
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
                case "link" -> DiscordLink.run(event);
                case "punish" -> Punish.run(event);
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
            commandData.add(Commands.slash("setuptickets", "Setup the ticket system")
                    .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                    .addOptions(SendTicketPanel.channel())
            );
            commandData.add(Commands.slash("link", "Link your Minecraft account")
                    .addOption(OptionType.STRING, "username", "Your Minecraft username", true)
                    .addOption(OptionType.STRING, "code", "Your 6-digit link code", true)
            );

            OptionData punishments = new OptionData(OptionType.STRING, "reason", "The punishment reason", true).setAutoComplete(true);


            commandData.add(Commands.slash("punish", "Punish a user in-game")
                    .addOption(OptionType.STRING, "player", "The name of the player", true)
                    .addOptions(punishments)
                    .addOption(OptionType.STRING, "note", "The note for this punishment", false)
            );
            event.getGuild().updateCommands().addCommands(commandData).queue();
        }
    }
    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("punish")) return;
        if (!event.getFocusedOption().getName().equals("reason")) return;

        String input = event.getFocusedOption().getValue().toLowerCase();

        java.util.List<Command.Choice> choices = PunishmentReason.getAllReasons().stream()
                .filter(r -> r.toLowerCase().contains(input))
                .limit(25)
                .map(r -> new Command.Choice(r, r))
                .toList();

        event.replyChoices(choices).queue();
    }


}
