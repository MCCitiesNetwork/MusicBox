package com.minecraftcitiesnetwork.musicbox;

import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.minecraftcitiesnetwork.musicbox.commands.MusicBoxExecutor;
import com.minecraftcitiesnetwork.musicbox.customPlayers.abstracts.AbstractBlockPlayer;
import com.minecraftcitiesnetwork.musicbox.customPlayers.models.MusicBoxSongPlayerModel;
import com.minecraftcitiesnetwork.musicbox.customPlayers.objects.SignPlayer;
import com.minecraftcitiesnetwork.musicbox.db.DatabaseLoader;
import com.minecraftcitiesnetwork.musicbox.gui.GUIActions;
import com.minecraftcitiesnetwork.musicbox.players.PlayerWrapper;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSongManager;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public final class MusicBox extends JavaPlugin {
    @Getter
    private static MusicBox instance;
    private MusicBoxConfig configObject;
    private boolean loaded = false;
    private Metrics bStats;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultValues();
        new MusicBoxExecutor().register(this);
        Bukkit.getPluginManager().registerEvents(new Handler(), this);
        reloadPlugin();
    }

    public void sendMessage(String pex, String noPexMessage, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(pex))
                player.sendMessage(message);
            else
                player.sendMessage(noPexMessage);
        }
    }

    private void saveDefaultValues() {
        saveDefaultConfig();
        new File(getDataFolder(), "songs").mkdirs();
    }

    public void reloadPlugin() {
        com.minecraftcitiesnetwork.musicbox.utils.BukkitUtils.checkPrimary();
        loaded = false;
        savePreventedPlayers();
        destroyAllPlayers();
        try (FileInputStream input = new FileInputStream(new File(getDataFolder(), "config.yml"))) {
            configObject = MusicBoxConfig.parseConfig(input);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        DatabaseLoader.reload();
        Lang.reload(new File(getDataFolder(), "lang"), configObject.getLang());
        PlayerWrapper.clearAll();
        MusicBoxSongManager.reload(new File(getDataFolder(), "songs"));
        GUIActions.reloadGUI();


        if (!configObject.isBStats() && bStats != null) {
            bStats.shutdown();
            bStats = null;
        }
        if (configObject.isBStats() && bStats == null) {
            bStats = new Metrics(this, 8766);
            bStats.addCustomChart(
                    new SingleLineChart(
                            "song_count",
                            () -> MusicBoxSongManager.getAllSongs().size())
            );
        }
        loaded = true;
        SignPlayer.restorePreventedPlayers();
    }

    public void destroyAllPlayers() {
        MusicBoxSongPlayerModel.destroyAll();
        PlayerWrapper.clearAll();
    }

    @Override
    public void onDisable() {
        loaded = false;
        try {
            savePreventedPlayers();
        } finally {
            destroyAllPlayers();
            if (bStats != null) bStats.shutdown();
        }
    }

    private void savePreventedPlayers() {
        if (!DatabaseLoader.isInitialized()) return;
        List<Location> signLocations = SignPlayer.getPreventedPlayers().stream()
                .map(AbstractBlockPlayer::getLocation).collect(Collectors.toList());
        DatabaseLoader.getBase().savePreventedSigns(signLocations);
    }
}
