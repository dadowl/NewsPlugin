package dev.dadowl.newsplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Handler implements Listener {

    private NewsPlugin plugin = null;

    public Handler(NewsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){

        Player player = e.getPlayer();

        if (!plugin.vkManager.isPlayerSeenLastPost(player.getUniqueId())){
            player.openBook(plugin.vkManager.newsBook);
            plugin.vkManager.setPlayerSeenLastPost(player.getUniqueId());
        }

    }
}
