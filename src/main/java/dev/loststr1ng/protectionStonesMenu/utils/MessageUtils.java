package dev.loststr1ng.protectionStonesMenu.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {

    public static Component getColoredMessage(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message)
                .decoration(TextDecoration.ITALIC, false);
    }

    public static String getLegacy(String message){
        Component component = getColoredMessage(message);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static List<String> getColoredList(List<String> list){
        List<String> coloredList = new ArrayList<>();
        for(String l: list){
            coloredList.add(getLegacy(l));
        }

        return coloredList;
    }

    public static List<Component> components(List<String> list){
        List<Component> coloredList = new ArrayList<>();
        for(String l: list){
            coloredList.add(getColoredMessage(l));
        }
        return coloredList;
    }
}
