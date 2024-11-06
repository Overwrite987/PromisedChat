package ru.overwrite.chat;

import lombok.Getter;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import ru.overwrite.chat.utils.Config;
import ru.overwrite.chat.utils.Metrics;

import net.milkbowl.vault.permission.Permission;
import net.milkbowl.vault.chat.Chat;

@Getter
public final class PromisedChat extends JavaPlugin {

    private Chat chat;

    private Permission perms;

    private final Config pluginConfig = new Config();

    @Override
    public void onEnable() {
        long st = System.currentTimeMillis();
        if (!isPaper()) {
            return;
        }
        saveDefaultConfig();
        setupConfig();
        var sm = getServer().getServicesManager();
        setupChat(sm);
        setupPerms(sm);
        var pm = getServer().getPluginManager();
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new CommandListener(this), this);
        new AutoMessages(this).startMSG(getConfig());
        getCommand("promisedchat").setExecutor(new CommandClass(this));
        new Metrics(this, 20699);
        var et = System.currentTimeMillis();
        getLogger().info("Plugin started in " + (et - st) + " ms");
    }

    public boolean isPaper() {
        if (getServer().getName().equals("CraftBukkit")) {
            var l = getLogger();
            l.info(" ");
            l.info("============= ! WARNING ! =============");
            l.info("Этот плагин работает только на Paper и его форках!");
            l.info("Автор категорически выступает за отказ от использования устаревшего и уязвимого софта!");
            l.info("Скачать Paper: https://papermc.io/downloads/all");
            l.info("============= ! WARNING ! =============");
            l.info(" ");
            this.setEnabled(false);
            return false;
        }
        return true;
    }

    private void setupChat(ServicesManager servicesManager) {
        RegisteredServiceProvider<Chat> cp = servicesManager.getRegistration(Chat.class);
        if (cp != null) {
            chat = cp.getProvider();
        }
    }

    private void setupPerms(ServicesManager servicesManager) {
        RegisteredServiceProvider<Permission> pp = servicesManager.getRegistration(Permission.class);
        if (pp != null) {
            perms = pp.getProvider();
        }
    }

    public void setupConfig() {
        var config = getConfig();
        pluginConfig.setupFormats(config);
        pluginConfig.setupHover(config);
        pluginConfig.setupCooldown(config);
        pluginConfig.setupNewbie(config);
        pluginConfig.setupAutoMessage(config);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }
}
