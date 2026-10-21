package com.minecraftcitiesnetwork.musicbox.customPlayers.playlist;

import lombok.RequiredArgsConstructor;
import com.minecraftcitiesnetwork.musicbox.customPlayers.interfaces.IPlayList;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSong;

import java.util.Collections;
import java.util.List;

/**
 * Небольшая имплементация плейлиста
 * Содерижит только одну музыку и то, отдаст её только один раз
 */
@RequiredArgsConstructor
public class SingletonPlayList implements IPlayList {
    private final MusicBoxSong song;

    @Override
    public void next() {
        // Тоже нет
    }

    @Override
    public List<MusicBoxSong> getNextSongs(int a) {
        return Collections.emptyList();

    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrev() {
        return false;
    }

    @Override
    public boolean isSingleList() {
        return true;
    }

    @Override
    public List<MusicBoxSong> getPrevSongs(int count) {
        return Collections.emptyList();
    }

    @Override
    public MusicBoxSong getCurrent() {
        return song;
    }

    @Override
    public void back(int count) {
        // NO
    }

    @Override
    public int getSongNum(MusicBoxSong song) {
        return 0;
    }

    @Override
    public void setSong(MusicBoxSong song) {
        // НЕА
    }
}
