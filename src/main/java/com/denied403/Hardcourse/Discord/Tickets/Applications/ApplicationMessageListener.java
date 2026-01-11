package com.denied403.Hardcourse.Discord.Tickets.Applications;

import com.denied403.Hardcourse.Discord.Tickets.PanelButtonListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.denied403.Hardcourse.Hardcourse.applicationQuestions;

public class ApplicationMessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getChannelType() != ChannelType.PRIVATE) return;
        if (event.getAuthor().isBot()) return;

        String userId = event.getAuthor().getId();
        if (!PanelButtonListener.applicationProgress.containsKey(userId)) return;

        List<String> questions = applicationQuestions;
        int currentIndex = PanelButtonListener.applicationProgress.get(userId);
        String message = event.getMessage().getContentRaw().trim();
        MessageChannel channel = event.getChannel();

        if (currentIndex == -1 && message.toLowerCase().startsWith("edit ")) {
            try {
                int editIndex = Integer.parseInt(message.split(" ")[1]) - 1;
                if (editIndex < 0 || editIndex >= questions.size()) {
                    channel.sendMessage("❌ Invalid question number. Use a number between 1 and " + questions.size()).queue();
                    return;
                }

                PanelButtonListener.applicationEditing.add(userId);
                PanelButtonListener.applicationProgress.put(userId, editIndex);

                List<String> answers = PanelButtonListener.applicationAnswers.get(userId);
                while (answers.size() <= editIndex) {
                    answers.add("");
                }

                channel.sendMessage("✏️ Re-answer **Question " + (editIndex + 1) + "**:\n" + questions.get(editIndex)).queue();
            } catch (Exception e) {
                channel.sendMessage("❌ Invalid format. Use `edit <number>` like `edit 2`.").queue();
            }
            return;
        }

        if (currentIndex == -1) {
            channel.sendMessage("⏳ You've completed your application. Use the buttons or type `edit <number>`.").queue();
            return;
        }

        List<String> answers = PanelButtonListener.applicationAnswers.get(userId);
        while (answers.size() <= currentIndex) {
            answers.add("");
        }
        answers.set(currentIndex, message);

        if (PanelButtonListener.applicationEditing.contains(userId)) {
            PanelButtonListener.applicationProgress.put(userId, -1);
            PanelButtonListener.applicationEditing.remove(userId);
        } else if (currentIndex + 1 >= questions.size()) {
            PanelButtonListener.applicationProgress.put(userId, -1);
        } else {
            PanelButtonListener.applicationProgress.put(userId, currentIndex + 1);
            channel.sendMessage("**Question " + (currentIndex + 2) + ":** " + questions.get(currentIndex + 1)).queue();
            return;
        }
        List<MessageEmbed> finalEmbeds = new ArrayList<>();

        EmbedBuilder currentEmbed = new EmbedBuilder()
                .setTitle("📋 Confirm Your Application")
                .setColor(Color.YELLOW)
                .setFooter("User ID: " + userId, event.getAuthor().getEffectiveAvatarUrl())
                .setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getEffectiveAvatarUrl());

        int fieldCount = 0;
        int currentEmbedLength = 0;

        for (int i = 0; i < questions.size(); i++) {

            String question = "Q" + (i + 1) + ": " + questions.get(i);
            String answer = answers.get(i);

            List<String> chunks = new ArrayList<>();
            while (answer.length() > 1024) {
                chunks.add(answer.substring(0, 1024));
                answer = answer.substring(1024);
            }
            chunks.add(answer);

            for (int chunkIndex = 0; chunkIndex < chunks.size(); chunkIndex++) {

                String fieldTitle = (chunkIndex == 0 ? question : question + " (cont.)");
                String fieldValue = chunks.get(chunkIndex);

                int fieldTotalLength = fieldTitle.length() + fieldValue.length();

                if (fieldCount >= 25 || currentEmbedLength + fieldTotalLength >= 5900) {
                    finalEmbeds.add(currentEmbed.build());

                    currentEmbed = new EmbedBuilder()
                            .setColor(Color.YELLOW)
                            .setFooter("User ID: " + userId, event.getAuthor().getEffectiveAvatarUrl());

                    fieldCount = 0;
                    currentEmbedLength = 0;
                }

                currentEmbed.addField(fieldTitle, fieldValue, false);

                fieldCount++;
                currentEmbedLength += fieldTotalLength;
            }
        }

        finalEmbeds.add(currentEmbed.build());

        channel.sendMessage("✅ Your answer has been updated.\nPlease review your application below:")
                .queue(msg -> {
                    for (int i = 0; i < finalEmbeds.size() - 1; i++) {
                        channel.sendMessageEmbeds(finalEmbeds.get(i)).queue();
                    }
                    MessageEmbed lastEmbed = finalEmbeds.getLast();

                    channel.sendMessageEmbeds(lastEmbed)
                            .setActionRow(
                                    Button.success("application:submit", "✅ Submit"),
                                    Button.secondary("application:edit", "✏️ Edit A Response"),
                                    Button.danger("application:cancel", "❌ Cancel")
                            ).queue();
                });

    }
}
