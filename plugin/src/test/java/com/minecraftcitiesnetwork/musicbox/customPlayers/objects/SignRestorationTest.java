package com.minecraftcitiesnetwork.musicbox.customPlayers.objects;

import com.minecraftcitiesnetwork.musicbox.MusicBox;
import com.minecraftcitiesnetwork.musicbox.db.*;
import com.minecraftcitiesnetwork.musicbox.db.model.PlayerPlayListModel;
import com.minecraftcitiesnetwork.musicbox.utils.SignUtils;
import org.bukkit.*;
import org.bukkit.block.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SignRestorationTest {
    @Test void emptyMasterAndMissingSongPlaylistAreNotConstructedAsPlayers() {
        var sign=mock(Sign.class);
        when(sign.getLine(1)).thenReturn(SignPlayer.SIGN_SECOND_LINE);
        when(sign.getLine(3)).thenReturn("P"); when(sign.getLine(0)).thenReturn("MASTER");
        assertTrue(SignUtils.parseSignPlaylist(sign).isEmpty());
        try(var loader=mockStatic(DatabaseLoader.class)) {
            var database=mock(AbstractBase.class); loader.when(DatabaseLoader::getBase).thenReturn(database);
            when(database.getPlayListById(7)).thenReturn(new PlayerPlayListModel(7,UUID.randomUUID(),"Synthetic"));
            when(sign.getLine(0)).thenReturn("LIST:7");
            assertTrue(SignUtils.parseSignPlaylist(sign).isEmpty());
        }
    }
    @Test void failedSignDoesNotPreventLaterRestorationAttemptsOrAcknowledgeRows() {
        try(var loader=mockStatic(DatabaseLoader.class); var bukkit=mockStatic(Bukkit.class); var statics=mockStatic(MusicBox.class)) {
            var database=mock(AbstractBase.class); loader.when(DatabaseLoader::getBase).thenReturn(database);
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            var plugin=mock(MusicBox.class); statics.when(MusicBox::getInstance).thenReturn(plugin);
            when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
            var bad=mock(Location.class); var later=mock(Location.class); var block=mock(Block.class);
            when(database.getPreventedSigns()).thenReturn(List.of(bad,later));
            when(bad.getBlock()).thenThrow(new IllegalStateException("Synthetic inaccessible block"));
            when(later.getBlock()).thenReturn(block); when(block.getState()).thenReturn(mock(BlockState.class));
            assertDoesNotThrow(SignPlayer::restorePreventedPlayers);
            verify(later).getBlock(); verify(database,never()).acknowledgePreventedSign(any());
        }
    }
}
