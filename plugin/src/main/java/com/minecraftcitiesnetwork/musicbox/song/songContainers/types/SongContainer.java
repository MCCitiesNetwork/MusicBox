package com.minecraftcitiesnetwork.musicbox.song.songContainers.types;

import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSong;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface SongContainer {
    String getNameId();

    List<MusicBoxSong> getSongs();

    default List<MusicBoxSong> getSongsShuffle() {
        List<MusicBoxSong> list = new ArrayList<>(getSongs());
        Collections.shuffle(list);
        return list;
    }

    default List<MusicBoxSong> getSongsRand(boolean rand) {
        return rand ? getSongsShuffle() : getSongs();
    }

    default List<MusicBoxSong> getAllSongs() {
        return getSongs();
    }
}
