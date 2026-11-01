package com.minecraftcitiesnetwork.musicbox.customPlayers.objects;

import org.bukkit.Material;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;
import com.minecraftcitiesnetwork.musicbox.MusicBox;
import com.minecraftcitiesnetwork.musicbox.customPlayers.abstracts.AbstractBlockPlayer;
import com.minecraftcitiesnetwork.musicbox.customPlayers.interfaces.IPlayList;
import com.minecraftcitiesnetwork.musicbox.db.DatabaseLoader;

import com.minecraftcitiesnetwork.musicbox.minecraft.BlockAccess;
import com.minecraftcitiesnetwork.musicbox.utils.BukkitUtils;
import com.minecraftcitiesnetwork.musicbox.utils.FaceUtils;
import com.minecraftcitiesnetwork.musicbox.utils.SignUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class SignPlayer extends AbstractBlockPlayer {
    public static final String SIGN_SECOND_LINE = String.format("%s[%sMUSIC%s]", ChatColor.GRAY, ChatColor.GREEN, ChatColor.GRAY);
    private final Sign sign;
    private final boolean preventDestroy;
    private Location infoSign;

    /**
     * Создаёт новый проигрыватель для таблички
     *
     * @param list Плейлист который к ней привязан
     * @param sign Табличка к которой привязана эта музыка
     */
    private SignPlayer(IPlayList list, int range, Sign sign) {
        super(list, sign.getLocation(), range);
        this.sign = sign;
        preventDestroy = sign.getLine(3).contains("P");
        if (preventDestroy) {
            getRangePlayerModel().setAutoDestroyMillis(0);
            getLocation().getChunk().addPluginChunkTicket(MusicBox.getInstance());
        }
        setupInfoSign();
    }

    /**
     * Возвращает массив который содержат SongPlayer'ы защищенные от уничтожения
     */
    public static Set<SignPlayer> getPreventedPlayers() {
        return getAll()
                .stream()
                .filter(p -> p instanceof SignPlayer)
                .map(p -> (SignPlayer) p)
                .filter(SignPlayer::isPreventDestroy)
                .collect(Collectors.toSet());
    }

    /**
     * Пихать только таблички плугина
     *
     * @param sign       Табличка
     * @param pin        какой стороны был редстоун
     * @param newCurrent Мощность
     */
    public static void redstoneSign(Sign sign, int pin, int newCurrent) {
        SignPlayer player = AbstractBlockPlayer.findByLocation(sign.getLocation());
        // Связано с включением или выключением
        if (pin == 0) {
            if (newCurrent > 0) {
                createSign(sign);
            } else if (player != null) {
                player.destroy();
            }
        } else if (player != null && newCurrent > 0) {
            IPlayList list = player.getPlayList();
            switch (pin) {
                case 1:
                    list.back(1);
                    break;
                case 2:
                    list.next();
                    break;
                default:
                    return;
            }
            player.destroy();
            new SignPlayer(list, player.getRange(), sign);
        }
    }

    private static void createSign(Sign sign) {
        int range = SignUtils.parseSignRange(sign);
        SignUtils
                .parseSignPlaylist(sign)
                .ifPresent(l -> new SignPlayer(l, range, sign));
    }

    /**
     * Восстанавливает сохранённые проигрыватели
     */
    public static void restorePreventedPlayers() {
        List<Location> locations = DatabaseLoader.getBase().getPreventedSigns();
        for (Location location : locations) {
            BukkitUtils.runSyncTask(() -> {
                try {
                    BlockState state = location.getBlock().getState();
                    if (state instanceof Sign sign && isPlayerSign(sign)) {
                        SignUtils.parseSignPlaylist(sign).ifPresent(list -> {
                            new SignPlayer(list, SignUtils.parseSignRange(sign), sign);
                            if (AbstractBlockPlayer.findByLocation(location) != null)
                                DatabaseLoader.getBase().acknowledgePreventedSign(location);
                        });
                    }
                } catch (RuntimeException ex) {
                    MusicBox.getInstance().getLogger().warning("Unable to restore a saved music sign; retaining its database row: " + ex.getClass().getSimpleName());
                }
            });
        }
    }

    public static boolean isPlayerSign(Sign s) {
        return s.getLine(1).startsWith(SignPlayer.SIGN_SECOND_LINE);
    }

    @Override
    public void destroy() {
        if (isDestroyed()) return;
        org.bukkit.Chunk chunk = getLocation().getChunk();
        super.destroy();
        if (preventDestroy && getPreventedPlayers().stream().noneMatch(player ->
                BukkitUtils.inChunk(player.getLocation(), chunk.getWorld(), chunk.getX(), chunk.getZ())))
            chunk.removePluginChunkTicket(MusicBox.getInstance());
    }

    private void pingLever() {
        BukkitUtils.runSyncTask(() -> {
            @NotNull Block block = getTargetLocation().getBlock();
            BlockFace face = new BlockAccess().getRotation(block);
            face = FaceUtils.invertFace(face);
            @NotNull Block leverBlock = block.getRelative(face, 2);

            if (leverBlock.getType() == Material.LEVER) {
                BlockAccess utils = new BlockAccess();
                utils.setLever(leverBlock, true);
                Bukkit.getScheduler().runTaskLater(MusicBox.getInstance(), () ->
                        utils.setLever(leverBlock, false), 10);
            }
        });
    }

    private void setupInfoSign() {
        if (sign.getLine(3).contains("I"))
            SignUtils
                    .findSign(sign.getLocation())
                    .ifPresent(s -> {
                        infoSign = s.getLocation();
                        SignUtils.setPlayListInfo(infoSign, super.getPlayList());
                    });
    }

    @Override
    protected void every100MillisAsync() {
        BukkitUtils.runSyncTask(() -> {
            Block b = getTargetLocation().getBlock();
            if (!(b.getState() instanceof Sign) || !b.isBlockIndirectlyPowered())
                destroy();
        });
    }

    @Override
    protected SignPlayer runNextSong(IPlayList list) {
        return new SignPlayer(list, getRange(), sign);
    }

    @Override
    protected void songEnd() {
        pingLever();
    }
}
