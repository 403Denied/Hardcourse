package com.denied403.hardcoursecheckpoints.Events;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import com.transfemme.dev.core403.Punishments.Api.CustomEvents.IPBanEvent;
import com.transfemme.dev.core403.Punishments.Api.CustomEvents.NameBanEvent;
import com.transfemme.dev.core403.Punishments.Api.CustomEvents.PunishmentEvent;
import com.transfemme.dev.core403.Punishments.Api.CustomEvents.RevertEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.*;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDiscordEnabled;
import static org.bukkit.Bukkit.getServer;

public class BanListener implements Listener {
    private static HardcourseCheckpoints plugin = JavaPlugin.getPlugin(HardcourseCheckpoints.class);
    public BanListener(HardcourseCheckpoints plugin) {
        BanListener.plugin = plugin;
    }

    public static void runBanCleanup(String playerName) {
        MessageChannel channel = jda.getTextChannelById(plugin.getConfig().getString("Report-Channel-Id"));
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
                    if (Bukkit.getOfflinePlayer(event.getTargetUUID()).getStatistic(Statistic.PLAY_ONE_MINUTE) >= 60) {
                        getServer().getPlayer(event.getStaff()).sendMessage("&c&lHARDCOURSE &rThis player has more than 1 hour of playtime. Remember to provide evidence in &c#punishment-proof&f.");
                    }
                }
                if(isDiscordEnabled()) {
                    runBanCleanup(playerName);
                }
            }
        }
        if(isDiscordEnabled()) {
            punishmentChannel.sendMessage("`" + event.getStaff() + "` " + event.getTypeOfPunishment() + " `" + playerName + "` for " + reason + " [" + event.getDuration() + "]").queue();
        }
    }
    @EventHandler
    public void onRevert(RevertEvent event) {
        String playerName = Bukkit.getOfflinePlayer(event.getTargetUUID()).getName();
        String staffName = event.getStaff();
        String reason = event.getReason();
        String id = event.getPunishmentID();
        if(isDiscordEnabled()) {
            punishmentChannel.sendMessage("`" + staffName + "` reverted punishment ID " + id + " on `" + playerName + "` for `" + reason + "`").queue();
        }
    }
    @EventHandler
    public void onNameBan(NameBanEvent event) {
        String playerName = Bukkit.getOfflinePlayer(event.getBannedUUID()).getName();
        String staffName = event.getStaffName();
        if(isDiscordEnabled()) {
            punishmentChannel.sendMessage("`" + staffName + "` name banned `" + playerName + "`").queue();
        }
    }
    @EventHandler
    public void onIpBan(IPBanEvent event){
        String playerName = Bukkit.getOfflinePlayer(event.getBannedUUID()).getName();
        String staffName = event.getStaffName();
        if(isDiscordEnabled()) {
            punishmentChannel.sendMessage("`" + staffName + "` IP banned `" + playerName + "`").queue();
        }
    }
}
