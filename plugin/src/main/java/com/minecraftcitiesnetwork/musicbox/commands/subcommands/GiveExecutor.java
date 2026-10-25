package com.minecraftcitiesnetwork.musicbox.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.minecraftcitiesnetwork.musicbox.gui.GUIActions;
import com.minecraftcitiesnetwork.musicbox.players.PlayerWrapper;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSong;

public class GiveExecutor extends AbstractSelect {
    public GiveExecutor() {
        super("musicbox.give");
    }
    @Override
    protected void noArgs(CommandSender sender, Player player) {
        GUIActions.openGiveInventoryMany(PlayerWrapper.getInstance(player));
    }
    @Override
    protected void processSong(CommandSender sender, Player target, MusicBoxSong song, String[] args) {
        GUIActions.giveDisc(PlayerWrapper.getInstance(target), song);
    }
}
