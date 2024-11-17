package ru.overwrite.chat;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent;

import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent;

import ru.overwrite.chat.utils.Config;
import ru.overwrite.chat.utils.ExpiringMap;
import ru.overwrite.chat.utils.Utils;

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
        var p = e.getPlayer();
        if (checkNewbie(p, e)) {
            return;
        }
        var name = p.getName();
        var msg = e.getMessage();
        var pfx = plugin.getChat().getPlayerPrefix(p);
        var sfx = plugin.getChat().getPlayerSuffix(p);
        var gm = removeGlobalPrefix(msg);
        String[] replacementList = {name, pfx, sfx, getDonatePlaceholder(p)};
        if (pluginConfig.forceGlobal || (msg.charAt(0) == '!' && !gm.isBlank())) {
            if (processCooldown(e, p, name, globalCooldowns, pluginConfig.globalRateLimit)) {
                return;
            }
            var gf = Utils.colorize(Utils.replacePlaceholders(p, Utils.replaceEach(pluginConfig.globalFormat, searchList, replacementList)));
            var cmsg = Utils.formatByPerm(p, gm);
            if (pluginConfig.hoverText) {
                e.setCancelled(true);
                sendHover(p, replacementList, gf, new ArrayList<>(Bukkit.getOnlinePlayers()), cmsg);
                return;
            }
            e.setFormat(getFormatWithMessage(gf, cmsg));
            return;
        }
        if (processCooldown(e, p, name, localCooldowns, pluginConfig.localRateLimit)) {
            return;
        }
        var lf = Utils.colorize(Utils.replacePlaceholders(p, Utils.replaceEach(pluginConfig.localFormat, searchList, replacementList)));
        e.getRecipients().clear();
        e.getRecipients().add(p);
        List<Player> rInf = getRadius(p);
        if (!rInf.isEmpty()) {
            e.getRecipients().addAll(rInf);
        }
        var cmsg = Utils.formatByPerm(p, msg);
        if (pluginConfig.hoverText) {
            rInf.add(p);
            e.setCancelled(true);
            sendHover(p, replacementList, lf, rInf, cmsg);
            return;
        }
        e.setFormat(getFormatWithMessage(lf, cmsg));
    }

    private boolean checkNewbie(Player p, Cancellable e) {
        if (pluginConfig.newbieChat) {
            if (p.hasPermission("pchat.bypass.newbie")) {
                return false;
            }
            var time = (System.currentTimeMillis() - p.getFirstPlayed()) / 1000;
            if (time <= pluginConfig.newbieCooldown) {
                var cooldown = Utils.getTime((int) (pluginConfig.newbieCooldown - time), " ч. ", " мин. ", " сек. ");
                p.sendMessage(pluginConfig.newbieMessage.replace("%time%", cooldown));
                e.setCancelled(true);
                return true;
            }
        }
        return false;
    }

    private void sendHover(Player p, String[] replacementList, String format, List<Player> recipients, String chatMessage) {
        var ht = Utils.colorize(Utils.replacePlaceholders(p, Utils.replaceEach(pluginConfig.hoverMessage, searchList, replacementList)));
        var hover = new HoverEvent(Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(ht)));
        var fcm = getFormatWithMessage(format, chatMessage);
        BaseComponent[] comp = TextComponent.fromLegacyText(fcm);
        for (var component : comp) {
            component.setHoverEvent(hover);
        }
        for (var ps : recipients) {
            ps.spigot().sendMessage(comp);
        }
        // Костыли... костыли вечны.
        Bukkit.getConsoleSender().sendMessage(fcm);
    }

    private boolean processCooldown(Cancellable e, Player p, String name, ExpiringMap<String, Long> playerCooldown, long rateLimit) {
        if (p.hasPermission("pchat.bypass.cooldown")) {
            return false;
        }
        if (playerCooldown.containsKey(name)) {
            var cooldown = Utils.getTime((int) (rateLimit / 1000 + (playerCooldown.get(name) - System.currentTimeMillis()) / 1000), " ч. ", " мин. ", " сек. ");
            p.sendMessage(pluginConfig.tooFast.replace("%time%", cooldown));
            e.setCancelled(true);
            return true;
        }
        playerCooldown.put(name, System.currentTimeMillis());
        return false;
    }

    private List<Player> getRadius(Player p) {
        List<Player> plist = new ArrayList<>();
        var maxDist = Math.pow(pluginConfig.chatRadius, 2.0D);
        final var loc = p.getLocation();
        for (var ps : Bukkit.getOnlinePlayers()) {
            if (ps.getWorld() != p.getWorld()) {
                continue;
            }
            var dist = loc.distanceSquared(ps.getLocation()) <= maxDist;
            if (ps != p && dist) {
                plist.add(ps);
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
        var pgroup = plugin.getPerms().getPrimaryGroup(p);
        return pluginConfig.perGroupColor.getOrDefault(pgroup, "");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        var name = e.getPlayer().getName();
        localCooldowns.remove(name);
        globalCooldowns.remove(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(PlayerKickEvent e) {
        var name = e.getPlayer().getName();
        localCooldowns.remove(name);
        globalCooldowns.remove(name);
    }
}
