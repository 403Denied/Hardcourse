package com.denied403.Hardcourse.Discord.Tickets;

import com.denied403.Hardcourse.Hardcourse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.List;

public class ApplicationButtonListener extends ListenerAdapter {

    private final Hardcourse plugin;
    private static final String LOG_CHANNEL_ID = "880187846026207282";

    public ApplicationButtonListener(Hardcourse plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String userId = event.getUser().getId();

        if (!TicketButtonListener.applicationAnswers.containsKey(userId)) return;

        switch (event.getComponentId()) {
            case "application:submit" -> {
                List<String> questions = plugin.getApplicationQuestions();
                List<String> answers = TicketButtonListener.applicationAnswers.get(userId);

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("📥 New Application Submission")
                        .setColor(Color.CYAN)
                        .setFooter("User ID: " + userId, event.getUser().getEffectiveAvatarUrl())
                        .setAuthor(event.getUser().getName(), null, event.getUser().getEffectiveAvatarUrl());

                for (int i = 0; i < questions.size(); i++) {
                    String question = questions.get(i);
                    String answer = i < answers.size() ? answers.get(i) : "*No answer*";
                    embed.addField("Q" + (i + 1) + ": " + question, answer, false);
                }

                // Send application to log channel
                event.getJDA().getTextChannelById(LOG_CHANNEL_ID)
                        .sendMessageEmbeds(embed.build()).queue();

                // Acknowledge submit
                event.reply("✅ Your application has been submitted! Expect a response within 3 days.")
                        .setEphemeral(true).queue();

                // Disable buttons
                event.getMessage().editMessageComponents(
                        ActionRow.of(
                                Button.success("application:submit", "✅ Submit").asDisabled(),
                                Button.secondary("application:edit", "✏️ Edit Another Response").asDisabled(),
                                Button.danger("application:cancel", "❌ Cancel Application").asDisabled()
                        )
                ).queue();

                TicketButtonListener.applicationAnswers.remove(userId);
                TicketButtonListener.applicationProgress.remove(userId);
            }

            case "application:edit" -> {
                event.reply("✏️ Please type `edit <question number>` to re-answer a specific question.\n" +
                                "For example: `edit 2` to re-answer question 2.")
                        .setEphemeral(true).queue();
                event.getMessage().editMessageComponents(
                        ActionRow.of(
                                Button.success("application:submit", "✅ Submit").asDisabled(),
                                Button.secondary("application:edit", "✏️ Edit A Response").asDisabled(),
                                Button.danger("application:cancel", "❌ Cancel Application").asDisabled()
                        )
                ).queue();
            }
            case "application:cancel" -> {
                event.reply("❌ Your application has been cancelled. You can start a new one anytime.")
                        .setEphemeral(true).queue();

                // Disable buttons
                event.getMessage().editMessageComponents(
                        ActionRow.of(
                                Button.success("application:submit", "✅ Submit").asDisabled(),
                                Button.secondary("application:edit", "✏️ Edit Another Response").asDisabled(),
                                Button.danger("application:cancel", "❌ Cancel Application").asDisabled()
                        )
                ).queue();

                TicketButtonListener.applicationAnswers.remove(userId);
                TicketButtonListener.applicationProgress.remove(userId);
            }
        }
    }
}
