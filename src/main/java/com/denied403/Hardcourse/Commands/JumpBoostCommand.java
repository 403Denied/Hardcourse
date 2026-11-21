package com.denied403.Hardcourse.Commands;

import com.denied403.core403.Punishments.Enums.IPunishmentDuration;
import com.denied403.core403.Punishments.Utils.PunishmentDurationParser;
import com.denied403.core403.Punishments.Utils.TimeUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.denied403.Hardcourse.Hardcourse.plugin;
import static com.denied403.core403.Util.Chat.ColorUtil.Colorize;

public class JumpBoostCommand implements Listener {

    private record Boost(int amplifier, long expiresAt) {
        boolean isPermanent() { return expiresAt < 0; }
    }

    private static final Map<UUID, Boost> activeBoosts = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> applying = ThreadLocal.withInitial(() -> false);

    // The latest "*" boost, given to players who join while it is still active.
    private static volatile Boost globalBoost;
    // Players who lost the current global boost (milk, /effect clear) so rejoining doesn't hand it back.
    private static final Set<UUID> globalBoostRemoved = ConcurrentHashMap.newKeySet();

    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .requires(source -> source.getSender().hasPermission("hardcourse.jumpboost"))
                .then(Commands.argument("target", StringArgumentType.word())
                        .suggests(JumpBoostCommand::targetSuggestions)
                        .then(Commands.argument("time", StringArgumentType.word())
                                .suggests(JumpBoostCommand::timeSuggestions)
                                .then(Commands.argument("level", IntegerArgumentType.integer(2, 255))
                                        .executes(JumpBoostCommand::execute))))
                .build();
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        String targetArg = StringArgumentType.getString(ctx, "target");
        String timeArg = StringArgumentType.getString(ctx, "time");
        int level = IntegerArgumentType.getInteger(ctx, "level");

        IPunishmentDuration duration;
        try {
            duration = PunishmentDurationParser.parse(timeArg);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Colorize("<prefix>Invalid time <accent>" + timeArg + "<main>. Use formats like <accent>30s<main>, <accent>5m<main>, <accent>1h30m<main>."));
            return 0;
        }
        long millis = duration.durationMillis();

        List<Player> targets = new ArrayList<>();
        boolean everyone = targetArg.equals("*");
        if (everyone) {
            targets.addAll(Bukkit.getOnlinePlayers());
            if (targets.isEmpty()) {
                sender.sendMessage(Colorize("<prefix>There are no players online."));
                return 0;
            }
        } else {
            Player target = Bukkit.getPlayerExact(targetArg);
            if (target == null) {
                sender.sendMessage(Colorize("<prefix>Player <accent>" + targetArg + "<main> is not online."));
                return 0;
            }
            targets.add(target);
        }

        long expiresAt = millis < 0 ? -1L : System.currentTimeMillis() + millis;
        Boost boost = new Boost(level - 1, expiresAt);
        String timeText = millis < 0 ? "forever" : "for <accent>" + TimeUtils.formatDate(millis) + "<main>";

        if (everyone) {
            globalBoost = boost;
            globalBoostRemoved.clear();
        }

        for (Player target : targets) {
            activeBoosts.put(target.getUniqueId(), boost);
            apply(target, boost);
            target.sendMessage(Colorize("<prefix>You have been given <accent>Jump Boost " + level + "<main> " + timeText + "!"));
        }

        String who = everyone ? "<accent>everyone<main>" : "<accent>" + targets.getFirst().getName() + "<main>";
        if (everyone || !(sender instanceof Player p) || !p.equals(targets.getFirst())) {
            sender.sendMessage(Colorize("<prefix>Gave " + who + " <accent>Jump Boost " + level + "<main> " + timeText + "."));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static void apply(Player player, Boost boost) {
        int ticks;
        if (boost.isPermanent()) {
            ticks = PotionEffect.INFINITE_DURATION;
        } else {
            long remainingTicks = (boost.expiresAt() - System.currentTimeMillis()) / 50L;
            if (remainingTicks <= 0) {
                activeBoosts.remove(player.getUniqueId());
                return;
            }
            ticks = remainingTicks > Integer.MAX_VALUE ? PotionEffect.INFINITE_DURATION : (int) remainingTicks;
        }
        applying.set(true);
        try {
            player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, ticks, boost.amplifier()));
        } finally {
            applying.set(false);
        }
    }

    private static boolean isExpired(Boost boost) {
        return !boost.isPermanent() && boost.expiresAt() <= System.currentTimeMillis();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Boost global = globalBoost;
        if (global == null) return;
        if (isExpired(global)) {
            globalBoost = null;
            globalBoostRemoved.clear();
            return;
        }
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (globalBoostRemoved.contains(uuid)) return;

        Boost existing = activeBoosts.get(uuid);
        if (existing != null && existing != global && !isExpired(existing)) return;

        activeBoosts.put(uuid, global);
        apply(player, global);
        String timeText = global.isPermanent() ? "forever" : "for <accent>" + TimeUtils.formatDate(global.expiresAt() - System.currentTimeMillis()) + "<main>";
        player.sendMessage(Colorize("<prefix>Everyone has <accent>Jump Boost " + (global.amplifier() + 1) + "<main>! You have it " + timeText + "."));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!activeBoosts.containsKey(player.getUniqueId())) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Boost boost = activeBoosts.get(player.getUniqueId());
            if (boost != null && player.isOnline()) apply(player, boost);
        });
    }

    @EventHandler
    public void onEffectRemoved(EntityPotionEffectEvent event) {
        if (applying.get()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getModifiedType() != PotionEffectType.JUMP_BOOST) return;
        if (event.getAction() != EntityPotionEffectEvent.Action.REMOVED
                && event.getAction() != EntityPotionEffectEvent.Action.CLEARED) return;
        if (event.getCause() == EntityPotionEffectEvent.Cause.DEATH) return;
        Boost removed = activeBoosts.remove(player.getUniqueId());
        if (removed != null && removed == globalBoost) globalBoostRemoved.add(player.getUniqueId());
    }

    private static CompletableFuture<Suggestions> targetSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();
        if ("*".startsWith(input)) builder.suggest("*");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(input)) builder.suggest(player.getName());
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> timeSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();
        for (String s : List.of("30s", "1m", "5m", "10m", "30m", "1h")) {
            if (s.startsWith(input)) builder.suggest(s);
        }
        return builder.buildFuture();
    }
}
