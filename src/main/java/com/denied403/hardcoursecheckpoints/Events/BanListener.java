package com.denied403.hardcoursecheckpoints.Events;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.jda;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDiscordEnabled;

public class BanListener implements Listener {
    private static HardcourseCheckpoints plugin = JavaPlugin.getPlugin(HardcourseCheckpoints.class);
    public BanListener(HardcourseCheckpoints plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBan(PlayerCommandPreprocessEvent e) {
        if(isDiscordEnabled()) {
            if (!e.getMessage().toLowerCase().startsWith("/ban ")) return;
            Player p = e.getPlayer();
            String[] args = e.getMessage().split(" ");
            if (args.length < 2) return;

            if (!p.hasPermission("core403.punish")) return;

            String target = args[1];
            runBanCleanup(target);
        }
    }

    @EventHandler
    public void onConsoleBan(ServerCommandEvent e) {
        if(isDiscordEnabled()) {
            String cmd = e.getCommand().toLowerCase();
            if (!cmd.startsWith("ban ")) return;
            String[] args = cmd.split(" ");
            if (args.length < 2) return;

            runBanCleanup(args[1]);
        }
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
                        // Change the button text to indicate the player is banned
                        updated.add(Button.danger(b.getId(), "Banned").asDisabled());
                        updated.add(b.asDisabled());
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


}
