package com.denied403.Hardcourse.Discord.Tickets;

import com.denied403.Hardcourse.Hardcourse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.List;

public class ApplicationMessageListener extends ListenerAdapter {

    private final Hardcourse plugin;

    public ApplicationMessageListener(Hardcourse plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getChannelType() != ChannelType.PRIVATE) return;
        if (event.getAuthor().isBot()) return;

        String userId = event.getAuthor().getId();
        if (!TicketButtonListener.applicationProgress.containsKey(userId)) return;

        List<String> questions = plugin.getApplicationQuestions();
        int currentIndex = TicketButtonListener.applicationProgress.get(userId);
        String message = event.getMessage().getContentRaw().trim();
        MessageChannel channel = event.getChannel();

        // 🔁 Edit mode trigger
        if (currentIndex == -1 && message.toLowerCase().startsWith("edit ")) {
            try {
                int editIndex = Integer.parseInt(message.split(" ")[1]) - 1;
                if (editIndex < 0 || editIndex >= questions.size()) {
                    channel.sendMessage("❌ Invalid question number. Use a number between 1 and " + questions.size()).queue();
                    return;
                }

                TicketButtonListener.applicationEditing.add(userId);
                TicketButtonListener.applicationProgress.put(userId, editIndex);

                // Ensure the list is big enough
                List<String> answers = TicketButtonListener.applicationAnswers.get(userId);
                while (answers.size() <= editIndex) {
                    answers.add("");
                }

                channel.sendMessage("✏️ Re-answer **Question " + (editIndex + 1) + "**:\n" + questions.get(editIndex)).queue();
            } catch (Exception e) {
                channel.sendMessage("❌ Invalid format. Use `edit <number>` like `edit 2`.").queue();
            }
            return;
        }

        // ⛔ Already complete and not editing
        if (currentIndex == -1) {
            channel.sendMessage("⏳ You've completed your application. Use the buttons or type `edit <number>`.").queue();
            return;
        }

        // ✅ Store the answer
        List<String> answers = TicketButtonListener.applicationAnswers.get(userId);
        while (answers.size() <= currentIndex) {
            answers.add("");
        }
        answers.set(currentIndex, message);

        // 🧭 Routing: edit vs full application flow
        if (TicketButtonListener.applicationEditing.contains(userId)) {
            TicketButtonListener.applicationProgress.put(userId, -1);
            TicketButtonListener.applicationEditing.remove(userId);
        } else if (currentIndex + 1 >= questions.size()) {
            TicketButtonListener.applicationProgress.put(userId, -1);
        } else {
            TicketButtonListener.applicationProgress.put(userId, currentIndex + 1);
            channel.sendMessage("**Question " + (currentIndex + 2) + ":** " + questions.get(currentIndex + 1)).queue();
            return;
        }

        // 📋 Confirmation embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📋 Confirm Your Application")
                .setColor(Color.YELLOW)
                .setFooter("User ID: " + userId, event.getAuthor().getEffectiveAvatarUrl())
                .setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getEffectiveAvatarUrl());

        while (answers.size() < questions.size()) {
            answers.add("*No answer*");
        }

        for (int i = 0; i < questions.size(); i++) {
            embed.addField("Q" + (i + 1) + ": " + questions.get(i), answers.get(i), false);
        }

        channel.sendMessage("✅ Your answer has been updated.\nPlease review your application below:")
                .addEmbeds(embed.build())
                .addActionRow(
                        Button.success("application:submit", "✅ Submit"),
                        Button.secondary("application:edit", "✏️ Edit A Response"),
                        Button.danger("application:cancel", "❌ Cancel")
                ).queue();
    }
}
