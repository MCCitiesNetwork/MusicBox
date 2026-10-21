package com.minecraftcitiesnetwork.musicbox.song.songContainers.factory;

import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSongManager;
import com.minecraftcitiesnetwork.musicbox.song.songContainers.SongContainerFactory;
import com.minecraftcitiesnetwork.musicbox.song.songContainers.containers.SingletonContainer;

public class SingletonContainerFactory implements SongContainerFactory<SingletonContainer> {
    public static String NAME = "ID";

    @Override
    public String getKey() {
        return NAME;
    }

    @Override
    public SingletonContainer parseContainer(int id) {
        return MusicBoxSongManager
                .findSongByHash(id)
                .map(SingletonContainer::new)
                .orElse(null);
    }
}
