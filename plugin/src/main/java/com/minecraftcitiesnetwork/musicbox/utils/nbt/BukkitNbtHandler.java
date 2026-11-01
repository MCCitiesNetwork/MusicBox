package com.minecraftcitiesnetwork.musicbox.utils.nbt;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.nbt.BinaryTagIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BukkitNbtHandler implements NBTHandler {
    @Override
    public ItemStack setNbt(ItemStack item, String key, Integer value) {
        ItemStack newItem = item.clone();

        ItemMeta meta = newItem.getItemMeta();
        meta.getPersistentDataContainer().set(NamespacedKey.fromString("musicbox:" + key), PersistentDataType.INTEGER, value);
        newItem.setItemMeta(meta);

        return newItem;
    }

    @Override
    public int getNbt(ItemStack item, String key) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        Integer value = meta.getPersistentDataContainer().get(NamespacedKey.fromString("musicbox:" + key), PersistentDataType.INTEGER);
        if (value != null) return value;
        // Read historical raw tags after Minecraft has migrated them to custom_data.
        // Never rewrite the original item; new discs continue using musicbox:song_hash.
        try {
            var root = BinaryTagIO.reader().read(new ByteArrayInputStream(item.serializeAsBytes()), BinaryTagIO.Compression.GZIP);
            int hash = root.getCompound("components").getCompound("minecraft:custom_data").getInt(key);
            return hash != 0 ? hash : root.getCompound("tag").getInt(key);
        } catch (IOException | IllegalArgumentException ex) {
            return 0;
        }
    }
}
