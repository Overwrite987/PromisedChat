package ru.overwrite.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import ru.overwrite.api.commons.TimeUtils;
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
        var player = e.getPlayer();
        var message = e.getMessage().split(" ", 1);
        final var command = message[0];
        var time = (System.currentTimeMillis() - player.getFirstPlayed()) / 1000;
        if (!player.hasPermission("pchat.bypass.newbie") && time <= pluginConfig.newbieCooldown) {
            for (var cmd : pluginConfig.newbieCommands) {
                if (command.startsWith(cmd + " ") || command.equalsIgnoreCase(cmd)) {
                    var cd = TimeUtils.getTime((int) (pluginConfig.newbieCooldown - time), " ч. ", " мин. ", " сек. ");
                    player.sendMessage(pluginConfig.newbieMessage.replace("%time%", cd));
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }
}
