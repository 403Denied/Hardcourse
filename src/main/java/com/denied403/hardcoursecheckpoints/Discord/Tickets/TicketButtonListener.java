package com.denied403.hardcoursecheckpoints.Discord.Tickets;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TicketButtonListener extends ListenerAdapter {
    private final HardcourseCheckpoints plugin;

    public TicketButtonListener(HardcourseCheckpoints plugin) {
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

            case "ticket:general" -> {
                TextInput reason = TextInput.create("reason", "What is this ticket about?", TextInputStyle.PARAGRAPH)
                        .setRequired(true)
                        .setMaxLength(1024)
                        .build();

                Modal ticketModal = Modal.create("modal:ticket", "Open a Ticket")
                        .addActionRow(reason)
                        .build();

                event.replyModal(ticketModal).queue();
            }

            case "ticket:appeal" -> {
                TextInput ign = TextInput.create("ign", "Your IGN", TextInputStyle.SHORT).setRequired(true).build();
                TextInput type = TextInput.create("type", "Punishment Type", TextInputStyle.SHORT).setRequired(true).build();
                TextInput reason = TextInput.create("punishment", "Punishment Reason", TextInputStyle.SHORT).setRequired(true).build();
                TextInput duration = TextInput.create("duration", "Punishment Duration", TextInputStyle.SHORT).setRequired(true).build();

                Modal appealModal = Modal.create("modal:appeal", "Appeal a Punishment")
                        .addActionRow(ign)
                        .addActionRow(type)
                        .addActionRow(reason)
                        .addActionRow(duration)
                        .build();

                event.replyModal(appealModal).queue();
            }


            case "ticket:application" -> {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("📋 Application Rules")
                        .setDescription("""
                                Please read the rules before applying:

                                • You must be 14 years old or older
                                • You must have at least 4 hours of playtime
                                • You must not have any punishments within the last month or a history of severe punishments
                                • You must have an active Discord account that has been in the server for at least 2 weeks
                                • You must be able to communicate in fluent English
                                • Intentionally bringing up your application to a staff member or asking them to read it will result in a denial.

                                Click "Agree" to begin your application or "Cancel" to stop.
                                """)
                        .setColor(Color.ORANGE);

                event.replyEmbeds(embed.build())
                        .addActionRow(
                                Button.success("application:agree", "Agree"),
                                Button.danger("application:cancel", "Cancel")
                        )
                        .setEphemeral(true).queue();
            }
            case "tickets:close" ->
                event.reply("Are you sure you want to close this ticket?")
                        .addActionRow(
                                Button.success("tickets:close:confirm", "Yes, close it"),
                                Button.danger("tickets:close:cancel", "No, cancel")
                        )
                        .setEphemeral(true).queue();
            case "tickets:close:confirm" -> {
                TextChannel channel = (TextChannel) event.getChannel();

                event.deferEdit().queue(hook -> hook.deleteOriginal().queue());

                channel.getHistory().retrievePast(1).queue(messages -> {
                    if (!messages.isEmpty()) {
                        Message firstMessage = messages.getFirst();
                        channel.sendMessage(firstMessage.toString()).queue();
                        List<User> mentionedUsers = firstMessage.getMentions().getUsers();

                        if (!mentionedUsers.isEmpty()) {
                            User ticketCreator = mentionedUsers.getFirst();
                            Member member = Objects.requireNonNull(event.getGuild()).getMember(ticketCreator);

                            if (member != null) {
                                channel.getManager()
                                        .putPermissionOverride(member, null, EnumSet.of(Permission.VIEW_CHANNEL))
                                        .queue();
                                String currentName = channel.getName();
                                if(!currentName.startsWith("ticket-closed-")){
                                    String ticketNumber = currentName.replace("ticket-", "");
                                    channel.getManager().setName("ticket-closed-" + ticketNumber).queue();
                                }

                                EmbedBuilder closedByEmbed = new EmbedBuilder()
                                        .setColor(Color.RED)
                                        .setTitle("🔒 Ticket Closed")
                                        .setDescription("Ticket closed by " + event.getUser().getAsMention());

                                EmbedBuilder controlsEmbed = new EmbedBuilder()
                                        .setColor(Color.DARK_GRAY)
                                        .setTitle("🎛️ Ticket Controls")
                                        .setDescription("Manage this ticket below.");

                                channel.sendMessageEmbeds(closedByEmbed.build()).queue();

                                channel.sendMessageEmbeds(controlsEmbed.build())
                                        .addActionRow(
                                                Button.success("tickets:open", "🔓 Open"),
                                                Button.danger("tickets:delete", "🗑️ Delete")
                                        ).queue();
                            } else {
                                event.getHook().sendMessage("❌ Couldn’t find the ticket creator.").setEphemeral(true).queue();
                            }
                        } else {
                            event.getHook().sendMessage("❌ No user mentioned in the ticket.").setEphemeral(true).queue();
                        }
                    }
                });
            }
            case "tickets:close:cancel" ->
                event.reply("Ticket closure cancelled.").setEphemeral(true).queue();
            case "tickets:open" -> {
                TextChannel channel = (TextChannel) event.getChannel();

                channel.getHistory().retrievePast(1).queue(messages -> {
                    if (!messages.isEmpty()) {
                        Message firstMessage = messages.getFirst();
                        List<User> mentionedUsers = firstMessage.getMentions().getUsers();

                        if (!mentionedUsers.isEmpty()) {
                            User ticketCreator = mentionedUsers.getFirst();
                            Member member = Objects.requireNonNull(event.getGuild()).getMember(ticketCreator);

                            if (member != null) {
                                channel.getManager()
                                        .putPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
                                        .queue();
                                String currentName = channel.getName();
                                if (currentName.startsWith("ticket-closed-")) {
                                    String ticketNumber = currentName.replace("ticket-closed-", "");
                                    channel.getManager().setName("ticket-" + ticketNumber).queue();
                                }
                                EmbedBuilder openedByEmbed = new EmbedBuilder()
                                        .setColor(Color.GREEN)
                                        .setTitle("🔓 Ticket Reopened")
                                        .setDescription("Ticket reopened by " + event.getUser().getAsMention());

                                event.replyEmbeds(openedByEmbed.build()).queue();
                            } else {
                                event.reply("❌ Couldn’t find the ticket creator.").setEphemeral(true).queue();
                            }
                        } else {
                            event.reply("❌ No user mentioned in the ticket.").setEphemeral(true).queue();
                        }
                    }
                });
            }
            case "tickets:delete" -> {
                TextChannel channel = (TextChannel) event.getChannel();
                channel.delete().queue();
            }

            case "application:agree" -> {
                event.reply("📨 Check your DMs to begin your application.").setEphemeral(true).queue();

                event.getUser().openPrivateChannel().queue(
                        channel -> {
                            List<String> questions = plugin.getApplicationQuestions();

                            applicationProgress.put(userId, 0);
                            applicationAnswers.put(userId, new ArrayList<>());

                            channel.sendMessage("Welcome to the application process!").queue();
                            channel.sendMessage("**Question 1:** " + questions.getFirst()).queue();
                        },
                        failure -> event.getHook().sendMessage("❌ Couldn't DM you. Please enable DMs.").setEphemeral(true).queue()
                );
            }

            case "application:cancel" -> {
                event.deferEdit().queue();
                event.getMessage().delete().queue();
            }
        }
    }
}
