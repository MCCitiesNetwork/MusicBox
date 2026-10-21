package com.minecraftcitiesnetwork.musicbox.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.minecraftcitiesnetwork.musicbox.Lang;
import com.minecraftcitiesnetwork.musicbox.commands.SubCommand;
import com.minecraftcitiesnetwork.musicbox.db.model.PlayerPlayListModel;
import com.minecraftcitiesnetwork.musicbox.gui.GUIActions;
import com.minecraftcitiesnetwork.musicbox.players.PlayerWrapper;
import com.minecraftcitiesnetwork.musicbox.utils.StringUtils;

public class PlaylistExecutor implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.ONLY_PLAYERS.toString());
            return;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            GUIActions.openPlaylistListEditor(PlayerWrapper.getInstance(player));
            return;
        }
        createPlaylist(player, args);
    }

    private void createPlaylist(Player player, String[] args) {
        if (args.length <= 0) {
            player.sendMessage(Lang.INPUT_NAME.toString());
            return;
        }
        String name = StringUtils.t(String.join(" ", args));
        GUIActions.openPlaylistEditor(
                PlayerWrapper.getInstance(player),
                new PlayerPlayListModel(-1, player.getUniqueId(), name)
        );
    }
}
