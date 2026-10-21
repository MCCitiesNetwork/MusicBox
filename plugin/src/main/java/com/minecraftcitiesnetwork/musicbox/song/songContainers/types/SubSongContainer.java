package com.minecraftcitiesnetwork.musicbox.song.songContainers.types;

import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSong;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSongContainer;

import java.util.LinkedList;
import java.util.List;

public interface SubSongContainer extends SongContainer {
    List<MusicBoxSongContainer> getSubContainers();

    SubSongContainer getParentContainer();

    /**
     * Возращает всю музыку с учётом саб контейнеров и в них и в них
     * Рекурсивная крч фигня
     */
    @Override
    default List<MusicBoxSong> getAllSongs() {
        // Так как мы будем очень часто добавлять в него переменные
        List<MusicBoxSong> list = new LinkedList<>(getSongs());
        for (SongContainer container : getSubContainers()) {
            list.addAll(container.getAllSongs());
        }
        return list;
    }
}
