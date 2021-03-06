package dev.dadowl.newsplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.jetbrains.annotations.NotNull;

@Commands(
    @org.bukkit.plugin.java.annotation.command.Command(name = "news", usage = "/<command>", desc = "Show last post from VK group")
)

public class NewsCommand implements CommandExecutor {

    private NewsPlugin plugin;

    public NewsCommand(NewsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        player.openBook(plugin.vkManager.newsBook);

        return true;
    }
}
