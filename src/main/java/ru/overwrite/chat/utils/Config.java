package ru.overwrite.chat.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.api.commons.StringUtils;

public class Config {

    public boolean newbieChat, autoMessage, hoverText;
    public int newbieCooldown;
    public String newbieMessage;
    public Set<String> newbieCommands;

    public int chatRadius;

    public boolean forceGlobal, isRandom;

    public String localFormat;
    public String globalFormat;

    public String hoverMessage;

    public String tooFast;

    public long localRateLimit, globalRateLimit;

    public Map<String, String> perGroupColor;

    public Int2ObjectOpenHashMap<List<String>> autoMessages;

    public void setupFormats(FileConfiguration config) {
        ConfigurationSection format = config.getConfigurationSection("format");
        localFormat = format.getString("local");
        chatRadius = format.getInt("localRadius");
        globalFormat = format.getString("global");
        forceGlobal = format.getBoolean("forceGlobal");
        ConfigurationSection donatePlaceholders = config.getConfigurationSection("donatePlaceholders");
        perGroupColor = new HashMap<>();
        for (String groupName : donatePlaceholders.getKeys(false)) {
            String color = donatePlaceholders.getString(groupName);
            perGroupColor.put(groupName, color);
        }
    }

    public void setupHover(FileConfiguration config) {
        ConfigurationSection hoverText = config.getConfigurationSection("hoverText");
        this.hoverText = hoverText.getBoolean("enable");
        hoverMessage = hoverText.getString("format");
    }

    public void setupCooldown(FileConfiguration config) {
        ConfigurationSection cooldown = config.getConfigurationSection("cooldown");
        localRateLimit = cooldown.getLong("localCooldown");
        globalRateLimit = cooldown.getLong("globalCooldown");
        tooFast = StringUtils.colorize(cooldown.getString("cooldownMessage"));
    }

    public void setupNewbie(FileConfiguration config) {
        ConfigurationSection newbieChat = config.getConfigurationSection("newbieChat");
        this.newbieChat = newbieChat.getBoolean("enable");
        newbieCooldown = newbieChat.getInt("newbieCooldown");
        newbieMessage = StringUtils.colorize(newbieChat.getString("newbieChatMessage"));
        newbieCommands = new HashSet<>(newbieChat.getStringList("newbieCommands"));
    }

    public void setupAutoMessage(FileConfiguration config) {
        ConfigurationSection autoMessage = config.getConfigurationSection("autoMessage");
        this.autoMessage = autoMessage.getBoolean("enable");
        if (!this.autoMessage) {
            return;
        }
        autoMessages = new Int2ObjectOpenHashMap<>();
        ConfigurationSection messages = autoMessage.getConfigurationSection("messages");
        for (String messageName : messages.getKeys(false)) {
            if (!StringUtils.isNumeric(messageName)) {
                break;
            }
            int messageID = Integer.parseInt(messageName);
            autoMessages.put(messageID, messages.getStringList(messageName));
        }
        isRandom = autoMessage.getBoolean("random");
    }
}
