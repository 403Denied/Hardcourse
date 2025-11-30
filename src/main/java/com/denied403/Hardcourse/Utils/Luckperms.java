package com.denied403.Hardcourse.Utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Luckperms {
    public static boolean hasLuckPermsPermission(UUID uuid, String permission) {
        try {
            LuckPerms lp = LuckPermsProvider.get();
            User user = lp.getUserManager().getUser(uuid);
            if (user == null) {
                user = lp.getUserManager().loadUser(uuid).get();
            }
            return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static CompletableFuture<String> getLuckPermsPrefix(UUID uuid) {
        LuckPerms lp = LuckPermsProvider.get();

        return lp.getUserManager().loadUser(uuid).thenApply(user -> {
            if (user == null) return null;

            CachedMetaData meta = user.getCachedData().getMetaData();
            String prefix = meta.getPrefix();
            return (prefix != null && !prefix.isEmpty()) ? prefix : null;
        });
    }
}
