package com.minecraftcitiesnetwork.musicbox.song.songContainers.factory;

import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSongContainer;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSongManager;
import com.minecraftcitiesnetwork.musicbox.song.songContainers.SongContainerFactory;

public class FolderContainerFactory implements SongContainerFactory<MusicBoxSongContainer> {
    public static final String NAME = "CHEST";

    @Override
    public String getKey() {
        return NAME;
    }

    @Override
    public MusicBoxSongContainer parseContainer(int id) {
        return MusicBoxSongManager
                .findContainerById(id)
                .orElse(null);
    }
}
