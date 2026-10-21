package com.minecraftcitiesnetwork.musicbox.customPlayers.objects;

import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;
import com.minecraftcitiesnetwork.musicbox.customPlayers.interfaces.IPlayList;
import com.minecraftcitiesnetwork.musicbox.customPlayers.interfaces.PlayerSongPlayer;
import com.minecraftcitiesnetwork.musicbox.customPlayers.models.MusicBoxSongPlayerModel;
import com.minecraftcitiesnetwork.musicbox.customPlayers.models.PlayerPlayerModel;
import com.minecraftcitiesnetwork.musicbox.players.PlayerWrapper;
import com.minecraftcitiesnetwork.musicbox.utils.SongUtils;

/**
 * Проигрыватель для игрока
 * Если игрок захочет послушать музыку никого не напрягая
 */
@Getter
public class RadioPlayer extends RadioSongPlayer implements PlayerSongPlayer {
    private final PlayerPlayerModel model;
    private final MusicBoxSongPlayerModel musicBoxModel;


    public RadioPlayer(IPlayList list, PlayerWrapper wrapper) {
        super(list.getCurrent().getSong());
        this.musicBoxModel = new MusicBoxSongPlayerModel(this, list, SongUtils.nextPlayerSong(wrapper));
        this.model = new PlayerPlayerModel(wrapper, musicBoxModel);
        musicBoxModel.runPlayer();

    }


    @Override
    public void destroy() {
        if (!isDestroyed()) {
            try {
                super.destroy();
            } catch (IllegalPluginAccessException ex) {
                // ПОФИК
            }
            model.destroy();
            musicBoxModel.destroy();
        }
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void playTick(Player player, int tick) {
        super.playTick(player, tick);
        if (player.equals(model.getWrapper().getPlayer())) {
            model.nextTick(getSong().getLength(), tick);
        }
    }
}
