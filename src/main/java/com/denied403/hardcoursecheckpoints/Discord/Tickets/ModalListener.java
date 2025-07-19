package com.denied403.hardcoursecheckpoints.Discord.Tickets;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.List;

public class ModalListener extends ListenerAdapter {

    private static final long TICKET_CATEGORY_ID = 1391982676294242427L;
    private static final long STAFF_ROLE_ID = 807859021431439390L;

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        String modalId = event.getModalId();

        switch (modalId) {
            case "modal:ticket" -> {
                String reason = event.getValue("reason").getAsString();
                Member member = event.getMember();
                if (member == null) {
                    event.reply("⚠️ Could not get your member info.").setEphemeral(true).queue();
                    return;
                }

                event.deferReply(true).queue();

                Category category = event.getGuild().getCategoryById(TICKET_CATEGORY_ID);
                if (category == null || category.getType() != ChannelType.CATEGORY) {
                    event.getHook().sendMessage("⚠️ Ticket category not found or invalid.").queue();
                    return;
                }

                List<TextChannel> channels = event.getGuild().getTextChannels();
                int nextTicketNumber;
                if (!channels.isEmpty()) {
                    nextTicketNumber = channels.stream()
                            .filter(ch -> {
                                Category parent = ch.getParentCategory();
                                return parent != null && parent.getIdLong() == TICKET_CATEGORY_ID;
                            })
                            .map(TextChannel::getName)
                            .filter(name -> name.startsWith("ticket-"))
                            .map(name -> name.substring("ticket-".length()))
                            .filter(numStr -> numStr.matches("\\d+"))
                            .mapToInt(Integer::parseInt)
                            .max()
                            .orElse(0) + 1;
                } else {
                    nextTicketNumber = 1;
                }

                String channelName = "ticket-" + nextTicketNumber;

                ChannelAction<TextChannel> createChannel = event.getGuild().createTextChannel(channelName)
                        .setParent(category)
                        .addPermissionOverride(event.getGuild().getPublicRole(), 0, Permission.VIEW_CHANNEL.getRawValue()) // deny @everyone
                        .addPermissionOverride(member, Permission.VIEW_CHANNEL.getRawValue(), 0)
                        .addPermissionOverride(event.getGuild().getRoleById(STAFF_ROLE_ID), Permission.VIEW_CHANNEL.getRawValue(), 0); // allow staff role

                createChannel.queue(textChannel -> {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("New Ticket #" + nextTicketNumber)
                            .setColor(Color.GREEN)
                            .addField("User", member.getEffectiveName() + " (" + member.getId() + ")", false)
                            .addField("Reason", reason, false)
                            .setTimestamp(event.getTimeCreated());

                    textChannel.sendMessageEmbeds(embed.build()).setContent(member.getAsMention()).addActionRow(Button.secondary("tickets:close", "🔒 Close")).queue();

                    event.getHook().sendMessage("🎟️ Your ticket has been created: " + textChannel.getAsMention())
                            .setEphemeral(true).queue();
                }, failure -> event.getHook().sendMessage("❌ Failed to create ticket channel. Please contact staff.").queue());
            }

            case "modal:appeal" -> {
                String ign = event.getValue("ign").getAsString();
                String type = event.getValue("type").getAsString();
                String punishmentReason = event.getValue("punishment").getAsString();
                String duration = event.getValue("duration").getAsString();

                event.reply("✅ Your appeal has been submitted!").setEphemeral(true).queue();
            }
        }
    }
}
