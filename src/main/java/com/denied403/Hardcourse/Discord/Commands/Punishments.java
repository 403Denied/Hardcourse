package com.denied403.Hardcourse.Discord.Commands;

import com.transfemme.dev.core403.Punishments.PunishmentReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.List;

import static com.denied403.Hardcourse.Hardcourse.checkpointDatabase;
import static com.denied403.Hardcourse.Utils.Luckperms.hasLuckPermsPermission;
import static com.transfemme.dev.core403.Core403.database;
import static com.transfemme.dev.core403.Punishments.PunishmentConfigLoader.getReasonById;

public class Punishments {
    public static void run(SlashCommandInteractionEvent event) {
        String linkedUuidString = checkpointDatabase.getUUIDFromDiscord(event.getMember().getId());
        UUID staffUUID;

        if (linkedUuidString != null) {
            staffUUID = UUID.fromString(linkedUuidString);
        } else {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setDescription("❌ You must be *linked* to use this!");
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        if (!hasLuckPermsPermission(staffUUID, "core403.punish.use")) {
            event.reply("❌ You do not have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String targetName = Objects.requireNonNull(event.getOption("username")).getAsString();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        target.getUniqueId();

        UUID targetUUID = target.getUniqueId();

        List<Object> formatted = new ArrayList<>();

        String sql = "SELECT id, type, reasonId, issued, expires, reverted " +
                "FROM punishments WHERE playerUuid = ? ORDER BY issued DESC";

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, targetUUID.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String typeRaw = rs.getString("type").toUpperCase(Locale.ROOT);

                if (typeRaw.equals("BLACKLIST")) continue;

                long expires = rs.getLong("expires");
                long reverted = rs.getLong("reverted");
                String reasonId = rs.getString("reasonId");

                PunishmentReason reason = getReasonById(reasonId);
                String reasonName = (reason != null ? reason.getName() : "Unknown");

                String typeSymbol =
                        switch (typeRaw) {
                            case "BAN", "NAME_BAN" -> "(B)";
                            case "MUTE" -> "(M)";
                            case "WARN" -> "(W)";
                            default -> "(?)";
                        };
                String line;
                if (reverted > 0) {

                    String revertedText = "<t:" + (reverted / 1000) + ":R>";
                    line = "~~" + typeSymbol + " " + reasonName + "~~ - Reverted: " + revertedText;

                } else {
                    String expiresText;

                    if (expires == -1) {
                        expiresText = "Expires: Never";
                    } else {
                        boolean expired = expires < System.currentTimeMillis();
                        String when = "<t:" + (expires / 1000) + ":R>";
                        expiresText = (expired ? "Expired: " : "Expires: ") + when;
                    }

                    line = typeSymbol + " " + reasonName + " - " + expiresText;
                }

                formatted.add(line);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            event.reply("❌ Error fetching punishment data.").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Punishments for `" + targetName + "`")
                .setColor(Color.RED)
                .setThumbnail("https://mc-heads.net/avatar/" + targetUUID + ".png");

        if (formatted.isEmpty()) {
            embed.setDescription("✅ No Punishments Found For `" + targetName + "`");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Object s : formatted) {
                sb.append("• ").append(s).append("\n");
            }
            embed.setDescription(sb.toString());
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
