package com.minecraftcitiesnetwork.musicbox.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.minecraftcitiesnetwork.musicbox.commands.MusicBoxExecutor;
import com.minecraftcitiesnetwork.musicbox.gui.GUIActions;
import com.minecraftcitiesnetwork.musicbox.players.PlayerWrapper;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSong;

public class PlayExecutor extends AbstractSelect {
    public PlayExecutor() {
        super("musicbox.use");
    }

    @Override
    protected void noArgs(CommandSender sender, Player player) {
        GUIActions.openDefaultInventory(PlayerWrapper.getInstance(player));
    }

    @Override
    protected void processSong(CommandSender sender, Player target, MusicBoxSong song, String[] args) {
        PlayerWrapper.getInstance(target).play(song);
    }
}
