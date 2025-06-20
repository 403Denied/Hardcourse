package com.denied403.hardcoursecheckpoints.Discord;

import com.denied403.hardcoursecheckpoints.Discord.Commands.CommandManager;
import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;

import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.getHighestCheckpoint;
import static com.denied403.hardcoursecheckpoints.Utils.Playtime.getPlaytime;

public class HardcourseDiscord {

    private final JavaPlugin plugin;
    public static JDA jda;
    public static TextChannel chatChannel;
    public static TextChannel staffChatChannel;
    public static TextChannel hacksChannel;

    public HardcourseDiscord(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void InitJDA() throws LoginException {
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
                            new CommandManager(),
                            new DiscordButtonListener((HardcourseCheckpoints) plugin)
                    )
                    .build().awaitReady();
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

        String hacksChannelId = plugin.getConfig().getString("Report-Channel-Id");
        if (hacksChannelId != null) {
            hacksChannel = jda.getTextChannelById(hacksChannelId);
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
            staffChatChannel.sendMessage("**`" + player.getDisplayName() + "`**: " + content).queue();
        }
        if (type.equals("chat")) {
            chatChannel.sendMessage("[**" + extra1 + getHighestCheckpoint(player.getUniqueId()).toString().replace(".0", "") + "**] `" + player.getDisplayName() + "`: " + content).queue();
        }
        if(type.equals("staffmessage")) {
            chatChannel.sendMessage("[**" + extra1 + getHighestCheckpoint(player.getUniqueId()).toString().replace(".0", "") + "**] **`" + player.getDisplayName() + "`**: " + content).queue();
        }
        if (type.equals("join")) {
            chatChannel.sendMessage(":inbox_tray: **`" + player.getDisplayName() + "`** joined").queue();
        }
        if(type.equals("firstjoin")) {
            chatChannel.sendMessage(":inbox_tray: **`" + player.getDisplayName() + "`** joined the server for the first time _[#" + Bukkit.getOfflinePlayers().length + "]_").queue();
        }
        if (type.equals("leave")) {
            chatChannel.sendMessage(":outbox_tray: **`" + player.getDisplayName() + "`** left the server").queue();
        }
        if (type.equals("hacks")) {
            String playerName = player.getDisplayName();
            String message = "**`" + playerName + "`** may be hacking. They skipped from level `" + extra1 + "` to level `" + extra2 + "`!";
            hacksChannel.sendMessage(message)
                    .setActionRow(Button.danger("ban:" + playerName, "Ban"))
                    .queue();
        }
        if (type.equals("starting")){
            chatChannel.sendMessage(":white_check_mark: **The server has started up!**").queue();
        }
        if (type.equals("stopping")){
            chatChannel.sendMessage(":octagonal_sign: **The server has shut down!**").queue();
        }
        if (type.equals("winning")){
            chatChannel.sendMessage(":trophy: **`" + player.getDisplayName() + "`** has completed the course! Their playtime was " + getPlaytime(player)).queue();
        }
    }
}
