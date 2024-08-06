package ru.overwrite.chat;

import java.util.logging.Logger;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ru.overwrite.chat.utils.Config;
import ru.overwrite.chat.utils.Metrics;

import net.milkbowl.vault.permission.Permission;
import net.milkbowl.vault.chat.Chat;

public final class PromisedChat extends JavaPlugin {

    @Getter
    private Chat chat;

    @Getter
    private Permission perms;

    @Getter
    private final Config pluginConfig = new Config();

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        if (!isPaper()) {
            return;
        }
        saveDefaultConfig();
        setupConfig();
        setupChat();
        setupPerms();
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new CommandListener(this), this);
        new AutoMessages(this).startMSG(getConfig());
        getCommand("promisedchat").setExecutor(new CommandClass(this));
        new Metrics(this, 20699);
        long endTime = System.currentTimeMillis();
        getLogger().info("Plugin started in " + (endTime - startTime) + " ms");
    }

    public boolean isPaper() {
        if (getServer().getName().equals("CraftBukkit")) {
            Logger logger = getLogger();
            logger.info(" ");
            logger.info("============= ! WARNING ! =============");
            logger.info("Этот плагин работает только на Paper и его форках!");
            logger.info("Автор категорически выступает за отказ от использования устаревшего и уязвимого софта!");
            logger.info("Скачать Paper: https://papermc.io/downloads/all");
            logger.info("============= ! WARNING ! =============");
            logger.info(" ");
            this.setEnabled(false);
            return false;
        }
        return true;
    }

    private void setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager()
                .getRegistration(Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }
    }

    private void setupPerms() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager()
                .getRegistration(Permission.class);
        if (permissionProvider != null) {
            perms = permissionProvider.getProvider();
        }
    }

    public void setupConfig() {
        FileConfiguration config = getConfig();
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
