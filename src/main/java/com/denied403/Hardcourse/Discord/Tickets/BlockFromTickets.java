package com.denied403.Hardcourse.Discord.Tickets;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class BlockFromTickets {
    public void run(SlashCommandInteractionEvent e){
        User userToBlock = e.getOption("user").getAsUser();
        e.reply("Blocked " + userToBlock.getAsMention() + " from tickets.").setEphemeral(true).queue();
    }
    public static OptionData userToBlock() {
        return new OptionData(OptionType.USER, "user", "The user to block from tickets").setRequired(true);
    }
}
