package ru.overwrite.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import ru.overwrite.api.commons.TimeUtils;
import ru.overwrite.chat.utils.Config;
import ru.overwrite.chat.utils.Utils;

public class CommandListener implements Listener {

    private final Config pluginConfig;

    public CommandListener(PromisedChat plugin) {
        pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(ignoreCancelled = true)
    public void playerCommand(PlayerCommandPreprocessEvent e) {
        if (!pluginConfig.newbieChat) {
            return;
        }
        Player player = e.getPlayer();
        String[] message = e.getMessage().split(" ", 1);
        final String command = message[0];
        long time = (System.currentTimeMillis() - player.getFirstPlayed()) / 1000;
        if (!player.hasPermission("pchat.bypass.newbie") && time <= pluginConfig.newbieCooldown) {
            for (String cmd : pluginConfig.newbieCommands) {
                if (command.startsWith(cmd + " ") || command.equalsIgnoreCase(cmd)) {
                    String cd = TimeUtils.getTime((int) (pluginConfig.newbieCooldown - time), " ч. ", " мин. ", " сек. ");
                    player.sendMessage(pluginConfig.newbieMessage.replace("%time%", cd));
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }
}
