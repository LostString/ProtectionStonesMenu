package dev.loststr1ng.protectionStonesMenu.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MessageUtils {

    private static final LegacyComponentSerializer AMPERSAND =
            LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer SECTION =
            LegacyComponentSerializer.legacySection();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .tags(TagResolver.resolver(TagResolver.standard(), spriteTagResolver()))
            .build();

    public static Component getColoredMessage(String message) {
        return getColoredMessage(null, message);
    }

    public static Component getColoredMessage(Player player, String message) {
        String parsed = setPlaceholders(player, message == null ? "" : message);
        Component component = containsMiniMessageTag(parsed)
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

    public static String getLegacyFallback(Player player, String message){
        String parsed = setPlaceholders(player, message == null ? "" : message);
        if (containsMiniMessageTag(parsed)) {
            return AMPERSAND.serialize(AMPERSAND.deserialize(MINI_MESSAGE.stripTags(parsed)));
        }
        return getLegacy(player, parsed);
    }

    public static List<String> getColoredList(List<String> list){
        return getColoredList(null, list);
    }

    public static List<String> getColoredList(Player player, List<String> list){
        List<String> coloredList = new ArrayList<>();
        for(String l: list){
            coloredList.add(getLegacyFallback(player, l));
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

    public static boolean containsMiniMessageTag(String message) {
        return message.indexOf('<') >= 0 && message.indexOf('>') > message.indexOf('<');
    }

    private static Component deserializeMiniMessage(String message) {
        try {
            return MINI_MESSAGE.deserialize(message);
        } catch (ParsingException ignored) {
            return AMPERSAND.deserialize(message);
        }
    }

    private static TagResolver spriteTagResolver() {
        return TagResolver.resolver("sprite", (arguments, context) ->
                Tag.selfClosingInserting(createSpriteComponent(arguments)));
    }

    private static Component createSpriteComponent(ArgumentQueue arguments) {
        List<String> parts = new ArrayList<>();
        while (arguments.hasNext()) {
            parts.add(arguments.pop().value());
        }

        if (parts.isEmpty()) {
            return Component.empty();
        }

        String atlas;
        String sprite;
        if (parts.size() == 1) {
            atlas = "minecraft:blocks";
            sprite = parts.get(0);
        } else if (parts.size() == 2) {
            atlas = parts.get(0);
            sprite = parts.get(1);
        } else {
            atlas = parts.get(0) + ":" + parts.get(1);
            sprite = String.join(":", parts.subList(2, parts.size()));
        }

        try {
            Class<?> keyClass = Class.forName("net.kyori.adventure.key.Key");
            Object atlasKey = keyClass.getMethod("key", String.class).invoke(null, atlas);
            Object spriteKey = keyClass.getMethod("key", String.class).invoke(null, sprite);

            Class<?> contentsClass = Class.forName("net.kyori.adventure.text.object.ObjectContents");
            Class<?> spriteContentsClass = Class.forName("net.kyori.adventure.text.object.SpriteObjectContentsImpl");
            Constructor<?> constructor = spriteContentsClass.getDeclaredConstructor(keyClass, keyClass);
            constructor.setAccessible(true);
            Object contents = constructor.newInstance(atlasKey, spriteKey);

            Method objectComponent = Component.class.getMethod("object", contentsClass);
            return (Component) objectComponent.invoke(null, contents);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return Component.empty();
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
