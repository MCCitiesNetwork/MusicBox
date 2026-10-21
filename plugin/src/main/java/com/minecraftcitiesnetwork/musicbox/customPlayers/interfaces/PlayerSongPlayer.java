package com.minecraftcitiesnetwork.musicbox.customPlayers.interfaces;

import com.minecraftcitiesnetwork.musicbox.customPlayers.models.PlayerPlayerModel;

/**
 * Название идиотское, согласен
 * На русском - Проигрыватель звука игрока
 * Помечается на все проигрыватели, так или иначе связанные с игроком
 */
public interface PlayerSongPlayer extends MusicBoxSongPlayer {
    PlayerPlayerModel getModel();
}
