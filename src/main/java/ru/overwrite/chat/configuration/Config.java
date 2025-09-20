package ru.overwrite.chat.configuration;

import it.unimi.dsi.fastutil.objects.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.chat.utils.Utils;

import java.util.List;
import java.util.Set;

public class Config {

    public boolean newbieChat, autoMessage, hoverText, clickEvent;
    public int newbieCooldown;
    public String newbieMessage;
    public ObjectSet<String> newbieCommands;

    public int chatRadius, autoMessageInterval;

    public boolean forceGlobal, isRandom;

    public String localFormat;
    public String globalFormat;

    public String hoverMessage;
    public String clickAction;
    public String clickActionValue;

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
        Set<String> keys = donatePlaceholders.getKeys(false);
        perGroupColor = new Object2ObjectOpenHashMap<>(keys.size());
        for (String groupName : keys) {
            String color = donatePlaceholders.getString(groupName);
            perGroupColor.put(groupName, color);
        }
    }

    public void setupHover(FileConfiguration config) {
        ConfigurationSection hoverText = config.getConfigurationSection("hoverText");
        this.hoverText = hoverText.getBoolean("enable");
        hoverMessage = hoverText.getString("format");
        ConfigurationSection clickEvent = hoverText.getConfigurationSection("clickEvent");
        if (clickEvent != null) {
            this.clickEvent = clickEvent.getBoolean("enable");
            clickAction = clickEvent.getString("actionType");
            clickActionValue = clickEvent.getString("actionValue");
        }
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
        ConfigurationSection messages = autoMessage.getConfigurationSection("messages");
        Set<String> keys = messages.getKeys(false);
        autoMessages = new ObjectArrayList<>(keys.size());
        for (String messageName : keys) {
            autoMessages.add(messages.getStringList(messageName));
        }
        isRandom = autoMessage.getBoolean("random");
        autoMessageInterval = autoMessage.getInt("messageInterval");
    }
}
