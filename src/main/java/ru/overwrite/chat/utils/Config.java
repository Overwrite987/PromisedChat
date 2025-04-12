package ru.overwrite.chat.utils;

import it.unimi.dsi.fastutil.objects.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Config {

    public boolean newbieChat, autoMessage, hoverText;
    public int newbieCooldown;
    public String newbieMessage;
    public ObjectSet<String> newbieCommands;

    public int chatRadius, autoMessageInterval;

    public boolean forceGlobal, isRandom;

    public String localFormat;
    public String globalFormat;

    public String hoverMessage;

    public String tooFast;

    public long localRateLimit, globalRateLimit;

    public Object2ObjectMap<String, String> perGroupColor;

    public ObjectList<List<String>> autoMessages;

    public void setupFormats(FileConfiguration config) {
        ConfigurationSection format = config.getConfigurationSection("format");
        localFormat = format.getString("local");
        chatRadius = format.getInt("localRadius");
        globalFormat = format.getString("global");
        forceGlobal = format.getBoolean("forceGlobal");
        ConfigurationSection donatePlaceholders = config.getConfigurationSection("donatePlaceholders");
        perGroupColor = new Object2ObjectOpenHashMap<>();
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
        tooFast = Utils.colorize(cooldown.getString("cooldownMessage"));
    }

    public void setupNewbie(FileConfiguration config) {
        ConfigurationSection newbieChat = config.getConfigurationSection("newbieChat");
        this.newbieChat = newbieChat.getBoolean("enable");
        newbieCooldown = newbieChat.getInt("newbieCooldown");
        newbieMessage = Utils.colorize(newbieChat.getString("newbieChatMessage"));
        newbieCommands = new ObjectOpenHashSet<>(newbieChat.getStringList("newbieCommands"));
    }

    public void setupAutoMessage(FileConfiguration config) {
        ConfigurationSection autoMessage = config.getConfigurationSection("autoMessage");
        this.autoMessage = autoMessage.getBoolean("enable");
        if (!this.autoMessage) {
            return;
        }
        autoMessages = new ObjectArrayList<>();
        ConfigurationSection messages = autoMessage.getConfigurationSection("messages");
        for (String messageName : messages.getKeys(false)) {
            autoMessages.add(messages.getStringList(messageName));
        }
        isRandom = autoMessage.getBoolean("random");
        autoMessageInterval = autoMessage.getInt("messageInterval");
    }
}
