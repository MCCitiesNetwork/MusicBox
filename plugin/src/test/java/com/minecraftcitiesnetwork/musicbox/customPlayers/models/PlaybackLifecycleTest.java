package com.minecraftcitiesnetwork.musicbox.customPlayers.models;

import com.minecraftcitiesnetwork.musicbox.MusicBox;
import com.minecraftcitiesnetwork.musicbox.customPlayers.interfaces.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class PlaybackLifecycleTest {
    @Test void queuedEndAfterStopCannotAdvancePlaylistOrCreatePlayer() {
        var player=mock(MusicBoxSongPlayer.class); var list=mock(IPlayList.class);
        var next=mock(java.util.function.Function.class);
        var model=new MusicBoxSongPlayerModel(player,list,next);
        model.destroy();
        model.startNext(); model.createNextPlayer();
        verifyNoInteractions(list,next);
    }
    @Test void normalEndAdvancesOnceWhilePluginIsLoaded() {
        var player=mock(MusicBoxSongPlayer.class); var list=mock(IPlayList.class);
        when(list.tryNext()).thenReturn(true);
        java.util.function.Function<IPlayList, MusicBoxSongPlayer> next=mock(java.util.function.Function.class);
        var model=new MusicBoxSongPlayerModel(player,list,next);
        try(var statics=mockStatic(MusicBox.class)) {
            var plugin=mock(MusicBox.class); statics.when(MusicBox::getInstance).thenReturn(plugin);
            when(plugin.isLoaded()).thenReturn(true);
            model.startNext(); model.startNext();
            verify(list,times(1)).tryNext(); verify(next,times(1)).apply(list);
            verify(player,times(1)).destroy();
        }
    }
    @Test void destroyedPlayerCannotAdvanceAndUnloadedPluginCannotAdvance() {
        var player=mock(MusicBoxSongPlayer.class); var list=mock(IPlayList.class);
        var next=mock(java.util.function.Function.class);
        var model=new MusicBoxSongPlayerModel(player,list,next);
        when(player.isDestroyed()).thenReturn(true);
        model.startNext();
        verifyNoInteractions(list,next);
        when(player.isDestroyed()).thenReturn(false);
        try(var statics=mockStatic(MusicBox.class)) {
            var plugin=mock(MusicBox.class); statics.when(MusicBox::getInstance).thenReturn(plugin);
            model.startNext(); verifyNoInteractions(list,next);
        }
    }
}
