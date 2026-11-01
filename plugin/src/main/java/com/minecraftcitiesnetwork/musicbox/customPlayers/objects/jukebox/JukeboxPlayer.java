package com.minecraftcitiesnetwork.musicbox.customPlayers.objects.jukebox;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.minecraftcitiesnetwork.musicbox.Lang;
import com.minecraftcitiesnetwork.musicbox.MusicBox;
import com.minecraftcitiesnetwork.musicbox.customPlayers.abstracts.AbstractBlockPlayer;
import com.minecraftcitiesnetwork.musicbox.customPlayers.interfaces.IPlayList;
import com.minecraftcitiesnetwork.musicbox.minecraft.jukebox.JukeboxFactory;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSong;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSongManager;
import com.minecraftcitiesnetwork.musicbox.utils.BukkitUtils;
import com.minecraftcitiesnetwork.musicbox.utils.SignUtils;

@Getter
public class JukeboxPlayer extends AbstractBlockPlayer {
    private Location infoSign;
    private final int playingSongHash;

    private JukeboxPlayer(IPlayList list, int range, Jukebox box) {
        super(list, box.getLocation(), range);
        playingSongHash = list.getCurrent().getHash();
        SignUtils
                .findSign(box.getLocation())
                .ifPresent(s -> {
                    infoSign = s.getLocation();
                    SignUtils.setPlayListInfo(infoSign, list);
                });
    }

    public static void onJukeboxClick(Jukebox jukebox, ItemStack clickedItem, PlayerInteractEvent e) {
        JukeboxPlayer sp = AbstractBlockPlayer.findByLocation(jukebox.getLocation());
        if (clickedItem == null || clickedItem.getType().isAir()) {
            if (sp != null) {
                e.setCancelled(true);
                sp.destroy();
                jukebox.eject();
            }
            return;
        }
        MusicBoxSong song = MusicBoxSongManager.findByItem(clickedItem).orElse(null);
        if (song == null) {
            if (sp != null) sp.destroy();
            return;
        }
        e.setCancelled(true);
        if (sp != null) sp.destroy();
        jukebox.eject();
        ItemStack inserted = clickedItem.clone();
        inserted.setAmount(1);
        JukeboxFactory.getJukebox(jukebox).setJukebox(inserted);
        ItemStack remaining = clickedItem.clone();
        remaining.setAmount(remaining.getAmount() - 1);
        e.getPlayer().getInventory().setItemInMainHand(remaining.getAmount() == 0 ? null : remaining);
        createNew(jukebox);
    }

    private static void createNew(Jukebox jukebox) {
        try {
            JukeboxPlaylistImpl playlist = new JukeboxPlaylistImpl(jukebox.getLocation());
            new JukeboxPlayer(playlist, MusicBox.getInstance().getConfigObject().getJukeboxRadius(), jukebox);
        } catch (JukeboxPlaylistInitException e) {
            // NOTHING
        }
    }

    /**
     * Вызывается когда игрок кликает с зажатым шифтом
     */
    public static void onSneakingClick(Jukebox jukebox, Player player) {
        JukeboxPlayer songPlayer = AbstractBlockPlayer.findByLocation(jukebox.getLocation());
        if (songPlayer != null) {
            songPlayer.getControl().open(player);
        }
    }

    /**
     * Вызывается когда редстоун тычет
     *
     * @param box    Проигрыватель
     * @param source Откуда пришёл сигнал
     * @param power  Сила сигнала
     */
    public static void onRedstone(Jukebox box, Block source, int power) {
        if (power > 0) {
            JukeboxPlayer player = AbstractBlockPlayer.findByLocation(box.getLocation());
            if (player != null) {
                player.getMusicBoxModel().startNext();
            } else if (JukeboxFactory.jukeboxAvailable())
                createNew(box);
        }
    }

    @Override
    protected void every100MillisAsync() {
        BukkitUtils.runSyncTask(() -> {
            Block b = getTargetLocation().getBlock();
            if (!(b.getState() instanceof Jukebox box) || !hasPlayingRecord(box, playingSongHash))
                destroy();
        });
    }

    static boolean hasPlayingRecord(Jukebox box, int playingHash) {
        return MusicBoxSongManager.findByItem(box.getInventory().getItem(0))
                .map(record -> record.getHash() == playingHash).orElse(false);
    }

    @Override
    protected JukeboxPlayer runNextSong(IPlayList list) {
        @NotNull BlockState state = getTargetLocation().getBlock().getState();
        if (state instanceof Jukebox) {
            return new JukeboxPlayer(list, getRange(), (Jukebox) state);
        } else
            return null;
    }

    @Override
    protected void songEnd() {
        // NOTHING
    }
}
