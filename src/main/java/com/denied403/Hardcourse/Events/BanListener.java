package com.denied403.Hardcourse.Events;

import com.denied403.Hardcourse.Utils.CheckpointDatabase;
import com.transfemme.dev.core403.Core403;
import com.transfemme.dev.core403.Punishments.Api.CustomEvents.IPBanEvent;
import com.transfemme.dev.core403.Punishments.Api.CustomEvents.NameBanEvent;
import com.transfemme.dev.core403.Punishments.Api.CustomEvents.PunishmentEvent;
import com.transfemme.dev.core403.Punishments.Api.CustomEvents.RevertEvent;
import com.transfemme.dev.core403.Punishments.Api.CustomEvents.PunishmentEditEvent;
import com.transfemme.dev.core403.Punishments.Database.PunishmentDatabase;
import com.transfemme.dev.core403.Punishments.Events.onChatEdit;
import com.transfemme.dev.core403.Punishments.Utils.PunishmentDurationParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.*;
import static com.denied403.Hardcourse.Hardcourse.*;
import static com.denied403.Hardcourse.Utils.ColorUtil.Colorize;
import static com.denied403.Hardcourse.Utils.Luckperms.hasLuckPermsPermission;
import static org.bukkit.Bukkit.getServer;

public class BanListener extends ListenerAdapter implements Listener {
    private static CheckpointDatabase database;
    private static PunishmentDatabase punishmentDatabase;

    public static void initialize(CheckpointDatabase db, PunishmentDatabase pDb) {
        database = db;
        punishmentDatabase = pDb;
    }

    public static void runBanCleanup(String playerName) {
        ThreadChannel channel = jda.getThreadChannelById(Objects.requireNonNull(plugin.getConfig().getString("Anticheat-Channel-Id")));
        if (channel == null) return;

        channel.getHistory().retrievePast(100).queue(messages -> {
            for (Message msg : messages) {
                List<Button> buttons = msg.getButtons();
                boolean changed = false;

                List<Button> updated = new ArrayList<>();
                for (Button b : buttons) {
                    if (b.getId() != null && b.getId().equalsIgnoreCase("ban:" + playerName)) {
                        updated.add(Button.success(b.getId(), "✅ Banned").asDisabled());
                        changed = true;
                    } else {
                        updated.add(b);
                    }
                }
                if (changed) {
                    channel.editMessageComponentsById(msg.getId(), ActionRow.of(updated)).queue();
                }
            }
        });
    }
    @EventHandler
    public void onBan(PunishmentEvent event) {
        String playerName = Bukkit.getOfflinePlayer(event.getTargetUUID()).getName();
        String reason = event.getReason();
        if (event.getTypeOfPunishment().startsWith("ban")) {
            if(reason.equalsIgnoreCase("Unfair Advantage")) {
                if(!(event.getStaff().equals("CONSOLE"))) {
                    if (Bukkit.getOfflinePlayer(event.getTargetUUID()).getStatistic(Statistic.PLAY_ONE_MINUTE) >= 72000) {
                        if(Bukkit.getOfflinePlayer(event.getStaff()).isOnline()) {
                            getServer().getPlayer(event.getStaff()).sendMessage(Colorize("&c&lHARDCOURSE &rThis player has more than 1 hour of playtime. Remember to provide evidence in &c#punishment-proof&f."));
                        }
                    }
                }
                if(isDiscordEnabled()) {
                    runBanCleanup(playerName);
                }
            }
        }
        if(isDiscordEnabled()) {
            String punishment = "";
            if(event.getTypeOfPunishment().equalsIgnoreCase("banned")){punishment = "Ban";}
            if(event.getTypeOfPunishment().equalsIgnoreCase("muted")){punishment = "Mute";}
            if(event.getTypeOfPunishment().equalsIgnoreCase("warned")){punishment = "Warn";}
            String durationString;
            long durationMs = event.getDuration();
            if(durationMs == -1){durationString = "Never";} else {durationString = "<t:" + (Instant.now().toEpochMilli() + durationMs) / 1000L + ":R>";}

            EmbedBuilder punishmentEmbed = new EmbedBuilder()
                    .setTitle(punishment + " Issued" + (isDev() ? " (Dev)" : ""))
                    .setDescription("**ID:** " + event.getPunishmentId() + "\n**Staff:** `" + event.getStaff() + "`\n**Target:** `" + playerName + "`\n**Reason:** " + reason + "\n**Expires:** " + durationString + "\n**Note:** " + (event.getNotes() == null ? "None" : event.getNotes()))
                    .setThumbnail("https://mc-heads.net/avatar/" + event.getTargetUUID() + ".png")
                    .setColor(Color.RED);
            Button revert = Button.danger("punishment_revert:" + event.getPunishmentId(), "Revert");
            Button note = Button.primary("punishment_addnote:" + event.getPunishmentId(), "Add Note");
            Button duration = Button.success("punishment_modify:" + event.getPunishmentId(), "Change Duration");
            punishmentChannel.sendMessageEmbeds(punishmentEmbed.build()).setActionRow(revert, note, duration).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(!event.getChannel().getId().equals(plugin.getConfig().getString("Punishment-Channel-Id"))) return;
        String linkedUuidString = database.getUUIDFromDiscord(event.getMember().getId());
        UUID linkedUUID;
        if (linkedUuidString != null) {
            linkedUUID = UUID.fromString(linkedUuidString);
        } else {
            EmbedBuilder embed = new EmbedBuilder().setColor(Color.RED).setDescription("❌ You must be *linked* to use this!");
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        if(event.getButton().getId().startsWith("punishment_revert:")) {
            String punishmentId = event.getButton().getId().split(":")[1];

            if(!hasLuckPermsPermission(linkedUUID, "core403.punish.revert")){
                event.reply("❌ You do not have permission to do that!").setEphemeral(true).queue();
                return;
            }
            if (checkReverted(event, punishmentId)) return;
            Modal modal = Modal.create("confirm_revert:" + punishmentId, "Revert Punishment")
                    .addActionRow(
                            TextInput.create("reason", "Reason", TextInputStyle.PARAGRAPH)
                                    .setPlaceholder("The reason to revert this punishment")
                                    .setRequired(true)
                                    .build()
                    )
                    .build();

            event.replyModal(modal).queue();
        }
        if(event.getButton().getId().startsWith("punishment_addnote:")) {
            String punishmentId = event.getButton().getId().split(":")[1];

            if(!hasLuckPermsPermission(linkedUUID, "core403.punish.use")){
                event.reply("❌ You do not have permission to do that!").setEphemeral(true).queue();
                return;
            }
            if (checkReverted(event, punishmentId)) return;
            Modal modal = Modal.create("add_note:" + punishmentId, "Add Note")
                    .addActionRow(
                            TextInput.create("note", "Note", TextInputStyle.PARAGRAPH)
                                    .setPlaceholder("The note to add to this punishment")
                                    .setRequired(true)
                                    .build()
                    )
                    .build();

            event.replyModal(modal).queue();
        }
        if(event.getButton().getId().startsWith("punishment_modify:")) {
            String punishmentId = event.getButton().getId().split(":")[1];
            if(!hasLuckPermsPermission(linkedUUID, "core403.punish.edit")){
                event.reply("❌ You do not have permission to do that!").setEphemeral(true).queue();
                return;
            }
            if (checkReverted(event, punishmentId)) return;
            Modal modal = Modal.create("modify:" + punishmentId, "Change Duration")
                    .addActionRow(
                            TextInput.create("duration", "Duration", TextInputStyle.SHORT)
                                    .setPlaceholder("The new duration for this punishment (ex. 6h, 3d, 1w, perm, etc)")
                                    .setRequired(true)
                                    .build()
                    )
                    .addActionRow(
                            TextInput.create("reason", "Reason", TextInputStyle.SHORT)
                                    .setPlaceholder("The reason to change this punishment's duration.")
                                    .setRequired(true)
                                    .build()
                    )
                    .build();
            event.replyModal(modal).queue();
        }
    }

    private boolean checkReverted(ButtonInteractionEvent event, String punishmentId) {
        if(punishmentDatabase.isReverted(punishmentId)){
            event.deferEdit().queue();
            event.getMessage().editMessageComponents(
                    ActionRow.of(
                            Button.danger("button:disabled", "Revert").asDisabled(),
                            Button.primary("button:disabled1", "Add Note").asDisabled(),
                            Button.success("button:disabled2", "Change Duration").asDisabled()
                    )
            ).queue();
            event.getHook().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription("❌ This punishment has already been reverted!").build()).setEphemeral(true).queue();
            return true;
        }
        return false;
    }
    private boolean checkRevertedModal(ModalInteractionEvent event, String punishmentId) {
        event.getMessage().editMessageComponents(
                ActionRow.of(
                        Button.danger("button:disabled", "Revert").asDisabled(),
                        Button.primary("button:disabled1", "Add Note").asDisabled(),
                        Button.success("button:disabled2", "Change Duration").asDisabled()
                )
        ).queue();
        if(punishmentDatabase.isReverted(punishmentId)){
            event.deferEdit().queue();
            event.getHook().sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription("❌ This punishment has already been reverted!").build()).setEphemeral(true).queue();
            return true;
        }
        return false;
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event){
        if(event.getModalId().startsWith("confirm_revert:")) {
            String punishmentId = event.getModalId().split(":")[1];
            if (checkRevertedModal(event, punishmentId)) return;
            String note = event.getValue("reason").getAsString();
            String linkedUuidString = database.getUUIDFromDiscord(event.getMember().getId());
            UUID linkedUUID = UUID.fromString(linkedUuidString);
            com.transfemme.dev.core403.Punishments.Events.onChatRevert.revertPunishment(punishmentId, linkedUUID, System.currentTimeMillis(), note, Core403.getPunishmentDatabase());
            EmbedBuilder punishmentEmbed = new EmbedBuilder().setColor(Color.GREEN).setDescription("✅ Punishment `" + punishmentId + "` successfully reverted.");
            event.replyEmbeds(punishmentEmbed.build()).setEphemeral(true).queue();
            return;
        }
        if(event.getModalId().startsWith("add_note:")) {
            String punishmentId = event.getModalId().split(":")[1];
            if (checkRevertedModal(event, punishmentId)) return;
            String note = event.getValue("note").getAsString();
            if(!Core403.getPunishmentDatabase().hasNotes(punishmentId)) {
                Core403.getPunishmentDatabase().addNotes(punishmentId, note);
            } else {
                Core403.getPunishmentDatabase().addGeneralNotes(punishmentId, note);
            }
            EmbedBuilder noteEmbed = new EmbedBuilder().setColor(Color.GREEN).setDescription("✅ Successfully added note to punishment `" + punishmentId + "`.");
            event.replyEmbeds(noteEmbed.build()).setEphemeral(true).queue();
            return;
        }
        if(event.getModalId().startsWith("modify:")) {
            String punishmentId = event.getModalId().split(":")[1];
            if (checkRevertedModal(event, punishmentId)) return;
            String discordId = event.getMember().getId();
            UUID linkedUuid = UUID.fromString(database.getUUIDFromDiscord(discordId));
            String duration =  event.getValue("duration").getAsString();
            try {
                PunishmentDurationParser.parse(duration);
            } catch (IllegalArgumentException e){
                EmbedBuilder failureEmbed = new EmbedBuilder().setColor(Color.RED).setDescription("❌ Invalid duration: `" + duration + "`.");
                event.replyEmbeds(failureEmbed.build()).setEphemeral(true).queue();
                return;
            }
            String reason = event.getValue("reason").getAsString();
            onChatEdit.editPunishment(punishmentId, linkedUuid, duration, reason, Core403.getPunishmentDatabase());
            EmbedBuilder noteEmbed = new EmbedBuilder().setColor(Color.GREEN).setDescription("✅ Successfully updated duration of punishment `" + punishmentId + "` to `" +  duration + "`.");
            event.replyEmbeds(noteEmbed.build()).setEphemeral(true).queue();
        }
    }

    @EventHandler
    public void onRevert(RevertEvent event) {
        String playerName = Bukkit.getOfflinePlayer(event.getTargetUUID()).getName();
        String staffName = event.getStaff();
        String reason = event.getReason();
        String punishmentType = event.getType().toLowerCase();
        if(isDiscordEnabled()) {
            punishmentChannel.sendMessage("`" + staffName + "` reverted a " + punishmentType + " from `" + playerName + "` for `" + reason + "`").queue();
        }
    }
    @EventHandler
    public void onNameBan(NameBanEvent event) {
        String playerName = Bukkit.getOfflinePlayer(event.getTargetUUID()).getName();
        String staffName = event.getStaff();
        if(isDiscordEnabled()) {
            EmbedBuilder punishmentEmbed = new EmbedBuilder()
                    .setTitle("Name Ban Issued" + (isDev() ? " (Dev)" : ""))
                    .setDescription("**ID:** " + event.getPunishmentId() + "\n**Staff:** " + staffName + "\n**Target:** " + playerName)
                    .setThumbnail("https://mc-heads.net/avatar/" + event.getTargetUUID() + ".png")
                    .setColor(Color.RED);
            punishmentChannel.sendMessageEmbeds(punishmentEmbed.build()).queue();
        }
    }
    @EventHandler
    public void onIpBan(IPBanEvent event){
        String playerName = Bukkit.getOfflinePlayer(event.getTargetUUID()).getName();
        String staffName = event.getStaff();
        if(isDiscordEnabled()) {
            EmbedBuilder punishmentEmbed = new EmbedBuilder()
                    .setTitle("IP Ban Issued" + (isDev() ? " (Dev)" : ""))
                    .setDescription("**ID:** " + event.getPunishmentId() + "\n**Staff:** " + staffName + "\n**Target:** " + playerName)
                    .setThumbnail("https://mc-heads.net/avatar/" + event.getTargetUUID() + ".png")
                    .setColor(Color.RED);
            punishmentChannel.sendMessageEmbeds(punishmentEmbed.build()).queue();
        }
    }
    @EventHandler
    public void onEdit(PunishmentEditEvent event){
        String playerName = Bukkit.getOfflinePlayer(event.getTargetUUID()).getName();
        String staffName = event.getStaff();
        String id = event.getPunishmentID();
        String reason = event.getReason();
        String punishmentReason = event.getPunishmentReason();
        String punishmentType = event.getPunishmentType().toLowerCase();
        if(isDiscordEnabled()) {
            punishmentChannel.sendMessage("`" + staffName + "` edited a " + punishmentType + " from `" + playerName + "` for `" + punishmentReason + "`").queue();
        }
    }
}
