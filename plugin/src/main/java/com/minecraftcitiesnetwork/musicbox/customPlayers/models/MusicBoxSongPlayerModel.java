package com.minecraftcitiesnetwork.musicbox.customPlayers.models;

import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import lombok.Getter;
import com.minecraftcitiesnetwork.musicbox.MusicBox;
import com.minecraftcitiesnetwork.musicbox.customPlayers.interfaces.IPlayList;
import com.minecraftcitiesnetwork.musicbox.customPlayers.interfaces.MusicBoxSongPlayer;
import com.minecraftcitiesnetwork.musicbox.gui.song.SPControlGUI;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSong;

import java.util.*;
import java.util.function.Function;

@Getter
public class MusicBoxSongPlayerModel {
    private final static Set<MusicBoxSongPlayerModel> all = Collections.newSetFromMap(new WeakHashMap<>());
    private final MusicBoxSongPlayer musicBoxSongPlayer;
    private final IPlayList playList;
    private final Function<IPlayList, ? extends MusicBoxSongPlayer> nextSongRunnable;
    private boolean run = false;
    private boolean stopped = false;

    /**
     * @param songPlayer       плеер который связан с этой моделью
     * @param playList         плейлист который сейчас играет
     * @param nextSongRunnable как ставить следующую музыку из плейлиста
     */
    public MusicBoxSongPlayerModel(MusicBoxSongPlayer songPlayer, IPlayList playList, Function<IPlayList, ? extends MusicBoxSongPlayer> nextSongRunnable) {
        all.add(this);
        this.musicBoxSongPlayer = songPlayer;
        this.playList = playList;
        this.nextSongRunnable = nextSongRunnable;
    }

    public static void destroyAll() {
        new ArrayList<>(all).forEach(a -> a.getMusicBoxSongPlayer().destroy());
        all.clear();
    }

    public MusicBoxSong getCurrentSong() {
        return playList.getCurrent();
    }

    public void runPlayer() {
        if (!run) {
            SongPlayer songPlayer = this.musicBoxSongPlayer.getApiPlayer();
            if (MusicBox.getInstance().getConfigObject().isEnable10octave()) {
                songPlayer.setEnable10Octave(true);
            }
            songPlayer.setPlaying(true);
            run = true;
        }
    }

    /**
     * Вызывается при вызове {@link SongPlayer#destroy()}
     */
    public void destroy() {
        stopped = true;
        if (controlGUI != null)
            controlGUI.close();
    }

    private SPControlGUI controlGUI;

    /**
     * Создаёт GUI для настройки этого SongPlayer'а
     */
    public SPControlGUI getControlGUI() {
        if (controlGUI == null)
            controlGUI = new SPControlGUI(this);
        return controlGUI;
    }
    // Немного чернухи

    public void setPlayers(Collection<UUID> players) {
        SongPlayer api = musicBoxSongPlayer.getApiPlayer();
        Set<UUID> current = new HashSet<>(api.getPlayerUUIDs());
        current.stream().filter(id -> !players.contains(id)).forEach(api::removePlayer);
        players.stream().filter(id -> !current.contains(id)).forEach(api::addPlayer);
    }

    /**
     * Применяет следующий плейлист
     */
    private void acceptNext() {
        MusicBoxSongPlayer nextPlayer = nextSongRunnable.apply(playList);
        if (nextPlayer != null && controlGUI != null)
            controlGUI.openNext(nextPlayer.getMusicBoxModel());
    }

    /**
     * Вызывается из event'a
     */
    public void onSongEnd() {
        com.minecraftcitiesnetwork.musicbox.utils.BukkitUtils.runSyncTask(this::startNext);
    }

    private boolean nextCreated = false;

    public void createNextPlayer() {
        if (stopped || nextCreated || musicBoxSongPlayer.isDestroyed() || !MusicBox.getInstance().isLoaded()) return;
        nextCreated = true;
        getMusicBoxSongPlayer().destroy();
        acceptNext();
    }

    /**
     * Завершилась ли музыка сама по себе, или её destroy()
     */
    private boolean songEndNormal = false;

    /**
     * Возможно костыль, но сначало нужно destroy SongPlayer а потом его модель
     */
    public void pingSongEnded() {
        songEndNormal = true;
    }

    public void startNext() {
        if (stopped || nextCreated || musicBoxSongPlayer.isDestroyed() || !MusicBox.getInstance().isLoaded()) return;
        if (playList.tryNext()) {
            createNextPlayer();
        } else
            getMusicBoxSongPlayer().destroy();
    }
}
