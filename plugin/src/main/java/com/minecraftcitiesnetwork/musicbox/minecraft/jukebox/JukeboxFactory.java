package com.minecraftcitiesnetwork.musicbox.minecraft.jukebox;

import org.bukkit.block.Jukebox;
import org.bukkit.inventory.ItemStack;

public final class JukeboxFactory {
    private JukeboxFactory() {}
    public static boolean jukeboxAvailable() { return true; }
    public static IJukebox getJukebox(Jukebox jukebox) {
        return new IJukebox() {
            public void setJukebox(ItemStack item) {
                jukebox.getInventory().setItem(0, item);
                jukebox.stopPlaying();
            }
            public ItemStack getJukebox() { return jukebox.getInventory().getItem(0); }
        };
    }
}
