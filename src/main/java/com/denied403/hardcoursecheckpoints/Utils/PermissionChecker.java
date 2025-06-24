package com.denied403.hardcoursecheckpoints.Utils;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PermissionChecker {

    private static Permission permission = null;

    public PermissionChecker(HardcourseCheckpoints plugin) {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) {
            permission = rsp.getProvider();
        }
    }

    public static boolean playerHasPermission(String playerName, String permissionNode) {
        if (permission == null) {
            return false;
        }
        OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(playerName);
        return permission.playerHas(null, player, permissionNode);

    }
}
