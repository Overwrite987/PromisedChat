package ru.overwrite.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import ru.overwrite.chat.utils.Config;
import ru.overwrite.chat.utils.Utils;

public class CommandListener implements Listener {

    private final Config pluginConfig;

    public CommandListener(PromisedChat plugin) {
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(ignoreCancelled = true)
    public void playerCommand(PlayerCommandPreprocessEvent e) {
        if (!pluginConfig.newbieChat) {
            return;
        }
        var p = e.getPlayer();
        final var command = cutCommand(e.getMessage());
        var time = (System.currentTimeMillis() - p.getFirstPlayed()) / 1000;
        if (!p.hasPermission("pchat.bypass.newbie") && time <= pluginConfig.newbieCooldown) {
            for (var cmd : pluginConfig.newbieCommands) {
                if (command.equalsIgnoreCase(cmd)) {
                    var cooldown = Utils.getTime((int) (pluginConfig.newbieCooldown - time), " ч. ", " мин. ", " сек. ");
                    p.sendMessage(pluginConfig.newbieMessage.replace("%time%", cooldown));
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    private String cutCommand(String str) {
        int index = str.indexOf(' ');
        return index == -1 ? str : str.substring(0, index);
    }
}
