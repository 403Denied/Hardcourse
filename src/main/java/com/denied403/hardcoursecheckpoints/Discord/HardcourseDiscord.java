package com.denied403.hardcoursecheckpoints.Discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;

import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.getHighestCheckpoint;

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
                    .addEventListeners(new DiscordListener())
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

    public static void sendMessage(Player player, String content, String type, String lastCP) {
        if (chatChannel == null) {
            player.sendMessage("Chat channel not found!");
            return;
        }
        if (staffChatChannel == null) {
            player.sendMessage("Staff chat channel not found!");
            return;
        }
        if (type.equals("staffchat")) {
            staffChatChannel.sendMessage("**" + player.getDisplayName() + "**: " + content).queue();
        }
        if (type.equals("chat")) {
            chatChannel.sendMessage("[**" + getHighestCheckpoint(player.getUniqueId()).toString().replace(".0", "") + "**] **" + player.getDisplayName() + "**: " + content).queue();
        }
        if (type.equals("join")) {
            chatChannel.sendMessage("**+** __" + player.getDisplayName() + "__").queue();
        }
        if (type.equals("leave")) {
            chatChannel.sendMessage("**-** __" + player.getDisplayName() + "__").queue();
        }
        if (type.equals("hacks")) {
            hacksChannel.sendMessage("**" + player.getDisplayName() + "** may be hacking. They skipped from " + lastCP + " to " + getHighestCheckpoint(player.getUniqueId())).queue();
        }
        if (type.equals("starting")){
            chatChannel.sendMessage("**Server is starting.**").queue();
        }
        if (type.equals("stopping")){
            chatChannel.sendMessage("**Server is shutting down.**").queue();
        }
    }
}
