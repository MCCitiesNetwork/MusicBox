package com.minecraftcitiesnetwork.musicbox.song.songContainers;

import com.minecraftcitiesnetwork.musicbox.song.songContainers.types.SongContainer;

public interface SongContainerFactory<T extends SongContainer> {
    String getKey();

    T parseContainer(int id);
}
