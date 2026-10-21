package com.minecraftcitiesnetwork.musicbox.song.songContainers.containers;

import lombok.RequiredArgsConstructor;
import com.minecraftcitiesnetwork.musicbox.song.songContainers.types.SongContainer;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSong;
import com.minecraftcitiesnetwork.musicbox.song.songContainers.factory.SingletonContainerFactory;

import java.util.Collections;
import java.util.List;

/**
 * По названию можно и догадаться, содержит в себе 1 единственную музыку
 */
@RequiredArgsConstructor
public class SingletonContainer implements SongContainer {

    private final MusicBoxSong song;

    @Override
    public String getNameId() {
        return SingletonContainerFactory.NAME+":" + song.getHash();
    }

    @Override
    public List<MusicBoxSong> getSongs() {
        return Collections.singletonList(song);
    }

    @Override
    public List<MusicBoxSong> getSongsShuffle() {
        return getSongs();
    }
}
