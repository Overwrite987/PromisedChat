package ru.overwrite.chat.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {

    private static final Pattern colorPattern = Pattern.compile("&([0-9a-fA-Fklmnor])");

    private static final Object2ObjectMap<String, ChatColor> colorCodesPermissions = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<String, String> colorCodesMap = new Object2ObjectOpenHashMap<>();

    private static final Object2ObjectMap<String, ChatColor> colorStylesPermissions = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<String, String> colorStylesMap = new Object2ObjectOpenHashMap<>();

    static {
        colorCodesPermissions.put("pchat.color.black", ChatColor.BLACK);
        colorCodesPermissions.put("pchat.color.dark_blue", ChatColor.DARK_BLUE);
        colorCodesPermissions.put("pchat.color.dark_green", ChatColor.DARK_GREEN);
        colorCodesPermissions.put("pchat.color.dark_aqua", ChatColor.DARK_AQUA);
        colorCodesPermissions.put("pchat.color.dark_red", ChatColor.DARK_RED);
        colorCodesPermissions.put("pchat.color.dark_purple", ChatColor.DARK_PURPLE);
        colorCodesPermissions.put("pchat.color.gold", ChatColor.GOLD);
        colorCodesPermissions.put("pchat.color.gray", ChatColor.GRAY);
        colorCodesPermissions.put("pchat.color.dark_gray", ChatColor.DARK_GRAY);
        colorCodesPermissions.put("pchat.color.blue", ChatColor.BLUE);
        colorCodesPermissions.put("pchat.color.green", ChatColor.GREEN);
        colorCodesPermissions.put("pchat.color.aqua", ChatColor.AQUA);
        colorCodesPermissions.put("pchat.color.red", ChatColor.RED);
        colorCodesPermissions.put("pchat.color.light_purple", ChatColor.LIGHT_PURPLE);
        colorCodesPermissions.put("pchat.color.yellow", ChatColor.YELLOW);
        colorCodesPermissions.put("pchat.color.white", ChatColor.WHITE);
        colorStylesPermissions.put("pchat.style.obfuscated", ChatColor.MAGIC);
        colorStylesPermissions.put("pchat.style.bold", ChatColor.BOLD);
        colorStylesPermissions.put("pchat.style.strikethrough", ChatColor.STRIKETHROUGH);
        colorStylesPermissions.put("pchat.style.underline", ChatColor.UNDERLINE);
        colorStylesPermissions.put("pchat.style.italic", ChatColor.ITALIC);
        colorStylesPermissions.put("pchat.style.reset", ChatColor.RESET);

        colorCodesMap.put("0", "black");
        colorCodesMap.put("1", "dark_blue");
        colorCodesMap.put("2", "dark_green");
        colorCodesMap.put("3", "dark_aqua");
        colorCodesMap.put("4", "dark_red");
        colorCodesMap.put("5", "dark_purple");
        colorCodesMap.put("6", "gold");
        colorCodesMap.put("7", "gray");
        colorCodesMap.put("8", "dark_gray");
        colorCodesMap.put("9", "blue");
        colorCodesMap.put("a", "green");
        colorCodesMap.put("b", "aqua");
        colorCodesMap.put("c", "red");
        colorCodesMap.put("d", "light_purple");
        colorCodesMap.put("e", "yellow");
        colorCodesMap.put("f", "white");

        colorStylesMap.put("l", "bold");
        colorStylesMap.put("k", "obfuscated");
        colorStylesMap.put("m", "strikethrough");
        colorStylesMap.put("n", "underline");
        colorStylesMap.put("o", "italic");
        colorStylesMap.put("r", "reset");
    }

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");
    private static final char COLOR_CHAR = '§';

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder builder = new StringBuilder(message.length() + 32);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(builder,
                    COLOR_CHAR + "x" +
                            COLOR_CHAR + group.charAt(0) +
                            COLOR_CHAR + group.charAt(1) +
                            COLOR_CHAR + group.charAt(2) +
                            COLOR_CHAR + group.charAt(3) +
                            COLOR_CHAR + group.charAt(4) +
                            COLOR_CHAR + group.charAt(5));
        }
        message = matcher.appendTail(builder).toString();
        return translateAlternateColorCodes('&', message);
    }

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        final char[] b = textToTranslate.toCharArray();

        for (int i = 0, length = b.length - 1; i < length; ++i) {
            if (b[i] == altColorChar && isValidColorCharacter(b[i + 1])) {
                b[i++] = COLOR_CHAR;
                b[i] |= 0x20;
            }
        }

        return new String(b);
    }

    private static boolean isValidColorCharacter(char c) {
        return switch (c) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D',
                 'E', 'F', 'r', 'R', 'k', 'K', 'l', 'L', 'm', 'M', 'n', 'N', 'o', 'O', 'x', 'X' -> true;
            default -> false;
        };
    }

    public static String replacePlaceholders(Player player, String message) {
        if (PlaceholderAPI.containsPlaceholders(message)) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        return message;
    }

    public static String getTime(int time, String hoursMark, String minutesMark, String secondsMark) {
        final int hours = getHours(time);
        final int minutes = getMinutes(time);
        final int seconds = getSeconds(time);

        final StringBuilder result = new StringBuilder();

        if (hours > 0) {
            result.append(hours).append(hoursMark);
        }

        if (minutes > 0 || hours > 0) {
            result.append(minutes).append(minutesMark);
        }

        result.append(seconds).append(secondsMark);

        return result.toString();
    }

    public static int getHours(int time) {
        return time / 3600;
    }

    public static int getMinutes(int time) {
        return (time % 3600) / 60;
    }

    public static int getSeconds(int time) {
        return time % 60;
    }

    public static String replaceEach(String text, String[] searchList, String[] replacementList) {
        if (text == null || text.isEmpty() || searchList.length == 0 || replacementList.length == 0) {
            return text;
        }

        if (searchList.length != replacementList.length) {
            throw new IllegalArgumentException("Search and replacement arrays must have the same length.");
        }

        final StringBuilder result = new StringBuilder(text);

        for (int i = 0; i < searchList.length; i++) {
            String search = searchList[i];
            String replacement = replacementList[i];

            int start = 0;

            while ((start = result.indexOf(search, start)) != -1) {
                result.replace(start, start + search.length(), replacement);
                start += replacement.length();
            }
        }

        return result.toString();
    }

    public static String formatByPerm(Player player, String message) {
        if (player.hasPermission("pchat.style.hex")) {
            return colorize(message);
        }
        final Matcher matcher = colorPattern.matcher(message);
        final char colorChar = '&';
        while (matcher.find()) {
            final String code = matcher.group(1);
            final String colorPerm = "pchat.color." + colorCodesMap.get(code);
            final String stylePerm = "pchat.style." + colorStylesMap.get(code);
            final ChatColor color = colorCodesPermissions.get(colorPerm);
            final ChatColor style = colorStylesPermissions.get(stylePerm);

            if (color != null && player.hasPermission(colorPerm)) {
                message = message.replace(colorChar + code, color.toString());
            }
            if (style != null && player.hasPermission(stylePerm)) {
                message = message.replace(colorChar + code, style.toString());
            }
        }
        return message;
    }

}
