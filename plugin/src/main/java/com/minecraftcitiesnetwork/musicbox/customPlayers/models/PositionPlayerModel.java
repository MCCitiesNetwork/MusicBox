package com.minecraftcitiesnetwork.musicbox.customPlayers.models;

import lombok.Getter;
import com.minecraftcitiesnetwork.musicbox.customPlayers.interfaces.PositionPlayer;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSong;

@Getter
public class PositionPlayerModel {
    private final PositionPlayer positionPlayer;
    private final MusicBoxSong song;

    public PositionPlayerModel(PositionPlayer positionPlayer, MusicBoxSong song) {
        this.positionPlayer = positionPlayer;
        this.song = song;
    }
}
