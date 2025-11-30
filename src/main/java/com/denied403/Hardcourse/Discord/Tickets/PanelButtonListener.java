package com.denied403.Hardcourse.Discord.Tickets;

import com.denied403.Hardcourse.Hardcourse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PanelButtonListener extends ListenerAdapter {
    private final Hardcourse plugin;

    public PanelButtonListener(Hardcourse plugin) {
        this.plugin = plugin;
    }

    public static final Map<String, Integer> applicationProgress = new HashMap<>();
    public static final Map<String, List<String>> applicationAnswers = new HashMap<>();
    public static final Set<String> applicationEditing = new HashSet<>();

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        String userId = event.getUser().getId();

        switch (id) {
            case "ticket:application" -> {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("📋 Application Information")
                        .addField("Rules", """
                                Please read these rules before applying:

                                • You must be 14 years old or older
                                • You must have at least 4 hours of playtime
                                • You must not have any punishments within the last month or a history of severe punishments
                                • You must have an active Discord account that has been in the server for at least 2 weeks
                                • To make sure you are paying attention to these guidelines, please include the word `Toast` somewhere in your application. It cannot be in place of an answer.
                                • You must be able to communicate in fluent English
                                • Intentionally bringing up your application to a staff member or asking them to read it will result in a denial.
                                """, true)
                        .addField("Reminders", """
                                Please read these reminders before applying:
                                
                                • You must meet all listed requirements in order to apply
                                • Take your time with your application, this is something you do not need to rush
                                • With questions that include "Explain your answer" or "Why?" We want you to put into detail. Again, don't rush these questions.
                                • Once you have completed the application, read over it. Check for any spelling mistakes, punctuation etc. Having good grammar and punctuation will help you within your application.
                                • We are under no obligation to share any information given in this application without your consent
                                """, true)
                        .setDescription("Click \"Agree\" to begin your application, or \"Cancel\" to stop.")
                        .setColor(Color.ORANGE);

                event.replyEmbeds(embed.build())
                        .addActionRow(
                                Button.success("send_application:agree", "Agree"),
                                Button.danger("send_application:cancel", "Cancel")
                        )
                        .setEphemeral(true).queue();
            }
            case "send_application:agree" -> event.getUser().openPrivateChannel().queue(
                    channel -> {
                        List<String> questions = plugin.getApplicationQuestions();

                        applicationProgress.put(userId, 0);
                        applicationAnswers.put(userId, new ArrayList<>());

                        channel.sendMessage("Welcome to the application process!\n⚠️ Beware that since this application is done via a discord bot, if it shuts down, your application progress will be lost. Not to worry, simply join Hardcourse to start it again, and paste your answers back in.").queue();
                        channel.sendMessage("**Question 1:** " + questions.getFirst()).queue();
                        event.reply("📨 Check your DMs to begin your application. If you did not receive a DM, please make sure you have your DMs open for this server.").setEphemeral(true).queue();
                    }, failure -> event.reply("❌ Couldn't DM you. Please enable DMs.").setEphemeral(true).queue()
            );
            case "send_application:cancel" -> {
                event.deferEdit().queue();
                event.getMessage().delete().queue();
            }
        }
    }
}
