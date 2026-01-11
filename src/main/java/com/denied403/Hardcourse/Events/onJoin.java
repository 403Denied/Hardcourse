package com.denied403.Hardcourse.Events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.Hardcourse.Hardcourse.*;
import static com.denied403.Hardcourse.Points.Shop.PointsShop.givePointsShopPaper;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;
import static com.transfemme.dev.core403.Util.ColorUtil.stripAllColors;

public class onJoin implements Listener {
    @EventHandler
    public void onJoinEvent(org.bukkit.event.player.PlayerJoinEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();

        if (DiscordEnabled) {
            if (player.hasPlayedBefore()) {
                sendMessage(player, null, "join", null, null);
            } else {
                sendMessage(player, null, "firstjoin", null, null);
            }
            sendMessage(player, null, "logs", "join", null);
        }
        if(!player.hasPlayedBefore()) {
            checkpointDatabase.setCheckpointData(player.getUniqueId(), 1, 0, 0);
        }
        if(checkpointDatabase.getCheckpointData(player.getUniqueId()) == null) {
            int season;
            double level;
            if(!player.getWorld().getName().startsWith("Season")){
                season = 1;
            } else {
                season = Integer.parseInt(player.getWorld().getName().replace("Season", ""));
            }
            File file = new File(plugin.getDataFolder(), "checkpoints.yml");
            if (!file.exists()) {
                return;
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            if(config.getKeys(false).contains(player.getUniqueId().toString())) {
                level = config.getDouble(player.getUniqueId().toString());
                config.set(player.getUniqueId().toString(), null);
                try {
                    config.save(file);
                } catch (Exception e) {
                    Bukkit.getLogger().severe("Failed to save checkpoints file: " + e.getMessage());
                }
                player.sendMessage(Colorize("<prefix>Your checkpoint data has successfully been migrated from legacy storage to the new system. Level: <accent>" + String.valueOf(level).replace(".0", "") + "<main> Season: <accent>" + season + "<main>. If you believe there is an error with these numbers, please contact an administrator."));
                checkpointDatabase.setCheckpointData(player.getUniqueId(), season, level, 0);
            } else {
                checkpointDatabase.setCheckpointData(player.getUniqueId(), 1, 0, 0);
                player.teleport(player.getWorld().getSpawnLocation());
                player.setRespawnLocation(player.getLocation());
            }
        }
        if(checkpointDatabase.getSeason(player.getUniqueId()) == 0 || checkpointDatabase.getSeason(player.getUniqueId()) == null) {
            checkpointDatabase.setSeason(player.getUniqueId(), 1);
        }
        if(isDev) {
            boolean hasPointsShop = false;

            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null || item.getType() != Material.PAPER) continue;

                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && stripAllColors(meta.displayName()).equalsIgnoreCase("Points Shop")) {
                    hasPointsShop = true;
                    break;
                }
            }
            if (!hasPointsShop) {
                givePointsShopPaper(player, true);
            }
        }
        Material killItem = Material.CLOCK;
        ItemStack killItemStack = new ItemStack(killItem);
        org.bukkit.inventory.meta.ItemMeta killItemMeta = killItemStack.getItemMeta();
        killItemMeta.addEnchant(Enchantment.INFINITY, 1, true);
        killItemMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        killItemMeta.displayName(Colorize("&c&lStuck").decoration(TextDecoration.ITALIC, false));
        killItemMeta.itemName(Colorize("&c&lStuck").decoration(TextDecoration.ITALIC, false));
        java.util.ArrayList<Component> stuckLore = new java.util.ArrayList<>();
        stuckLore.add(Colorize(" "));
        stuckLore.add(Colorize("<gray>Click if you're stuck to go back to your level").decoration(TextDecoration.ITALIC, false));
        killItemMeta.lore(stuckLore);
        killItemStack.setItemMeta(killItemMeta);
        if(!player.getInventory().contains(killItemStack)) {
            player.getInventory().setItem(8, killItemStack);
        }

        if (!player.hasPlayedBefore()) {
            World targetWorld = Bukkit.getServer().getWorld("Season1");
            assert targetWorld != null;
            Location spawnLocation = targetWorld.getSpawnLocation();
            player.teleport(spawnLocation);
            player.updateCommands();
            player.sendMessage(Colorize("<prefix>Welcome to hardcourse. This parkour server contains over 1000 levels that will test your patience (and your will to live). To die you may click the <accent>clock<main> in your 9th hotbar slot. Think it's worth it? <accent>You may begin<main>."));
        }

        ItemStack torch = new ItemStack(Material.TORCH);
        ItemMeta torchMeta = torch.getItemMeta();
        torchMeta.displayName(Colorize("&cHide &rPlayers").decoration(TextDecoration.ITALIC, false));
        torchMeta.itemName(Colorize("&cHide &rPlayers").decoration(TextDecoration.ITALIC, false));
        torch.setItemMeta(torchMeta);

        ItemStack soulTorch = new ItemStack(Material.SOUL_TORCH);
        ItemMeta soulTorchMeta = soulTorch.getItemMeta();
        soulTorchMeta.displayName(Colorize("&cShow &rPlayers").decoration(TextDecoration.ITALIC, false));
        soulTorchMeta.itemName(Colorize("&cShow &rPlayers").decoration(TextDecoration.ITALIC, false));
        soulTorch.setItemMeta(soulTorchMeta);

        if(isDev) {
            if (player.getInventory().contains(soulTorch)) {
                int index = player.getInventory().first(soulTorch);
                if (index != -1) {
                    player.getInventory().setItem(index, torch);
                    player.updateInventory();
                }
            }
            if (!player.getInventory().contains(torch) || !player.getInventory().contains(soulTorch)) {
                player.getInventory().setItem(0, torch);
            }
        }
    }
}
