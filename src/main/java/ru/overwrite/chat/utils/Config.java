package ru.overwrite.chat.utils;

import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public boolean newbieChat, autoMessage, hoverText;
    public int newbieCooldown;
    public String newbieMessage;
    public ObjectSet<String> newbieCommands;

    public int chatRadius;

    public boolean forceGlobal, isRandom;

    public String localFormat;
    public String globalFormat;

    public String hoverMessage;

    public String tooFast;

    public long localRateLimit, globalRateLimit;

    public Object2ObjectMap<String, String> perGroupColor;

    public Int2ObjectMap<List<String>> autoMessages;

    public void setupFormats(FileConfiguration config) {
        var format = config.getConfigurationSection("format");
        localFormat = format.getString("local");
        chatRadius = format.getInt("localRadius");
        globalFormat = format.getString("global");
        forceGlobal = format.getBoolean("forceGlobal");
        var donatePlaceholders = config.getConfigurationSection("donatePlaceholders");
        perGroupColor = new Object2ObjectOpenHashMap<>();
        for (String groupName : donatePlaceholders.getKeys(false)) {
            String color = donatePlaceholders.getString(groupName);
            perGroupColor.put(groupName, color);
        }
    }

    public void setupHover(FileConfiguration config) {
        var hoverText = config.getConfigurationSection("hoverText");
        this.hoverText = hoverText.getBoolean("enable");
        hoverMessage = hoverText.getString("format");
    }

    public void setupCooldown(FileConfiguration config) {
        var cooldown = config.getConfigurationSection("cooldown");
        localRateLimit = cooldown.getLong("localCooldown");
        globalRateLimit = cooldown.getLong("globalCooldown");
        tooFast = Utils.colorize(cooldown.getString("cooldownMessage"));
    }

    public void setupNewbie(FileConfiguration config) {
        var newbieChat = config.getConfigurationSection("newbieChat");
        this.newbieChat = newbieChat.getBoolean("enable");
        newbieCooldown = newbieChat.getInt("newbieCooldown");
        newbieMessage = Utils.colorize(newbieChat.getString("newbieChatMessage"));
        newbieCommands = new ObjectOpenHashSet<>(newbieChat.getStringList("newbieCommands"));
    }

    public void setupAutoMessage(FileConfiguration config) {
        var autoMessage = config.getConfigurationSection("autoMessage");
        this.autoMessage = autoMessage.getBoolean("enable");
        if (!this.autoMessage) {
            return;
        }
        autoMessages = new Int2ObjectOpenHashMap<>();
        var messages = autoMessage.getConfigurationSection("messages");
        for (var messageName : messages.getKeys(false)) {
            if (!Utils.isNumeric(messageName)) {
                break;
            }
            var messageID = Integer.parseInt(messageName);
            autoMessages.put(messageID, messages.getStringList(messageName));
        }
        isRandom = autoMessage.getBoolean("random");
    }
}
