package com.denied403.Hardcourse.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ColorUtil {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    public static Component Colorize(String input){
        String mmFormatted = convertLegacyToMiniMessage(input);
        return miniMessage.deserialize(mmFormatted);
    }
    public static String convertLegacyToMiniMessage(String legacyText) {
        return legacyText
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");
    }
    public static String stripAllColors(String input) {
        String noLegacy = input.replaceAll("(?i)&[0-9A-FK-OR]", "");
        return noLegacy.replaceAll("<[^>]+>", "");
    }

    public static String stripAllColors(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
