package com.minecraftcitiesnetwork.musicbox;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class InteractionGuardTest {
    @Test void protectedClickDoesNotReadOrMutateBlock() {
        var event=mock(PlayerInteractEvent.class);
        when(event.useInteractedBlock()).thenReturn(Event.Result.DENY);
        new Handler().onInteract(event);
        verify(event,never()).getClickedBlock();
    }
    @Test void offhandDoesNotInsertOrEjectDisc() {
        try(var statics=mockStatic(MusicBox.class)) {
            var plugin=mock(MusicBox.class); statics.when(MusicBox::getInstance).thenReturn(plugin);
            when(plugin.isLoaded()).thenReturn(true);
            var event=mock(PlayerInteractEvent.class); var block=mock(Block.class);
            when(event.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
            when(event.getClickedBlock()).thenReturn(block); when(event.getHand()).thenReturn(EquipmentSlot.OFF_HAND);
            new Handler().onInteract(event);
            verifyNoInteractions(block);
        }
    }
}
