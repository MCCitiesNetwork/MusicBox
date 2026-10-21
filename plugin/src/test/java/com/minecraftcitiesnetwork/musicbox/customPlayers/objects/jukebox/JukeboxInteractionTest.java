package com.minecraftcitiesnetwork.musicbox.customPlayers.objects.jukebox;

import com.minecraftcitiesnetwork.musicbox.*;
import com.minecraftcitiesnetwork.musicbox.customPlayers.abstracts.AbstractBlockPlayer;
import com.minecraftcitiesnetwork.musicbox.song.*;
import org.bukkit.Material;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JukeboxInteractionTest {
    @Test void insertionConsumesOneAndLeavesRemainingStackInHand() {
        try(var songs=mockStatic(MusicBoxSongManager.class); var statics=mockStatic(MusicBox.class); var blocks=mockStatic(AbstractBlockPlayer.class);
            var lists=mockConstruction(JukeboxPlaylistImpl.class); var players=mockConstruction(JukeboxPlayer.class)) {
            var song=mock(MusicBoxSong.class); var held=mock(ItemStack.class);
            when(held.getType()).thenReturn(mock(Material.class));
            songs.when(() -> MusicBoxSongManager.findByItem(held)).thenReturn(Optional.of(song));
            var inserted=mock(ItemStack.class); var remaining=mock(ItemStack.class);
            when(held.clone()).thenReturn(inserted,remaining); when(remaining.getAmount()).thenReturn(3,2);
            var box=mock(Jukebox.class); var inventory=mock(JukeboxInventory.class); when(box.getInventory()).thenReturn(inventory);
            var player=mock(Player.class); var playerInventory=mock(PlayerInventory.class); when(player.getInventory()).thenReturn(playerInventory);
            var event=mock(PlayerInteractEvent.class); when(event.getPlayer()).thenReturn(player);
            var plugin=mock(MusicBox.class); var config=mock(MusicBoxConfig.class);
            statics.when(MusicBox::getInstance).thenReturn(plugin); when(plugin.getConfigObject()).thenReturn(config);
            JukeboxPlayer.onJukeboxClick(box,held,event);
            verify(event).setCancelled(true); verify(box).eject();
            verify(inserted).setAmount(1); verify(remaining).setAmount(2);
            verify(inventory).setItem(0,inserted); verify(box).stopPlaying();
            verify(playerInventory).setItemInMainHand(remaining); verify(held,never()).setAmount(anyInt());
            assertEquals(1,players.constructed().size());
        }
    }
    @Test void ordinaryItemStopsCustomPlaybackAndLeavesVanillaInteractionUntouched() {
        try(var songs=mockStatic(MusicBoxSongManager.class); var blocks=mockStatic(AbstractBlockPlayer.class)) {
            var box=mock(Jukebox.class); var active=mock(JukeboxPlayer.class);
            blocks.when(() -> AbstractBlockPlayer.findByLocation(box.getLocation())).thenReturn(active);
            var held=mock(ItemStack.class); when(held.getType()).thenReturn(mock(Material.class));
            songs.when(() -> MusicBoxSongManager.findByItem(held)).thenReturn(Optional.empty());
            var event=mock(PlayerInteractEvent.class);
            JukeboxPlayer.onJukeboxClick(box,held,event);
            verify(active).destroy(); verify(event,never()).setCancelled(anyBoolean());
            verify(box,never()).eject(); verify(held,never()).setAmount(anyInt());
        }
    }
    @Test void liveRecordRemovalOrReplacementDoesNotMatchOriginalPlayingSong() {
        try(var songs=mockStatic(MusicBoxSongManager.class)) {
            var box=mock(Jukebox.class); var inventory=mock(JukeboxInventory.class); when(box.getInventory()).thenReturn(inventory);
            var stack=mock(ItemStack.class); when(inventory.getItem(0)).thenReturn(stack);
            var original=mock(MusicBoxSong.class); when(original.getHash()).thenReturn(111);
            songs.when(() -> MusicBoxSongManager.findByItem(stack)).thenReturn(Optional.of(original));
            assertTrue(JukeboxPlayer.hasPlayingRecord(box,111));
            var replacement=mock(MusicBoxSong.class); when(replacement.getHash()).thenReturn(222);
            songs.when(() -> MusicBoxSongManager.findByItem(stack)).thenReturn(Optional.of(replacement));
            assertFalse(JukeboxPlayer.hasPlayingRecord(box,111));
            when(inventory.getItem(0)).thenReturn(null);
            songs.when(() -> MusicBoxSongManager.findByItem(null)).thenReturn(Optional.empty());
            assertFalse(JukeboxPlayer.hasPlayingRecord(box,111));
        }
    }
}
