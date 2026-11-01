package com.minecraftcitiesnetwork.musicbox.db.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.minecraftcitiesnetwork.musicbox.db.DatabaseLoader;
import com.minecraftcitiesnetwork.musicbox.players.PlayerWrapper;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSong;
import com.minecraftcitiesnetwork.musicbox.song.songContainers.types.SongContainer;
import com.minecraftcitiesnetwork.musicbox.song.songContainers.factory.ListContainerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PlayerPlayListModel implements SongContainer {
    private int id;
    private final UUID owner;
    private String name;
    private final List<MusicBoxSong> songs = new LinkedList<>();

    // Original positions of unavailable songs survive reads and subsequent edits.
    private final java.util.Map<Integer, Integer> unresolvedSongs = new java.util.TreeMap<>();

    private final List<Integer> originalSongHashes = new java.util.ArrayList<>();

    public List<Integer> songHashesForSave() {
        List<Integer> hashes = new java.util.ArrayList<>();
        songs.forEach(song -> hashes.add(song.getHash()));
        java.util.Set<Integer> resolved = new java.util.HashSet<>(hashes);
        int previousUnknownIndex = -1;
        for (var entry : unresolvedSongs.entrySet()) {
            int hash = entry.getValue();
            if (resolved.contains(hash)) continue;
            int insertion = hashes.size();
            // Keep missing references before their next surviving original neighbour.
            for (int i = entry.getKey() + 1; i < originalSongHashes.size(); i++) {
                int candidate = originalSongHashes.get(i);
                if (resolved.contains(candidate)) {
                    insertion = hashes.indexOf(candidate);
                    break;
                }
            }
            insertion = Math.max(insertion, previousUnknownIndex + 1);
            hashes.add(insertion, hash);
            previousUnknownIndex = insertion;
        }
        return hashes;
    }

    public void rememberPersistedHashes(List<Integer> hashes) {
        java.util.Set<Integer> missing = new java.util.HashSet<>(unresolvedSongs.values());
        songs.forEach(song -> missing.remove(song.getHash()));
        originalSongHashes.clear();
        originalSongHashes.addAll(hashes);
        unresolvedSongs.clear();
        for (int i = 0; i < hashes.size(); i++) {
            if (missing.contains(hashes.get(i))) unresolvedSongs.put(i, hashes.get(i));
        }
    }

    /**
     * Возращает владельца если тот в сети
     */
    public Optional<PlayerWrapper> getOwnerWrapper() {
        Player player = Bukkit.getPlayer(owner);
        if (player == null)
            return Optional.empty();
        else
            return Optional.of(PlayerWrapper.getInstance(player));
    }

    public void save() {
        DatabaseLoader.getBase().savePlayList(this);
    }

    public void delete() {
        DatabaseLoader.getBase().deleteMe(this);
    }

    @Override
    public String getNameId() {
        return ListContainerFactory.NAME + ":" + id;
    }

}
