package com.minecraftcitiesnetwork.musicbox.song.songContainers.factory;

import com.minecraftcitiesnetwork.musicbox.db.DatabaseLoader;
import com.minecraftcitiesnetwork.musicbox.db.model.PlayerPlayListModel;
import com.minecraftcitiesnetwork.musicbox.song.songContainers.SongContainerFactory;

public class ListContainerFactory implements SongContainerFactory<PlayerPlayListModel> {
    public static String NAME = "LIST";

    @Override
    public String getKey() {
        return NAME;
    }

    @Override
    public PlayerPlayListModel parseContainer(int id) {
        return DatabaseLoader.getBase().getPlayListById(id);
    }
}
