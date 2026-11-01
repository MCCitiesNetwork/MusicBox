package com.minecraftcitiesnetwork.musicbox.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import com.minecraftcitiesnetwork.musicbox.MusicBox;
import com.minecraftcitiesnetwork.musicbox.commands.SubCommand;

public class ReloadExecutor implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        Bukkit.getScheduler().runTask(MusicBox.getInstance(), () -> {
            MusicBox.getInstance().reloadPlugin();
            sender.sendMessage(ChatColor.GREEN + "Reloaded");
        });

    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return sender.hasPermission("musicbox.admin");
    }
}
