package dev.loststr1ng.protectionStonesMenu.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MessageUtils {

    private static final LegacyComponentSerializer AMPERSAND =
            LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer SECTION =
            LegacyComponentSerializer.legacySection();

    public static Component getColoredMessage(String message) {
        return getColoredMessage(null, message);
    }

    public static Component getColoredMessage(Player player, String message) {
        String parsed = setPlaceholders(player, message == null ? "" : message);
        Component component = looksLikeMiniMessage(parsed)
                ? deserializeMiniMessage(parsed)
                : AMPERSAND.deserialize(parsed);
        return component.decoration(TextDecoration.ITALIC, false);
    }

    public static String getLegacy(String message){
        return getLegacy(null, message);
    }

    public static String getLegacy(Player player, String message){
        Component component = getColoredMessage(player, message);
        return SECTION.serialize(component);
    }

    public static List<String> getColoredList(List<String> list){
        return getColoredList(null, list);
    }

    public static List<String> getColoredList(Player player, List<String> list){
        List<String> coloredList = new ArrayList<>();
        for(String l: list){
            coloredList.add(getLegacy(player, l));
        }

        return coloredList;
    }

    public static List<Component> components(List<String> list){
        return components(null, list);
    }

    public static List<Component> components(Player player, List<String> list){
        List<Component> coloredList = new ArrayList<>();
        for(String l: list){
            coloredList.add(getColoredMessage(player, l));
        }
        return coloredList;
    }

    private static boolean looksLikeMiniMessage(String message) {
        return message.indexOf('<') >= 0 && message.indexOf('>') > message.indexOf('<');
    }

    private static Component deserializeMiniMessage(String message) {
        try {
            Class<?> miniMessageClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            Object miniMessage = miniMessageClass.getMethod("miniMessage").invoke(null);
            Object component = miniMessageClass.getMethod("deserialize", String.class)
                    .invoke(miniMessage, message);
            return component instanceof Component ? (Component) component : AMPERSAND.deserialize(message);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return AMPERSAND.deserialize(message);
        }
    }

    private static String setPlaceholders(Player player, String message) {
        if (player == null || !Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return message;
        }

        try {
            Class<?> placeholderApi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Object parsed = placeholderApi
                    .getMethod("setPlaceholders", OfflinePlayer.class, String.class)
                    .invoke(null, player, message);
            return parsed instanceof String ? (String) parsed : message;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return message;
        }
    }
}
