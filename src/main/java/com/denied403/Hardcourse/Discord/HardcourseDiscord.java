package com.denied403.Hardcourse.Discord;

import com.denied403.Hardcourse.Discord.Commands.CommandManager;
import com.denied403.Hardcourse.Discord.Tickets.Applications.ApplicationButtonListener;
import com.denied403.Hardcourse.Discord.Tickets.Applications.ApplicationMessageListener;
import com.denied403.Hardcourse.Discord.Tickets.PanelButtonListener;
import com.denied403.Hardcourse.Events.BanListener;
import com.denied403.Hardcourse.Hardcourse;
import com.denied403.Hardcourse.Utils.CheckpointDatabase;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static com.denied403.Hardcourse.Hardcourse.isDiscordEnabled;
import static com.denied403.Hardcourse.Utils.ColorUtil.stripAllColors;
import static com.denied403.Hardcourse.Utils.Playtime.getPlaytime;

public class HardcourseDiscord {

    private final JavaPlugin plugin;
    public static JDA jda;
    public static TextChannel chatChannel;
    public static TextChannel staffChatChannel;
    public static ThreadChannel hacksChannel;
    public static ThreadChannel logsChannel;
    public static ThreadChannel punishmentChannel;
    public static ThreadChannel reportChannel;

    public HardcourseDiscord(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    private static final Map<String, Message> lastHackAlert = new HashMap<>();
    private static CheckpointDatabase database;
    public static void initialize(CheckpointDatabase db) {
        database = db;
    }

    public void InitJDA() {
        if(isDiscordEnabled()) {
            String discordToken = plugin.getConfig().getString("DISCORD_TOKEN");
            if (discordToken == null) {
                plugin.getLogger().severe("Please provide a DISCORD_TOKEN in the config.yml file!");
                return;
            }
            try {
                jda = JDABuilder.createDefault(discordToken)
                        .setActivity(Activity.playing("On Hardcourse"))
                        .setStatus(OnlineStatus.ONLINE)
                        .enableIntents(GatewayIntent.GUILD_MEMBERS,
                                GatewayIntent.GUILD_MESSAGES,
                                GatewayIntent.GUILD_WEBHOOKS,
                                GatewayIntent.GUILD_PRESENCES,
                                GatewayIntent.MESSAGE_CONTENT)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .setChunkingFilter(ChunkingFilter.ALL)
                        .enableCache(CacheFlag.ONLINE_STATUS)
                        .addEventListeners(
                                new DiscordListener(),
                                new CommandManager((Hardcourse) plugin),
                                new DiscordButtonListener(),
                                new PanelButtonListener(),
                                new ApplicationMessageListener((Hardcourse) plugin),
                                new ApplicationButtonListener((Hardcourse) plugin),
                                new BanListener()

                        ).build().awaitReady();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (jda == null) {
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return;
            }
            String chatChannelId = plugin.getConfig().getString("Chat-Channel-Id");
            if (chatChannelId != null) {
                chatChannel = jda.getTextChannelById(chatChannelId);
            }

            String staffChatChannelId = plugin.getConfig().getString("Staff-Chat-Channel-Id");
            if (staffChatChannelId != null) {
                staffChatChannel = jda.getTextChannelById(staffChatChannelId);
            }

            String hacksChannelId = plugin.getConfig().getString("Anticheat-Channel-Id");
            if (hacksChannelId != null) {
                hacksChannel = jda.getThreadChannelById(hacksChannelId);
            }
            String logsChannelId = plugin.getConfig().getString("Logs-Channel-Id");
            if (logsChannelId != null) {
                logsChannel = jda.getThreadChannelById(logsChannelId);
            }
            String punishmentChannelId = plugin.getConfig().getString("Punishment-Channel-Id");
            if (punishmentChannelId != null) {
                punishmentChannel = jda.getThreadChannelById(punishmentChannelId);
            }
            String reportChannelId = plugin.getConfig().getString("Reports-Channel-Id");
            if (reportChannelId != null) {
                reportChannel = jda.getThreadChannelById(reportChannelId);
            }
        }
    }

    public static void sendMessage(Player player, String content, String type, String extra1, String extra2) {
        if (chatChannel == null) {
            player.sendMessage("Chat channel not found!");
            return;
        }
        if (staffChatChannel == null) {
            player.sendMessage("Staff chat channel not found!");
            return;
        }
        if (type.equals("staffchat")) {
            staffChatChannel.sendMessage("**`" + stripAllColors(player.displayName()) + "`**: " + content).queue();
        }
        if (type.equals("chat")) {
            chatChannel.sendMessage("[**" + extra1 + database.getLevel(player.getUniqueId()).toString().replace(".0", "") + "**] `" + stripAllColors(player.displayName()) + "`: " + content).queue();
        }
        if(type.equals("staffmessage")) {
            chatChannel.sendMessage("[**" + extra1 + database.getLevel(player.getUniqueId()).toString().replace(".0", "") + "**] **`" + stripAllColors(player.displayName()) + "`**: " + content).queue();
        }
        if (type.equals("join")) {
            chatChannel.sendMessage(":inbox_tray: **`" + stripAllColors(player.displayName()) + "`** joined").queue();
        }
        if(type.equals("firstjoin")) {
            chatChannel.sendMessage(":inbox_tray: **`" + stripAllColors(player.displayName()) + "`** joined the server for the first time _[#" + Bukkit.getOfflinePlayers().length + "]_").queue();
        }
        if (type.equals("leave")) {
            chatChannel.sendMessage(":outbox_tray: **`" + stripAllColors(player.displayName()) + "`** left the server").queue();
        }
        if (type.equals("hacks")) {
            String playerName = stripAllColors(player.displayName());
            String messageContent;
            if(player.getStatistic(Statistic.PLAY_ONE_MINUTE) >= 72000){
                messageContent = "**`" + playerName + "`** skipped from level `" + extra1 + "` to level `" + extra2 + "`! This player has more than 1 hour of playtime, watch out!";
            } else {
                messageContent = "**`" + stripAllColors(playerName) + "`** skipped from level `" + extra1 + "` to level `" + extra2 + "`!";
            }
            if (lastHackAlert.containsKey(playerName)) {
                Message oldMessage = lastHackAlert.get(playerName);
                oldMessage.editMessage(oldMessage.getContentRaw())
                        .setComponents()
                        .queue();
            }
            if(player.getStatistic(Statistic.PLAY_ONE_MINUTE) >= 72000){
                hacksChannel.sendMessage(messageContent).queue(sentMessage -> lastHackAlert.put(playerName, sentMessage));
            }
            else {
                hacksChannel.sendMessage(messageContent)
                        .setActionRow(Button.danger("ban:" + playerName, "Ban"))
                        .queue(sentMessage -> lastHackAlert.put(playerName, sentMessage));
            }
        }
        if (type.equals("starting")){
            chatChannel.sendMessage(":white_check_mark: **The server has started up!**").queue();
        }
        if (type.equals("stopping")){
            chatChannel.sendMessage(":octagonal_sign: **The server has shut down!**").queue();
        }
        if (type.equals("winning")){
            if(!(extra1.equals("3"))) {
                chatChannel.sendMessage(":trophy: **`" + stripAllColors(player.displayName()) + "`** has completed **Season " + extra1 + "**! Their playtime was " + getPlaytime(player)).queue();
            } else {
                chatChannel.sendMessage(":trophy: **`" + stripAllColors(player.displayName()) + "`** has completed **Season " + extra1 + "**! Their playtime was " + getPlaytime(player) + ". This player has finished hardcourse!").queue();
            }
        }
        if(type.equals("logs")){
            final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            if(extra1.equals("true")){
                logsChannel.sendMessage("`[FILTERED] [" + f.format(new Date()) + "] " + stripAllColors(player.getName()) + ": " + content.replaceAll("`", "'") + "`").queue();
            } if(extra1.equals("false")){
                logsChannel.sendMessage("`[" + f.format(new Date()) + "] " + stripAllColors(player.getName()) + ": " + content.replaceAll("`", "'") + "`").queue();
            }
            if(extra1.equals("join")){
                logsChannel.sendMessage("`[" + f.format(new Date()) + "] " + stripAllColors(player.getName()) + " joined`").queue();
            }
            if(extra1.equals("quit")){
                logsChannel.sendMessage("`[" + f.format(new Date()) + "] " + stripAllColors(player.getName()) + " quit`").queue();
            }
            if(extra1.equals("vanished")){
                logsChannel.sendMessage((extra2.equals("silent") ? "`[SILENT] " : "`") + "[" + f.format(new Date()) + "] " + stripAllColors(player.getName()) + " vanished`").queue();
            }
            if(extra1.equals("unvanished")){
                logsChannel.sendMessage((extra2.equals("silent") ? "`[SILENT] " : "`") + "[" + f.format(new Date()) + "] " + stripAllColors(player.getName()) + " unvanished`").queue();
            }
        }
    }
}
