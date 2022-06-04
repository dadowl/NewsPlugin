package dev.dadowl.newsplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.jetbrains.annotations.NotNull;

@Commands(
    @org.bukkit.plugin.java.annotation.command.Command(name = "news", usage = "/<command>")
)

public class NewsCommand implements CommandExecutor {

    private NewsPlugin plugin;

    public NewsCommand(NewsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {


        return false;
    }
}
