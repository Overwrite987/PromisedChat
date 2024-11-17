package ru.overwrite.chat;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import ru.overwrite.chat.utils.Config;
import ru.overwrite.chat.utils.Utils;

public class AutoMessages {

    private final PromisedChat plugin;
    private final Config pluginConfig;

    public AutoMessages(PromisedChat plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    public void startMSG(FileConfiguration config) {
        (new BukkitRunnable() {
            public void run() {
                if (!pluginConfig.autoMessage) {
                    return;
                }
                List<String> amsg = getAutoMessage();
                if (amsg == null || amsg.isEmpty()) {
                    return;
                }
                for (var p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission("pchat.automessage")) {
                        continue;
                    }
                    for (var msg : amsg) {
                        p.sendMessage(Utils.colorize(msg));
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