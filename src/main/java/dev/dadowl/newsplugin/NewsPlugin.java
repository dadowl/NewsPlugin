package dev.dadowl.newsplugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class NewsPlugin extends JavaPlugin {

    public static NewsPlugin instance = null;

    public static int VK_APP = 0;
    public static String VK_SECRET = "";
    public static int VK_GROUP = 0;

    public static boolean ready = false;

    public NewsPlugin() {
        if (NewsPlugin.instance != null) {
            throw new Error("Plugin already initialized");
        }

        NewsPlugin.instance = this;
    }

    public static NewsPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {

        File cFile = new File(getDataFolder(), "config.yml");
        if (!cFile.exists()) {
            this.getConfig().options().copyDefaults(true);
            this.saveDefaultConfig();
        }

        if (getConfig().getConfigurationSection("vk") == null
            || getConfig().getConfigurationSection("vk").getInt("app_id") == 0
            || getConfig().getConfigurationSection("vk").getString("secret_key") == null
            || getConfig().getConfigurationSection("vk").getInt("group_id") == 0){
            getLogger().severe("Данные для ВК не указаны в конфиге. Отключаем плагин.");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        VK_APP = getConfig().getConfigurationSection("vk").getInt("app_id");
        VK_SECRET = getConfig().getConfigurationSection("vk").getString("secret_key");
        VK_GROUP = getConfig().getConfigurationSection("vk").getInt("group_id");

        getCommand("news").setExecutor(new NewsCommand(this));
        Bukkit.getPluginManager().registerEvents(new Handler(this), this);


        VkManager.update();
    }

    @Override
    public void onDisable() {

    }
}
