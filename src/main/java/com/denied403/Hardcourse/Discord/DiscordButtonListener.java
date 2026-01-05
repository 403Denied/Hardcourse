package com.denied403.Hardcourse.Discord;

import com.denied403.Hardcourse.Utils.CheckpointDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.transfemme.dev.core403.Punishments.Events.onConfirmClick;
import com.transfemme.dev.core403.Punishments.PunishmentReason;
import org.bukkit.Bukkit;

import java.awt.*;
import java.sql.SQLException;
import java.util.UUID;

import static com.denied403.Hardcourse.Events.BanListener.runBanCleanup;
import static com.denied403.Hardcourse.Hardcourse.isDiscordEnabled;
import static com.denied403.Hardcourse.Hardcourse.plugin;
import static com.denied403.Hardcourse.Utils.Luckperms.hasLuckPermsPermission;

public class DiscordButtonListener extends ListenerAdapter {
    private static CheckpointDatabase database;
    public static void initialize(CheckpointDatabase db) {database = db;}

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(isDiscordEnabled()) {
            String id = event.getComponentId();

            if (id.startsWith("ban:")) {
                String discordId = event.getMember().getId();
                String linkedUuidString = database.getUUIDFromDiscord(discordId);
                UUID linkedUUID;
                if (linkedUuidString != null) {
                    linkedUUID = UUID.fromString(linkedUuidString);
                } else {
                    EmbedBuilder embed = new EmbedBuilder().setColor(Color.RED).setDescription("❌ You must be *linked* to use this!");
                    event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                    return;
                }
                if(hasLuckPermsPermission(linkedUUID, "core403.punish.use")) {
                    String playerName = id.substring("ban:".length());
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        try {
                            onConfirmClick.handlePunishment(linkedUUID.toString(), PunishmentReason.getReasonByName("Unfair Advantage"), Bukkit.getOfflinePlayer(playerName), "ban", "Issued via discord");
                            event.reply("Issued ban for **`" + playerName + "`**.").setEphemeral(true).queue();
                            runBanCleanup(playerName);
                        } catch(SQLException e){
                            EmbedBuilder embed = new EmbedBuilder().setColor(Color.RED).setDescription("❌ An error occurred. Please notify <@401582030506295308>. `" + e.getMessage() + "`");
                            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                        }
                    });
                } else {
                    EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setDescription("❌ You don't have permission to do this!");
                    event.replyEmbeds(denied.build()).setEphemeral(true).queue();
                }
            }
        }
    }
}

