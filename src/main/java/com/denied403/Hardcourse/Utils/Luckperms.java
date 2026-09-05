package com.denied403.Hardcourse.Utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;

import java.util.UUID;

public class Luckperms {
    public static void removeRank(UUID uuid){
        try {
            LuckPerms lp = LuckPermsProvider.get();
            User user = lp.getUserManager().getUser(uuid);
            if (user == null) {
                user = lp.getUserManager().loadUser(uuid).get();
            }
            user.data().clear();
            lp.getUserManager().saveUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void addRank(UUID uuid, String group){
        try {
            LuckPerms lp = LuckPermsProvider.get();
            User user = lp.getUserManager().getUser(uuid);
            if (user == null) {
                user = lp.getUserManager().loadUser(uuid).get();
            }
            Node node = InheritanceNode.builder(group).build();
            user.data().add(node);
            lp.getUserManager().saveUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
