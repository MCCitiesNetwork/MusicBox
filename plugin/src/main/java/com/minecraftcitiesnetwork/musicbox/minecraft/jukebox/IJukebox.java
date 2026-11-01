package com.minecraftcitiesnetwork.musicbox.minecraft.jukebox;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;



public interface IJukebox {
    default boolean isEmpty() {
        ItemStack item = getJukebox();
        return item == null || item.getType().equals(Material.AIR);
    }

    void setJukebox(ItemStack item);

    ItemStack getJukebox();
}
