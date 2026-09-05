package com.denied403.Hardcourse.Discord;

import com.denied403.core403.Discord.Applications.TicketSetup;
import com.denied403.core403.Discord.DiscordService;
import com.denied403.core403.Discord.DiscordSetup;
import com.denied403.core403.Punishments.Api.PunishmentEvent;
import com.denied403.core403.Punishments.Utils.PunishmentReason;
import com.denied403.core403.Util.LuckPermsUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.Color;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import static com.denied403.Hardcourse.Hardcourse.checkpointDatabase;
import static com.denied403.Hardcourse.Hardcourse.plugin;
import static com.denied403.core403.Core403.playerDatabase;
import static com.denied403.core403.Punishments.Events.PunishmentConfirmListener.handlePunishment;
import static com.denied403.core403.Util.ColorUtil.Colorize;
import static com.denied403.core403.Util.ColorUtil.stripAllColors;

public final class HardcourseDiscordExtras extends ListenerAdapter implements Listener {
    private static String hacksChannelId;
    private static String deathsChannelId;
    private static String checkpointsChannelId;
    private static String staffInviteChannelId;

    private static ThreadChannel hacksChannel() { return DiscordService.getThreadChannel(hacksChannelId); }
    private static ThreadChannel deathsChannel() { return DiscordService.getThreadChannel(deathsChannelId); }
    private static ThreadChannel checkpointsChannel() { return DiscordService.getThreadChannel(checkpointsChannelId); }
    private static final Map<String, Message> lastHackAlert = new HashMap<>();

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss z");
    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    private static String timestamp() {
        return FORMAT.format(new Date());
    }

    public static void init() {
        hacksChannelId = plugin.getConfig().getString("Anticheat-Channel-Id");
        deathsChannelId = plugin.getConfig().getString("Deaths-Channel-Id");
        checkpointsChannelId = plugin.getConfig().getString("Checkpoints-Channel-Id");
        staffInviteChannelId = plugin.getConfig().getString("Staff-Invite-Channel-Id");

        warnIfUnresolved("Anticheat-Channel-Id", hacksChannelId, hacksChannel());
        warnIfUnresolved("Deaths-Channel-Id", deathsChannelId, deathsChannel());
        warnIfUnresolved("Checkpoints-Channel-Id", checkpointsChannelId, checkpointsChannel());

        HardcourseDiscordExtras instance = new HardcourseDiscordExtras();
        DiscordService.registerListener(instance);
        Bukkit.getPluginManager().registerEvents(instance, plugin);
    }

    private static void warnIfUnresolved(String configKey, String configuredId, ThreadChannel resolved) {
        if (configuredId == null || configuredId.isBlank() || configuredId.endsWith("_HERE")) {
            plugin.getLogger().warning("config.yml: " + configKey + " is not set - the corresponding Discord logging is disabled.");
        } else if (resolved == null) {
            plugin.getLogger().warning("config.yml: " + configKey + " (" + configuredId
                    + ") could not be resolved to a thread. If it's archived, open it in Discord to unarchive it and restart. "
                    + "Otherwise double-check the id and that the bot can see the channel.");
        }
    }

    public static DiscordSetup buildSetup() {
        return new DiscordSetup().infoExtraFields((embed, offlinePlayer) -> {
            UUID uuid = offlinePlayer.getUniqueId();
            Double level = checkpointDatabase.getLevel(uuid);
            String levelString;
            if (level == null) {
                levelString = "Not Migrated";
            } else {
                Integer season = checkpointDatabase.getSeason(uuid);
                levelString = (season != null ? season + "-" : "") + String.valueOf(level).replace(".0", "");
            }
            embed.addField("Level", levelString, false);
        });
    }

    public static TicketSetup buildTicketSetup() {
        return new TicketSetup().onAccepted((event, targetId) -> {
            if (staffInviteChannelId == null) return;
            var inviteChannel = event.getJDA().getTextChannelById(staffInviteChannelId);
            if (inviteChannel == null) return;
            inviteChannel.createInvite().setMaxAge(0).setMaxUses(1).queue(invite -> {
                String inviteUrl = invite.getUrl();
                event.getJDA().retrieveUserById(targetId).queue(user -> user.openPrivateChannel().queue(pc ->
                        pc.sendMessage("🎉 **Your application has been accepted!**\n\n📩 **Join the staff discord here:**\n" + inviteUrl).queue()));
            });
        });
    }

    public static void logCheckpointChange(String message) {
        DiscordService.sendToThread(checkpointsChannel(), "`[" + timestamp() + "] " + message + "`");
    }

    public static void announceWinning(Player player, String seasonId, boolean finalSeason) {
        if (!DiscordService.isEnabled() || DiscordService.chatChannel() == null) return;
        String name = stripAllColors(player.displayName());
        String suffix = finalSeason ? ". This player has finished hardcourse!" : "";
        DiscordService.chatChannel().sendMessage(":trophy: **`" + name + "`** has completed **Season " + seasonId
                + "**! Their playtime was " + com.denied403.core403.Util.Playtime.getPlaytime(player) + suffix).queue();
    }

    public static void logDeath(Player player) {
        DiscordService.sendToThread(deathsChannel(), "`[" + timestamp() + "] " + player.getName() + " died [#" + player.getStatistic(Statistic.DEATHS) + "]`");
    }

    public static void logDeathsSet(String targetName, long deaths, String actorName) {
        DiscordService.sendToThread(deathsChannel(), "`[" + timestamp() + "] " + targetName + " had their deaths set to " + deaths + " by " + actorName + "`");
    }

    public static void sendHacksAlert(Player p, String fromLevel, String toLevel) {
        ThreadChannel hacksChannel = hacksChannel();
        if (hacksChannel == null) return;
        String playerName = stripAllColors(p.displayName());
        boolean longPlaytime = p.getStatistic(Statistic.PLAY_ONE_MINUTE) >= 72000;
        String messageContent = longPlaytime
                ? "**`" + playerName + "`** skipped from level `" + fromLevel + "` to level `" + toLevel + "`! This player has more than 1 hour of playtime, watch out!"
                : "**`" + playerName + "`** skipped from level `" + fromLevel + "` to level `" + toLevel + "`!";

        if (lastHackAlert.containsKey(playerName)) {
            Message oldMessage = lastHackAlert.get(playerName);
            oldMessage.editMessage(oldMessage.getContentRaw()).setComponents().queue();
        }

        if (longPlaytime) {
            DiscordService.sendToThread(hacksChannel, ch -> ch.sendMessage(messageContent), sent -> lastHackAlert.put(playerName, sent));
        } else {
            DiscordService.sendToThread(hacksChannel,
                    ch -> ch.sendMessage(messageContent).setComponents(ActionRow.of(Button.danger("ban:" + playerName, "Ban"))),
                    sent -> lastHackAlert.put(playerName, sent));
        }
    }

    public static void runBanCleanup(String playerName) {
        ThreadChannel hacksChannel = hacksChannel();
        if (hacksChannel == null) return;
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
        if (!event.getTypeOfPunishment().startsWith("ban")) return;
        if (!event.getReason().equalsIgnoreCase("Unfair Advantage")) return;

        String playerName = Bukkit.getOfflinePlayer(event.getTargetUUID()).getName();
        if (!event.getStaff().equals("CONSOLE")) {
            if (Bukkit.getOfflinePlayer(event.getTargetUUID()).getStatistic(Statistic.PLAY_ONE_MINUTE) >= 72000) {
                Player staffPlayer = Bukkit.getPlayer(event.getStaff());
                if (staffPlayer != null && staffPlayer.isOnline()) {
                    staffPlayer.sendMessage(Colorize("<prefix>This player has more than 1 hour of playtime. Remember to provide evidence in &c#punishment-proof&f."));
                }
            }
        }
        runBanCleanup(playerName);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith("ban:")) return;

        String discordId = event.getMember().getId();
        String linkedUuidString = playerDatabase.getUUIDFromDiscord(discordId);
        UUID linkedUUID;
        if (linkedUuidString != null) {
            linkedUUID = UUID.fromString(linkedUuidString);
        } else {
            EmbedBuilder embed = new EmbedBuilder().setColor(Color.RED).setDescription("❌ You must be *linked* to use this!");
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        if (!LuckPermsUtil.hasPermission(linkedUUID, "core403.punish.use")) {
            EmbedBuilder denied = new EmbedBuilder().setColor(Color.RED).setDescription("❌ You don't have permission to do this!");
            event.replyEmbeds(denied.build()).setEphemeral(true).queue();
            return;
        }

        String playerName = id.substring("ban:".length());
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                handlePunishment(linkedUUID.toString(), PunishmentReason.getReasonByName("Unfair Advantage"), Bukkit.getOfflinePlayer(playerName), "ban", "Issued via discord");
                event.reply("Issued ban for **`" + playerName + "`**.").setEphemeral(true).queue();
                runBanCleanup(playerName);
            } catch (SQLException e) {
                EmbedBuilder embed = new EmbedBuilder().setColor(Color.RED).setDescription("❌ An error occurred. `" + e.getMessage() + "`");
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            }
        });
    }
}
