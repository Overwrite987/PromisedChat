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

import ru.overwrite.api.cache.ExpiringMap;
import ru.overwrite.api.commons.StringUtils;
import ru.overwrite.api.commons.TimeUtils;
import ru.overwrite.chat.utils.Config;
import ru.overwrite.chat.utils.Utils;

public class ChatListener implements Listener {

    private final PromisedChat instance;
    private final Config pluginConfig;
    private final ExpiringMap<String, Long> localCooldowns;
    private final ExpiringMap<String, Long> globalCooldowns;

    public ChatListener(PromisedChat plugin) {
        instance = plugin;
        pluginConfig = plugin.getPluginConfig();
        localCooldowns = new ExpiringMap<>(pluginConfig.localRateLimit, TimeUnit.MILLISECONDS);
        globalCooldowns = new ExpiringMap<>(pluginConfig.globalRateLimit, TimeUnit.MILLISECONDS);
    }

    private final String[] searchList = {"<player>", "<prefix>", "<suffix>", "<dph>"};

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerChatSent(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (pluginConfig.newbieChat) {
            long time = (System.currentTimeMillis() - p.getFirstPlayed()) / 1000;
            if (!p.hasPermission("pchat.bypass.newbie") && time <= pluginConfig.newbieCooldown) {
                String cd = TimeUtils.getTime((int) (pluginConfig.newbieCooldown - time), " ч. ", " мин. ", " сек. ");
                p.sendMessage(pluginConfig.newbieMessage.replace("%time%", cd));
                e.setCancelled(true);
                return;
            }
        }
        String name = p.getName();
        String message = e.getMessage();
        String prefix = instance.getChat().getPlayerPrefix(p);
        String suffix = instance.getChat().getPlayerSuffix(p);
        String globalMessage = removeGlobalPrefix(message);
        String[] replacementList = {name, prefix, suffix, getDonatePlaceholder(p)};
        if (pluginConfig.forceGlobal || (message.split("")[0].equals("!") && !globalMessage.isBlank())) {
            if (processCooldown(e, p, name, globalCooldowns, pluginConfig.globalRateLimit)) {
                return;
            }
            String gf = StringUtils.colorize(Utils.replacePlaceholders(p, StringUtils.replaceEach(pluginConfig.globalFormat, searchList, replacementList)));
            String chatMessage = Utils.formatByPerm(p, globalMessage);
            if (pluginConfig.hoverText) {
                e.setCancelled(true);
                sendHover(p, replacementList, gf, new ArrayList<>(Bukkit.getOnlinePlayers()), chatMessage);
            } else {
                e.setFormat(gf.replace("<message>", chatMessage).replace("%", "%%"));
            }
            return;
        }
        if (processCooldown(e, p, name, localCooldowns, pluginConfig.localRateLimit)) {
            return;
        }
        String lf = StringUtils.colorize(Utils.replacePlaceholders(p, StringUtils.replaceEach(pluginConfig.localFormat, searchList, replacementList)));
        e.getRecipients().clear();
        e.getRecipients().add(p);
        List<Player> radiusInfo = getRadius(p);
        if (!radiusInfo.isEmpty()) {
            e.getRecipients().addAll(radiusInfo);
        }
        String chatMessage = Utils.formatByPerm(p, message);
        if (pluginConfig.hoverText) {
            radiusInfo.add(p);
            e.setCancelled(true);
            sendHover(p, replacementList, lf, radiusInfo, chatMessage);
        } else {
            e.setFormat(lf.replace("<message>", chatMessage).replace("%", "%%"));
        }
    }

    private void sendHover(Player p, String[] replacementList, String format, List<Player> recipients, String chatMessage) {
        String hovertext = StringUtils.colorize(Utils.replacePlaceholders(p, StringUtils.replaceEach(pluginConfig.hoverMessage, searchList, replacementList)));
        HoverEvent hover = new HoverEvent(Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hovertext)));
        int length = format.replace("<message>", "").replace("%", "%%").length();
        BaseComponent[] comp = TextComponent.fromLegacyText(format.replace("<message>", chatMessage).replace("%", "%%"));
        // Применяем HoverEvent только к первым элементам, а не ко всему сообщению
        for (int i = 0; i < length; i++) {
            comp[i].setHoverEvent(hover);
        }
        for (Player ps : recipients) {
            ps.spigot().sendMessage(comp);
        }
        // Костыли... костыли вечны.
        Bukkit.getConsoleSender().sendMessage(format.replace("<message>", chatMessage).replace("%", "%%"));
    }

    private boolean processCooldown(Cancellable e, Player p, String name, ExpiringMap<String, Long> playerCooldown, long rateLimit) {
        if (p.hasPermission("pchat.bypass.cooldown")) {
            return false;
        }
        if (playerCooldown.containsKey(name)) {
            String cd = TimeUtils.getTime((int) (rateLimit / 1000 + (playerCooldown.get(name) - System.currentTimeMillis()) / 1000), " ч. ", " мин. ", " сек. ");
            p.sendMessage(pluginConfig.tooFast.replace("%time%", cd));
            e.setCancelled(true);
            return true;
        }
        playerCooldown.put(name, System.currentTimeMillis());
        return false;
    }

    private List<Player> getRadius(Player p) {
        List<Player> playerlist = new ArrayList<>();
        for (Player ps : Bukkit.getOnlinePlayers()) {
            if (ps.getWorld() == p.getWorld()) {
                boolean dist = p.getLocation().distanceSquared(ps.getLocation()) <= Math.pow(pluginConfig.chatRadius, 2.0D);
                if (ps != p && dist) {
                    playerlist.add(ps);
                }
            }
        }
        return playerlist;
    }

    public String removeGlobalPrefix(String message) {
        return pluginConfig.forceGlobal ? message : message.substring(1).trim();
    }

    private String getDonatePlaceholder(Player p) {
        String placeholder = "";
        String playerGroup = instance.getPerms().getPrimaryGroup(p);
        if (pluginConfig.perGroupColor.containsKey(playerGroup)) {
            placeholder = pluginConfig.perGroupColor.get(playerGroup);
        }
        return placeholder;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        String name = e.getPlayer().getName();
        localCooldowns.remove(name);
        globalCooldowns.remove(name);
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        String name = e.getPlayer().getName();
        localCooldowns.remove(name);
        globalCooldowns.remove(name);
    }
}
