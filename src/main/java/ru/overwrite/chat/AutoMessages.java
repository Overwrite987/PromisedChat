package ru.overwrite.chat;

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ru.overwrite.api.commons.StringUtils;
import ru.overwrite.chat.utils.Config;

public class AutoMessages {

    private final PromisedChat plugin;
    private final Config pluginConfig;

    public AutoMessages(PromisedChat plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
    }

    public void startMSG(FileConfiguration config) {
        (new BukkitRunnable() {
            public void run() {
                if (!pluginConfig.autoMessage) {
                    return;
                }
                List<String> amsg = getAutoMessage();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission("pchat.automessage")) {
                        continue;
                    }
                    for (String message : amsg) {
                        p.sendMessage(StringUtils.colorize(message));
                    }
                }
            }
        }).runTaskTimerAsynchronously(plugin, 20L, config.getInt("autoMessage.messageInterval") * 20L);
    }

    private int i = 0;

    private List<String> getAutoMessage() {
        if (pluginConfig.isRandom) {
            return pluginConfig.autoMessages.get(getRandomKey(pluginConfig.autoMessages.keySet()));
        }
        if (i++ >= pluginConfig.autoMessages.keySet().size()) {
            i = 0;
        }
        return pluginConfig.autoMessages.get(i);
    }

    private int getRandomKey(IntSet intSet) {
        return ThreadLocalRandom.current().nextInt(intSet.size());
    }
}