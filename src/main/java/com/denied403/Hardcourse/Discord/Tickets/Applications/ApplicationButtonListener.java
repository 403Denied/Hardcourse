package com.denied403.Hardcourse.Discord.Tickets.Applications;

import com.denied403.Hardcourse.Discord.Tickets.PanelButtonListener;
import com.denied403.Hardcourse.Hardcourse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ApplicationButtonListener extends ListenerAdapter {

    private final Hardcourse plugin;
    private static final String LOG_CHANNEL_ID = "1443274311652868221";

    public ApplicationButtonListener(Hardcourse plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String userId = event.getUser().getId();
        String id = event.getComponentId();
        switch (id) {
            case "application:submit" -> {
                if (!PanelButtonListener.applicationAnswers.containsKey(userId)) { event.reply("⚠️ You no longer have an active application.").setEphemeral(true).queue(); return;}
                List<String> questions = plugin.getApplicationQuestions();
                List<String> answers = PanelButtonListener.applicationAnswers.get(userId);

                List<MessageEmbed> finalEmbeds = new ArrayList<>();

                EmbedBuilder current = new EmbedBuilder()
                        .setTitle("📥 New Application Submission")
                        .setColor(Color.CYAN)
                        .setFooter("User ID: " + userId, event.getUser().getEffectiveAvatarUrl())
                        .setAuthor(event.getUser().getName(), null, event.getUser().getEffectiveAvatarUrl());

                int fieldCount = 0;
                int currentEmbedChars = current.length();

                for (int i = 0; i < questions.size(); i++) {

                    String question = "Q" + (i + 1) + ": " + questions.get(i);
                    String answer = i < answers.size() ? answers.get(i) : "*No answer*";

                    List<String> chunks = new ArrayList<>();
                    while (answer.length() > 1024) {
                        chunks.add(answer.substring(0, 1024));
                        answer = answer.substring(1024);
                    }
                    chunks.add(answer);

                    for (int chunkIndex = 0; chunkIndex < chunks.size(); chunkIndex++) {

                        String fieldName = chunkIndex == 0 ? question : question + " (cont.)";
                        String fieldValue = chunks.get(chunkIndex);

                        int fieldLen = fieldName.length() + fieldValue.length();

                        if (fieldCount >= 25 || currentEmbedChars + fieldLen >= 5900) {
                            finalEmbeds.add(current.build());

                            current = new EmbedBuilder()
                                    .setColor(Color.CYAN)
                                    .setFooter("User ID: " + userId, event.getUser().getEffectiveAvatarUrl());

                            fieldCount = 0;
                            currentEmbedChars = current.length();
                        }

                        current.addField(fieldName, fieldValue, false);
                        fieldCount++;
                        currentEmbedChars += fieldLen;
                    }
                }

                finalEmbeds.add(current.build());

                ForumChannel logChannel = event.getJDA().getForumChannelById(LOG_CHANNEL_ID);

                if (logChannel == null) {
                    event.reply("⚠️ Could not find log channel!").setEphemeral(true).queue();
                    return;
                }

                String threadName = "application-" + event.getUser().getName() + "-" + (int)(Math.random() * 9999);

                MessageCreateData initialPost = MessageCreateData.fromContent(
                        "📥 **New Application Submitted by <@" + event.getUser().getId() + ">**"
                );
                ForumTag pendingTag = logChannel.getAvailableTags().stream().filter(tag -> tag.getName().equalsIgnoreCase("Under Review")).findFirst().orElse(null);
                List<ForumTag> appliedTags = pendingTag != null ? List.of(pendingTag) : List.of();

                logChannel.createForumPost(threadName, initialPost).setTags(appliedTags).queue(forumPost -> {
                    Message message = forumPost.getMessage();
                    message.addReaction(Emoji.fromFormatted("<:upvote:1443360258558005433>")).queue();
                    message.addReaction(Emoji.fromFormatted("<:downvote:1443360277029585007>")).queue();
                    ThreadChannel thread = forumPost.getThreadChannel();
                    for (int i = 0; i < finalEmbeds.size(); i++) {
                        MessageEmbed embed = finalEmbeds.get(i);

                        if (i == finalEmbeds.size() - 1) {
                            thread.sendMessageEmbeds(embed)
                                    .setActionRow(
                                            Button.success("application_accept:" + userId, "✅ Accept"),
                                            Button.danger("application_deny:" + userId, "❌ Deny")
                                    )
                                    .queue();
                        } else {
                            thread.sendMessageEmbeds(embed).queue();
                        }
                    }
                });

                event.reply("✅ Your application has been submitted! Expect a response within 3 days from this bot.").setEphemeral(true).queue();
                event.getMessage().editMessageComponents(
                        ActionRow.of(
                                Button.success("application:submit", "✅ Submit").asDisabled(),
                                Button.secondary("application:edit", "✏️ Edit A Response").asDisabled(),
                                Button.danger("application:cancel", "❌ Cancel").asDisabled()
                        )
                ).queue();

                PanelButtonListener.applicationAnswers.remove(userId);
                PanelButtonListener.applicationProgress.remove(userId);
            }

            case "application:edit" -> {
                if (!PanelButtonListener.applicationAnswers.containsKey(userId)) { event.reply("⚠️ You no longer have an active application.").setEphemeral(true).queue(); return;}
                event.reply("✏️ Please type `edit <question number>` to re-answer a specific question.\n" +
                                "For example: `edit 2` to re-answer question 2.").setEphemeral(true).queue();
                event.getMessage().editMessageComponents(
                        ActionRow.of(
                                Button.success("application:submit", "✅ Submit").asDisabled(),
                                Button.secondary("application:edit", "✏️ Edit A Response").asDisabled(),
                                Button.danger("application:cancel", "❌ Cancel").asDisabled()
                        )
                ).queue();
            }
            case "application:cancel" -> {
                if (!PanelButtonListener.applicationAnswers.containsKey(userId)) { event.reply("⚠️ You no longer have an active application.").setEphemeral(true).queue(); return;}
                event.reply("❌ Your application has been cancelled. You can start a new one any time.").setEphemeral(true).queue();
                event.getMessage().editMessageComponents(
                        ActionRow.of(
                                Button.success("application:submit", "✅ Submit").asDisabled(),
                                Button.secondary("application:edit", "✏️ Edit A Response").asDisabled(),
                                Button.danger("application:cancel", "❌ Cancel").asDisabled()
                        )
                ).queue();

                PanelButtonListener.applicationAnswers.remove(userId);
                PanelButtonListener.applicationProgress.remove(userId);
            }
            default -> {
                if(id.startsWith("application_accept:")){
                    String requiredRoleId = "719169740903415847";
                    boolean hasPermission = event.getMember() != null &&
                            event.getMember().getRoles().stream()
                                    .anyMatch(role -> role.getId().equals(requiredRoleId));
                    if (!hasPermission) {
                        event.reply("⛔ You do not have permission to review applications.").setEphemeral(true).queue();
                        return;
                    }
                    String targetId = event.getComponentId().split(":")[1];
                    event.reply("Are you sure you want to accept <@" + targetId + ">'s application?")
                            .addActionRow(
                                    Button.success("application_accept_confirm:" + targetId, "✅ Confirm"),
                                    Button.danger("application_accept_cancel:" + targetId, "❌ Cancel")
                            )
                            .setEphemeral(true)
                            .queue();
                }
                if(id.startsWith("application_deny:")){
                    String requiredRoleId = "719169740903415847";
                    boolean hasPermission = event.getMember() != null &&
                            event.getMember().getRoles().stream()
                                    .anyMatch(role -> role.getId().equals(requiredRoleId));
                    if (!hasPermission) {
                        event.reply("⛔ You do not have permission to review applications.").setEphemeral(true).queue();
                        return;
                    }
                    String targetId = event.getComponentId().split(":")[1];
                    Modal modal = Modal.create("application_deny_modal:" + targetId, "Deny Application")
                            .addActionRow(TextInput.create("reason", "Denial Reason", TextInputStyle.PARAGRAPH)
                                    .setRequired(true)
                                    .build())
                            .build();
                    event.replyModal(modal).queue();
                }
                if (id.startsWith("application_accept_cancel:")) {
                    String requiredRoleId = "719169740903415847";
                    boolean hasPermission = event.getMember() != null &&
                            event.getMember().getRoles().stream()
                                    .anyMatch(role -> role.getId().equals(requiredRoleId));
                    if (!hasPermission) {
                        event.reply("⛔ You do not have permission to review applications.").setEphemeral(true).queue();
                        return;
                    }
                    event.editMessage("❌ Action canceled.").setComponents().queue();
                }
                if(id.startsWith("application_accept_confirm:")){
                    String requiredRoleId = "719169740903415847";
                    boolean hasPermission = event.getMember() != null &&
                            event.getMember().getRoles().stream()
                                    .anyMatch(role -> role.getId().equals(requiredRoleId));
                    if (!hasPermission) {
                        event.reply("⛔ You do not have permission to review applications.").setEphemeral(true).queue();
                        return;
                    }
                    String targetId = event.getComponentId().split(":")[1];
                    ThreadChannel thread = event.getChannel().asThreadChannel();
                    ForumTag acceptedTag = thread.getParentChannel().asForumChannel().getAvailableTagById("1443359891304616008");
                    thread.getManager().setAppliedTags(acceptedTag).queue();

                    event.reply("✅ Application accepted. The user has been notified.").setEphemeral(true).queue();
                    EmbedBuilder accepted = new EmbedBuilder()
                            .setTitle("Application Accepted")
                            .setColor(Color.GREEN)
                            .setThumbnail(event.getGuild().getIconUrl())
                            .setDescription("<@" + targetId + ">'s application was accepted by <@" + event.getUser().getId() + ">, and they have been sent an invite to the staff discord!")
                            .setAuthor("hardcourse.minehut.gg", null, event.getUser().getAvatarUrl())
                            .setTimestamp(Instant.now());
                    event.getChannel().sendMessageEmbeds(accepted.build()).queue();
                    disableApplicationButtons(event, targetId);
                    event.getMessage().delete().queue();

                    event.getJDA().getTextChannelById("1443272708782817280").createInvite()
                            .setMaxAge(0)
                            .setMaxUses(1)
                            .queue(invite -> {
                                String inviteUrl = invite.getUrl();
                                event.getJDA().retrieveUserById(targetId).queue(user -> user.openPrivateChannel().queue(pc -> pc.sendMessage("🎉 **Your application has been accepted!**\n\n"
                                                + "📩 **Join the staff discord here:**\n"
                                                + inviteUrl).queue()));

                            });
                }
            }
        }
    }
    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().startsWith("application_deny_modal:")) {
            String requiredRoleId = "719169740903415847";
            boolean hasPermission = event.getMember() != null &&
                    event.getMember().getRoles().stream()
                            .anyMatch(role -> role.getId().equals(requiredRoleId));
            if (!hasPermission) {
                event.reply("⛔ You do not have permission to review applications.").setEphemeral(true).queue();
                return;
            }
            String targetId = event.getModalId().split(":")[1];
            String reason = event.getValue("reason").getAsString();
            ThreadChannel thread = event.getChannel().asThreadChannel();
            ForumTag deniedTag = thread.getParentChannel().asForumChannel().getAvailableTagById("1443359902541152377");
            thread.getManager().setAppliedTags(deniedTag).queue();
            event.reply("❌ Application denied. The user has been notified.").setEphemeral(true).queue();
            EmbedBuilder denied = new EmbedBuilder()
                    .setTitle("Application Denied")
                    .setColor(Color.RED)
                    .setThumbnail(event.getGuild().getIconUrl())
                    .setDescription("<@" + targetId + ">'s application was denied by <@" + event.getUser().getId() + "> due to: `" + reason + "`")
                    .setAuthor("hardcourse.minehut.gg", null, event.getUser().getAvatarUrl())
                    .setTimestamp(Instant.now());
            event.getChannel().sendMessageEmbeds(denied.build()).queue();
            disableApplicationButtons(event, targetId);

            event.getJDA().retrieveUserById(targetId).queue(user -> user.openPrivateChannel().queue(pc -> pc.sendMessage("⚠️ **Your application has been denied.**\n\n"
                            + "**Reason:**\n" + reason).queue()));
        }
    }
    private void disableApplicationButtons(GenericInteractionCreateEvent event, String targetId) {
        event.getMessageChannel().getIterableHistory().takeAsync(500).thenAccept(messages ->
                messages.forEach(msg ->
                        msg.getButtons().forEach(button -> {
                            String id = button.getId();
                            if (id != null && (
                                    id.equals("application_accept:" + targetId)
                                            || id.equals("application_deny:" + targetId)
                            )) {
                                msg.editMessageComponents(
                                        ActionRow.of(
                                                Button.success("application_accept:" + targetId, "✅ Accept").asDisabled(),
                                                Button.danger("application_deny:" + targetId, "❌ Deny").asDisabled()
                                        )
                                ).queue();
                            }
                        })
                )
        );
    }


}
