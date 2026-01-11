package com.denied403.Hardcourse.Discord.Commands;

import com.transfemme.dev.core403.Punishments.Enums.PunishmentType;
import com.transfemme.dev.core403.Punishments.PunishmentReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import com.transfemme.dev.core403.Punishments.Events.onConfirmClick;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.awt.*;
import java.util.UUID;

import static com.denied403.Hardcourse.Hardcourse.checkpointDatabase;
import static com.denied403.Hardcourse.Hardcourse.plugin;
import static com.denied403.Hardcourse.Utils.Luckperms.hasLuckPermsPermission;

public class Punish {
    public static void run(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        String targetName = event.getOption("player") != null ? event.getOption("player").getAsString() : null;
        String reasonName = event.getOption("reason") != null ? event.getOption("reason").getAsString() : null;
        String notes = event.getOption("note") != null ? event.getOption("note").getAsString() : null;
        boolean isWarn = event.getOption("warn") != null && event.getOption("warn").getAsBoolean();
        if (targetName == null || reasonName == null) {
            event.getHook().sendMessage("❌ Player or reason not specified.").queue();
            return;
        }

        PunishmentReason reason = PunishmentReason.getReasonByName(reasonName);
        if (reason == null) {
            event.getHook().sendMessage("❌ Invalid punishment reason.").queue();
            return;
        }

        String type = reason.getType() == PunishmentType.WARN && isWarn ? "warn" : "confirm";

        String linkedUuidString = checkpointDatabase.getUUIDFromDiscord(event.getMember().getId());
        UUID staffUUID;
        if (linkedUuidString != null) {
            staffUUID = UUID.fromString(linkedUuidString);
        } else {
            EmbedBuilder embed = new EmbedBuilder().setColor(Color.RED).setDescription("❌ You must be *linked* to use this!");
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                OfflinePlayer staff = Bukkit.getOfflinePlayer(staffUUID);
                OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                if (!hasLuckPermsPermission(staff.getUniqueId(), "core403.punish.use")) {
                    event.reply("❌ You do not have permission to use this command.").setEphemeral(true).queue();
                    return;
                }
                long expires;
                if(notes != null) {
                    expires = onConfirmClick.handlePunishment(staff.getUniqueId().toString(), reason, target, type, notes);
                } else {
                    expires = onConfirmClick.handlePunishment(staff.getUniqueId().toString(), reason, target, type, null);
                }

                String expiresText;
                String expiresRelative;
                if (expires != -1) {
                    long discordTimestamp = expires / 1000L;
                    expiresText = "<t:" + discordTimestamp + ":f>";
                    expiresRelative = "<t:" + discordTimestamp + ":R>";
                } else {
                    expiresText = "Permanent";
                    expiresRelative = "Never";
                }

                EmbedBuilder eb = new EmbedBuilder()
                        .setDescription("✅ Issued " + reason.getType().toString().toLowerCase() +
                                " to `" + target.getName() + "` for **" + reasonName + "** to last until " + expiresText + " (" + expiresRelative + ").")
                        .setColor(Color.GREEN);

                event.getHook().sendMessageEmbeds(eb.build()).queue();

            } catch (Exception e) {
                e.printStackTrace();
                event.getHook().sendMessage("❌ An error occurred while issuing punishment.").queue();
            }
        });
    }
}
