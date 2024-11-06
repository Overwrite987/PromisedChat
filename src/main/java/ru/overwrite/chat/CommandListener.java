package ru.overwrite.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import ru.overwrite.api.commons.StringUtils;
import ru.overwrite.chat.utils.Config;

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
        var m = e.getMessage().split(" ", 1);
        final var command = m[0];
        var t = (System.currentTimeMillis() - p.getFirstPlayed()) / 1000;
        if (!p.hasPermission("pchat.bypass.newbie") && t <= pluginConfig.newbieCooldown) {
            for (var cmd : pluginConfig.newbieCommands) {
                if (command.startsWith(cmd + " ") || command.equalsIgnoreCase(cmd)) {
                    var cd = StringUtils.getTime((int) (pluginConfig.newbieCooldown - t), " ч. ", " мин. ", " сек. ");
                    p.sendMessage(pluginConfig.newbieMessage.replace("%time%", cd));
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }
}
