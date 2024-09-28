package ru.overwrite.chat.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;

import net.md_5.bungee.api.ChatColor;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import ru.overwrite.api.commons.StringUtils;

public class Utils {

    private static final Pattern colorPattern = Pattern.compile("&([0-9a-fA-Fklmnor])");

    private static final Map<String, ChatColor> colorCodesPermissions = new HashMap<>();
    private static final Map<String, String> colorCodesMap = new HashMap<>();

    private static final Map<String, ChatColor> colorStylesPermissions = new HashMap<>();
    private static final Map<String, String> colorStylesMap = new HashMap<>();

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

    public static String replacePlaceholders(Player player, String message) {
        if (PlaceholderAPI.containsPlaceholders(message)) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        return message;
    }

    public static String formatByPerm(Player player, String message) {
        if (player.hasPermission("pchat.style.hex")) {
            return StringUtils.colorize(message);
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
