package com.minecraftcitiesnetwork.musicbox.gui.playlist;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.minecraftcitiesnetwork.musicbox.Lang;
import com.minecraftcitiesnetwork.musicbox.db.DatabaseLoader;
import com.minecraftcitiesnetwork.musicbox.db.model.PlayerPlayListModel;
import com.minecraftcitiesnetwork.musicbox.minecraft.gui.GUI;
import com.minecraftcitiesnetwork.musicbox.minecraft.gui.InventoryAction;
import com.minecraftcitiesnetwork.musicbox.minecraft.gui.actions.ClickAction;
import com.minecraftcitiesnetwork.musicbox.players.PlayerWrapper;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSong;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSongManager;
import com.minecraftcitiesnetwork.musicbox.utils.ItemUtils;
import com.minecraftcitiesnetwork.musicbox.utils.StringUtils;
import com.minecraftcitiesnetwork.musicbox.song.songContainers.types.SongContainer;

import java.util.List;
import java.util.function.Function;

/**
 * Класс для создания инвентаря со списком плейлистов
 */
public class PlayListListGUI {
    private final List<PlayerPlayListModel> list;
    private final PlayerWrapper wrapper;

    public PlayListListGUI(PlayerWrapper wrapper) {
        this.wrapper = wrapper;
        this.list = DatabaseLoader.getBase().getPlayLists(wrapper.getPlayer().getUniqueId());
    }

    /**
     * Собсна открывает игроку инвентарь
     *
     * @param onSelect - Что делать с выбранном плейлистом(если null то это master плейлист)
     */
    public void openPage(int page, Function<SongContainer, InventoryAction> onSelect, Function<PlayerPlayListModel, List<String>> extraLore) {
        int offset = page * 45;
        int lastPage = getLastPage();
        GUI gui = new GUI(Lang.PLAYLIST_LIST_TITLE.toString(
                "{page}", String.valueOf(page + 1),
                "{last_page}", String.valueOf(lastPage)
        ));
        gui.open(wrapper.getPlayer());
        for (int i = 0; i < 45; i++) {
            int listIndex = offset + i;
            if (listIndex >= list.size())
                break;
            PlayerPlayListModel element = list.get(listIndex);
            List<String> lore = Lang.PLAYLIST_LORE.toList(
                    "{count}", String.valueOf(element.getSongs().size()),
                    "{duration}", StringUtils.toHumanTime(element
                            .getSongs()
                            .stream()
                            .mapToInt(MusicBoxSong::getDuration)
                            .sum())
            );
            if (extraLore != null)
                lore.addAll(extraLore.apply(element));
            ItemStack stack = ItemUtils.createStack(
                    Material.PAPER,
                    Lang.PLAYLIST_NAME.toString("{name}", element.getName()),
                    lore);
            gui.addItem(i, stack, onSelect.apply(element));
        }
        // Создать новый плейлист
        gui.addItem(
                49,
                ItemUtils.createStack(Material.SUGAR, Lang.CREATE_NEW_PLAYLIST.toString(), null),
                new ClickAction(() -> {
                    Player p = wrapper.getPlayer();
                    p.closeInventory();
                    p.sendMessage(Lang.NEW_PLAYLIST_MESSAGE.toString());
                })
        );
        // Главный плейлист
        gui.addItem(
                46,
                ItemUtils.createStack(Material.DIAMOND, Lang.MASTER_PLAYLIST.toString(), Lang.MASTER_PLAYLIST_LORE.toList()),
                onSelect.apply(MusicBoxSongManager.getMasterContainer())
        );
        // Пагинация
        if (page > 0)
            gui.addItem(
                    45,
                    ItemUtils.createStack(Material.MAGMA_CREAM, Lang.BACK.toString(), null),
                    new ClickAction(() -> openPage(page - 1, onSelect, extraLore)));
        if ((lastPage - 1) > page)
            gui.addItem(
                    53,
                    ItemUtils.createStack(Material.MAGMA_CREAM, Lang.NEXT.toString(), null),
                    new ClickAction(() -> openPage(
                            page + 1, onSelect, extraLore)));
    }

    private int getLastPage() {
        return (int) Math.ceil(list.size() / 45D);
    }
}
