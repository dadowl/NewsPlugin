package dev.dadowl.newsplugin;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.*;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.io.File;

@Plugin(name = "News", version = "1.0.0")
@Author("dadowl")
@Website("https://dadowl.dev/")
@Description("A simple plugin for getting the latest news from a VK group")
@ApiVersion(ApiVersion.Target.v1_18)
@LogPrefix("News")

public final class NewsPlugin extends JavaPlugin {

    public static NewsPlugin instance = null;

    public VkManager vkManager = null;

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

        ConfigurationSection vk = getConfig().getConfigurationSection("vk");

        if (vk == null
            || vk.getInt("app_id") == 0
            || vk.getString("secret_key") == null
            || vk.getInt("group_id") == 0)
        {
            getLogger().severe("Данные для ВК не указаны в конфиге. Отключаем плагин.");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        vkManager = new VkManager(this, vk);
        vkManager.update();

        getCommand("news").setExecutor(new NewsCommand(this));
        Bukkit.getPluginManager().registerEvents(new Handler(this), this);


        //VkManager.update();
    }

    @Override
    public void onDisable() {

    }
}
