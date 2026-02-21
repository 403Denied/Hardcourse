package com.denied403.Hardcourse.Events;

import com.transfemme.dev.core403.Punishments.Api.PunishmentEvent;
import com.transfemme.dev.core403.Punishments.Api.NameBanEvent;
import com.transfemme.dev.core403.Punishments.Api.RevertEvent;
import com.transfemme.dev.core403.Punishments.Api.PunishmentEditEvent;
import com.transfemme.dev.core403.Punishments.Api.IPBanEvent;
import com.transfemme.dev.core403.Punishments.Utils.PunishmentDurationParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.components.label.Label;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.*;
import static com.denied403.Hardcourse.Hardcourse.*;
import static com.denied403.Hardcourse.Utils.Luckperms.hasLuckPermsPermission;
import static com.transfemme.dev.core403.Punishments.Events.onChatEdit.editPunishment;
import static com.transfemme.dev.core403.Punishments.Events.onChatRevert.revertPunishment;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;
import static org.bukkit.Bukkit.getServer;

public class PunishmentListener extends ListenerAdapter implements Listener {
    public static void runBanCleanup(String playerName) {
        hacksChannel.getHistory().retrievePast(100).queue(messages -> {
            for (Message msg : messages) {
                boolean changed = false;

                List<MessageTopLevelComponentUnion> componentUnions = msg.getComponents();
                List<ActionRow> updatedRows = new ArrayList<>();

                for (MessageTopLevelComponentUnion union : componentUnions) {
                    if (union instanceof ActionRow) {
                        ActionRow row = union.asActionRow();
                        List<Button> buttons = new ArrayList<>();
                        for (Button b : row.getButtons()) {
                            if (b.getCustomId() != null && b.getCustomId().equalsIgnoreCase("ban:" + playerName)) {
                                buttons.add(Button.success(b.getCustomId(), "✅ Banned").asDisabled());
                                changed = true;
                            } else {
                                buttons.add(b);
                            }
                        }
                        updatedRows.add(ActionRow.of(buttons));
                    }
                    if (changed) {
                        msg.editMessageComponents(updatedRows).queue();
                    }
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
                            getServer().getPlayer(event.getStaff()).sendMessage(Colorize("<prefix>This player has more than 1 hour of playtime. Remember to provide evidence in &c#punishment-proof&f."));
                        }
                    }
                }
                if(DiscordEnabled) {
                    runBanCleanup(playerName);
                }
            }
        }
        if(DiscordEnabled) {
            String punishment = "";
            if(event.getTypeOfPunishment().equalsIgnoreCase("banned")){punishment = "Ban";}
            if(event.getTypeOfPunishment().equalsIgnoreCase("muted")){punishment = "Mute";}
            if(event.getTypeOfPunishment().equalsIgnoreCase("warned")){punishment = "Warn";}
            String durationString;
            long durationMs = event.getDuration();
            if(durationMs == -1){durationString = "Never";} else {durationString = "<t:" + (Instant.now().toEpochMilli() + durationMs) / 1000L + ":R>";}

            EmbedBuilder punishmentEmbed = new EmbedBuilder()
                    .setTitle(punishment + " Issued")
                    .setDescription("**ID:** " + event.getPunishmentId() + "\n**Staff:** `" + event.getStaff() + "`\n**Target:** `" + playerName + "`\n**Reason:** " + reason + "\n**Expires:** " + durationString + "\n**Note:** " + (event.getNotes() == null ? "None" : event.getNotes()))
                    .setThumbnail("https://mc-heads.net/avatar/" + event.getTargetUUID() + ".png")
                    .setColor(Color.RED);
            Button revert = Button.danger("punishment_revert:" + event.getPunishmentId(), "Revert");
            Button note = Button.primary("punishment_addNote:" + event.getPunishmentId(), "Add Note");
            Button duration = Button.success("punishment_modify:" + event.getPunishmentId(), "Change Duration");
            punishmentChannel.sendMessageEmbeds(punishmentEmbed.build()).setComponents(ActionRow.of(revert, note, duration)).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(!event.getChannel().getId().equals(plugin.getConfig().getString("Punishment-Channel-Id"))) return;
        String linkedUuidString = checkpointDatabase.getUUIDFromDiscord(event.getMember().getId());
        UUID linkedUUID;
        if (linkedUuidString != null) {
            linkedUUID = UUID.fromString(linkedUuidString);
        } else {
            EmbedBuilder embed = new EmbedBuilder().setColor(Color.RED).setDescription("❌ You must be *linked* to use this!");
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        if(event.getButton().getCustomId().startsWith("punishment_revert:")) {
            String punishmentId = event.getButton().getCustomId().split(":")[1];

            if(!hasLuckPermsPermission(linkedUUID, "core403.punish.revert")){
                event.reply("❌ You do not have permission to do that!").setEphemeral(true).queue();
                return;
            }
            if (checkReverted(event, punishmentId)) return;
            TextInput reasonInput = TextInput.create("reason", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("The reason to revert this punishment")
                    .setRequired(true)
                    .build();

            Modal modal = Modal.create("confirm_revert:" + punishmentId, "Revert Punishment")
                    // Use addComponents with the Label wrapper to satisfy the builder
                    .addComponents(Label.of("Reversion Reason", reasonInput))
                    .build();

            event.replyModal(modal).queue();
        }
        if(event.getButton().getCustomId().startsWith("punishment_addNote:")) {
            String punishmentId = event.getButton().getCustomId().split(":")[1];

            if(!hasLuckPermsPermission(linkedUUID, "core403.punish.use")){
                event.reply("❌ You do not have permission to do that!").setEphemeral(true).queue();
                return;
            }
            if (checkReverted(event, punishmentId)) return;
            TextInput noteInput = TextInput.create("note", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("The note to add to this punishment")
                    .setRequired(true)
                    .build();

            Modal modal = Modal.create("add_note:" + punishmentId, "Add Note")
                    .addComponents(Label.of("Note", noteInput))
                    .build();

            event.replyModal(modal).queue();
        }
        if(event.getButton().getCustomId().startsWith("punishment_modify:")) {
            String punishmentId = event.getButton().getCustomId().split(":")[1];
            if(!hasLuckPermsPermission(linkedUUID, "core403.punish.edit")){
                event.reply("❌ You do not have permission to do that!").setEphemeral(true).queue();
                return;
            }
            if (checkReverted(event, punishmentId)) return;
            TextInput durationInput = TextInput.create("duration", TextInputStyle.SHORT)
                    .setPlaceholder("Ex: 6h, 3d, 1w, perm")
                    .setRequired(true)
                    .build();

            TextInput reasonInput = TextInput.create("reason", TextInputStyle.SHORT)
                    .setPlaceholder("Why is the duration being changed?")
                    .setRequired(true)
                    .build();

            Modal modal = Modal.create("modify:" + punishmentId, "Change Duration")
                    .addComponents(
                            Label.of("New Duration", durationInput),
                            Label.of("Change Reason", reasonInput)
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

    @Override
    public void onModalInteraction(ModalInteractionEvent event){
        if(event.getModalId().startsWith("confirm_revert:")) {
            String punishmentId = event.getModalId().split(":")[1];
            if (checkRevertedModal(event, punishmentId)) return;
            String note = event.getValue("reason").getAsString();
            String linkedUuidString = checkpointDatabase.getUUIDFromDiscord(event.getMember().getId());
            UUID linkedUUID = UUID.fromString(linkedUuidString);
            if(!hasLuckPermsPermission(linkedUUID, "core403.punish.revert")){
                event.reply("❌ You do not have permission to do that!").setEphemeral(true).queue();
                return;
            }
            revertPunishment(punishmentId, linkedUUID, System.currentTimeMillis(), note);
            EmbedBuilder punishmentEmbed = new EmbedBuilder().setColor(Color.GREEN).setDescription("✅ Punishment `" + punishmentId + "` successfully reverted.");
            event.replyEmbeds(punishmentEmbed.build()).setEphemeral(true).queue();
            return;
        }
        if(event.getModalId().startsWith("add_note:")) {
            String punishmentId = event.getModalId().split(":")[1];
            if (checkRevertedModal(event, punishmentId)) return;
            String note = event.getValue("note").getAsString();
            if(!punishmentDatabase.hasNotes(punishmentId)) {
                punishmentDatabase.addNotes(punishmentId, note);
            } else {
                punishmentDatabase.addGeneralNotes(punishmentId, note);
            }
            EmbedBuilder noteEmbed = new EmbedBuilder().setColor(Color.GREEN).setDescription("✅ Successfully added note to punishment `" + punishmentId + "`.");
            event.replyEmbeds(noteEmbed.build()).setEphemeral(true).queue();
            return;
        }
        if(event.getModalId().startsWith("modify:")) {
            String punishmentId = event.getModalId().split(":")[1];
            if (checkRevertedModal(event, punishmentId)) return;
            String discordId = event.getMember().getId();
            UUID linkedUuid = UUID.fromString(checkpointDatabase.getUUIDFromDiscord(discordId));
            String duration =  event.getValue("duration").getAsString();
            try {
                PunishmentDurationParser.parse(duration);
            } catch (IllegalArgumentException e){
                EmbedBuilder failureEmbed = new EmbedBuilder().setColor(Color.RED).setDescription("❌ Invalid duration: `" + duration + "`.");
                event.replyEmbeds(failureEmbed.build()).setEphemeral(true).queue();
                return;
            }
            String reason = event.getValue("reason").getAsString();
            editPunishment(punishmentId, linkedUuid, duration, reason);
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
        if(DiscordEnabled) {
            if(punishmentType.equals("blacklist")) {
                punishmentChannel.sendMessage("`" + staffName + "` reverted an IP Ban from `" + playerName + "`").queue();
            } else {
                punishmentChannel.sendMessage("`" + staffName + "` reverted a " + punishmentType + " from `" + playerName + "` for `" + reason + "`").queue();
            }
        }
    }
    @EventHandler
    public void onNameBan(NameBanEvent event) {
        String playerName = Bukkit.getOfflinePlayer(event.getTargetUUID()).getName();
        String staffName = event.getStaff();
        if(DiscordEnabled) {
            EmbedBuilder punishmentEmbed = new EmbedBuilder()
                    .setTitle("Name Ban Issued")
                    .setDescription("**ID:** " + event.getPunishmentId() + "\n**Staff:** " + staffName + "\n**Target:** " + playerName)
                    .setThumbnail("https://mc-heads.net/avatar/" + event.getTargetUUID() + ".png")
                    .setColor(Color.RED);
            punishmentChannel.sendMessageEmbeds(punishmentEmbed.build()).queue();
        }
    }
    @EventHandler
    public void onIpBan(IPBanEvent event){
        List<UUID> targetUUIDs = event.getTargetUUIDs();
        List<String> targetNames = new ArrayList<>();
        for (UUID targetUUID : targetUUIDs){
            targetNames.add("`" + Bukkit.getOfflinePlayer(targetUUID).getName() + "`");
        }
        String staffName = event.getStaff();
        String notes = event.getNotes();
        if(notes == null){
            notes = "None";
        }
        if(DiscordEnabled) {
            EmbedBuilder punishmentEmbed = new EmbedBuilder()
                    .setTitle("IP Ban Issued")
                    .setDescription("**ID:** " + event.getPunishmentId() + "\n**Staff:** " + staffName + "\n**Target" + (targetUUIDs.size() > 1 ? "s:** " : ":** ") + String.join(", ", targetNames) + "\n**Note:** " + notes)
                    .setThumbnail("https://mc-heads.net/avatar/" + event.getTargetUUIDs().getFirst() + ".png")
                    .setColor(Color.RED);
            Button revert = Button.danger("punishment_revert:" + event.getPunishmentId(), "Revert");
            Button note = Button.primary("punishment_addNote:" + event.getPunishmentId(), "Add Note");
            punishmentChannel.sendMessageEmbeds(punishmentEmbed.build()).setComponents(ActionRow.of(revert, note)).queue();
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
        if(DiscordEnabled) {
            punishmentChannel.sendMessage("`" + staffName + "` edited a " + punishmentType + " from `" + playerName + "` for `" + punishmentReason + "`").queue();
        }
    }
}
