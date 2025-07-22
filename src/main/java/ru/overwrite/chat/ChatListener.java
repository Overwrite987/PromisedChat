package ru.overwrite.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.overwrite.chat.utils.Config;
import ru.overwrite.chat.utils.ExpiringMap;
import ru.overwrite.chat.utils.Utils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatListener implements Listener {

    private final PromisedChat plugin;
    private final Config pluginConfig;
    private final ExpiringMap<String, Long> localCooldowns;
    private final ExpiringMap<String, Long> globalCooldowns;

    public ChatListener(PromisedChat plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.localCooldowns = new ExpiringMap<>(pluginConfig.localRateLimit, TimeUnit.MILLISECONDS);
        this.globalCooldowns = new ExpiringMap<>(pluginConfig.globalRateLimit, TimeUnit.MILLISECONDS);
    }

    private final String[] searchList = {"<player>", "<prefix>", "<suffix>", "<dph>"};

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (checkNewbie(p, e)) {
            return;
        }
        String name = p.getName();
        String message = e.getMessage();
        String prefix = plugin.getChat().getPlayerPrefix(p);
        String suffix = plugin.getChat().getPlayerSuffix(p);
        String globalMessage = removeGlobalPrefix(message);
        String[] replacementList = {name, prefix, suffix, getDonatePlaceholder(p)};
        if (pluginConfig.forceGlobal || (message.charAt(0) == '!' && !globalMessage.isBlank())) {
            if (processCooldown(e, p, name, globalCooldowns, pluginConfig.globalRateLimit)) {
                return;
            }
            String globalFormat = Utils.colorize(Utils.replacePlaceholders(p, Utils.replaceEach(pluginConfig.globalFormat, searchList, replacementList)));
            String colorizedMessage = Utils.formatByPerm(p, globalMessage);
            if (pluginConfig.hoverText) {
                e.setCancelled(true);
                sendHover(p, replacementList, globalFormat, Bukkit.getOnlinePlayers(), colorizedMessage);
                return;
            }
            e.setFormat(getFormatWithMessage(globalFormat, colorizedMessage));
            return;
        }
        if (processCooldown(e, p, name, localCooldowns, pluginConfig.localRateLimit)) {
            return;
        }
        String localFormat = Utils.colorize(Utils.replacePlaceholders(p, Utils.replaceEach(pluginConfig.localFormat, searchList, replacementList)));
        e.getRecipients().clear();
        List<Player> playersInRadius = getRadius(p);
        if (!playersInRadius.isEmpty()) {
            e.getRecipients().addAll(playersInRadius);
        }
        String colorizedMessage = Utils.formatByPerm(p, message);
        if (pluginConfig.hoverText) {
            playersInRadius.add(p);
            e.setCancelled(true);
            sendHover(p, replacementList, localFormat, playersInRadius, colorizedMessage);
            return;
        }
        e.setFormat(getFormatWithMessage(localFormat, colorizedMessage));
    }

    private boolean checkNewbie(Player p, Cancellable e) {
        if (pluginConfig.newbieChat) {
            if (p.hasPermission("pchat.bypass.newbie")) {
                return false;
            }
            long time = (System.currentTimeMillis() - p.getFirstPlayed()) / 1000;
            if (time <= pluginConfig.newbieCooldown) {
                String cooldown = Utils.getTime((int) (pluginConfig.newbieCooldown - time), " ч. ", " мин. ", " сек. ");
                p.sendMessage(pluginConfig.newbieMessage.replace("%time%", cooldown));
                e.setCancelled(true);
                return true;
            }
        }
        return false;
    }

    private void sendHover(Player p, String[] replacementList, String format, Collection<? extends Player> recipients, String chatMessage) {
        String hoverText = Utils.colorize(Utils.replacePlaceholders(p, Utils.replaceEach(pluginConfig.hoverMessage, searchList, replacementList)));
        HoverEvent hoverEvent = new HoverEvent(Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverText)));
        String formatWithMessage = getFormatWithMessage(format, chatMessage);
        BaseComponent[] comp = TextComponent.fromLegacyText(formatWithMessage);
        for (BaseComponent component : comp) {
            component.setHoverEvent(hoverEvent);
        }
        for (Player recipient : recipients) {
            recipient.spigot().sendMessage(comp);
        }
        // Костыли... костыли вечны.
        Bukkit.getConsoleSender().sendMessage(formatWithMessage);
    }

    private boolean processCooldown(Cancellable e, Player p, String name, ExpiringMap<String, Long> playerCooldown, long rateLimit) {
        if (p.hasPermission("pchat.bypass.cooldown")) {
            return false;
        }
        if (playerCooldown.containsKey(name)) {
            String cooldown = Utils.getTime((int) (rateLimit / 1000 + (playerCooldown.get(name) - System.currentTimeMillis()) / 1000), " ч. ", " мин. ", " сек. ");
            p.sendMessage(pluginConfig.tooFast.replace("%time%", cooldown));
            e.setCancelled(true);
            return true;
        }
        playerCooldown.put(name, System.currentTimeMillis());
        return false;
    }

    private List<Player> getRadius(Player p) {
        List<Player> plist = new ObjectArrayList<>();
        double maxDist = Math.pow(pluginConfig.chatRadius, 2.0D);
        Location loc = p.getLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() != p.getWorld()) {
                continue;
            }
            if (loc.distanceSquared(player.getLocation()) <= maxDist) {
                plist.add(player);
            }
        }
        return plist;
    }

    private String removeGlobalPrefix(String message) {
        return pluginConfig.forceGlobal ? message : message.substring(1).trim();
    }

    private String getFormatWithMessage(String format, String chatMessage) {
        return format
                .replace("<message>", chatMessage)
                .replace("%", "%%"); // Это надо чтобы PAPI не выёбывался
    }

    private String getDonatePlaceholder(Player p) {
        String primaryGroup = plugin.getPerms().getPrimaryGroup(p);
        return pluginConfig.perGroupColor.getOrDefault(primaryGroup, "");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        String name = e.getPlayer().getName();
        localCooldowns.remove(name);
        globalCooldowns.remove(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(PlayerKickEvent e) {
        String name = e.getPlayer().getName();
        localCooldowns.remove(name);
        globalCooldowns.remove(name);
    }
}
