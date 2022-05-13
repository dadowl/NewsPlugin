package dev.dadowl.newsplugin;

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
        if (!NewsPlugin.ready) return;
        //open book with last post
    }
}
