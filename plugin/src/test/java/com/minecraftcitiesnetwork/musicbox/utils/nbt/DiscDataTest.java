package com.minecraftcitiesnetwork.musicbox.utils.nbt;

import net.kyori.adventure.nbt.*;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.*;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscDataTest {
    private final BukkitNbtHandler handler = new BukkitNbtHandler();
    private ItemStack item(CompoundBinaryTag root) throws Exception {
        var bytes = new ByteArrayOutputStream();
        BinaryTagIO.writer().write(root, bytes, BinaryTagIO.Compression.GZIP);
        var item = mock(ItemStack.class); var meta = mock(ItemMeta.class);
        when(item.hasItemMeta()).thenReturn(true); when(item.getItemMeta()).thenReturn(meta);
        when(meta.getPersistentDataContainer()).thenReturn(mock(PersistentDataContainer.class));
        when(item.serializeAsBytes()).thenReturn(bytes.toByteArray());
        return item;
    }
    @Test void pdcKeepsRollbackNamespaceAndTakesPrecedence() throws Exception {
        var item = item(CompoundBinaryTag.empty());
        when(item.getItemMeta().getPersistentDataContainer().get(NamespacedKey.fromString("musicbox:song_hash"), PersistentDataType.INTEGER)).thenReturn(-197);
        assertEquals(-197, handler.getNbt(item,"song_hash"));
        verify(item,never()).serializeAsBytes();
        var clone = mock(ItemStack.class); when(item.clone()).thenReturn(clone);
        var meta = mock(ItemMeta.class); when(clone.getItemMeta()).thenReturn(meta);
        var pdc = mock(PersistentDataContainer.class); when(meta.getPersistentDataContainer()).thenReturn(pdc);
        assertSame(clone,handler.setNbt(item,"song_hash",12345));
        verify(pdc).set(NamespacedKey.fromString("musicbox:song_hash"),PersistentDataType.INTEGER,12345);
        verify(item,never()).setItemMeta(any());
    }
    @Test void rawCustomDataAndOldTagAreReadWithoutRewriting() throws Exception {
        var tag = CompoundBinaryTag.builder().putInt("song_hash",-123456).build();
        var modern = item(CompoundBinaryTag.builder().put("components", CompoundBinaryTag.builder().put("minecraft:custom_data",tag).build()).build());
        var legacy = item(CompoundBinaryTag.builder().put("tag",tag).build());
        assertEquals(-123456,handler.getNbt(modern,"song_hash"));
        assertEquals(-123456,handler.getNbt(legacy,"song_hash"));
        verify(modern,never()).setItemMeta(any()); verify(legacy,never()).setItemMeta(any());
    }
    @Test void absentOrMalformedDataDoesNotIdentifySong() throws Exception {
        assertEquals(0,handler.getNbt(null,"song_hash"));
        var item = item(CompoundBinaryTag.empty());
        assertEquals(0,handler.getNbt(item,"song_hash"));
        when(item.serializeAsBytes()).thenReturn(new byte[]{1,2,3});
        assertEquals(0,handler.getNbt(item,"song_hash"));
    }
}
