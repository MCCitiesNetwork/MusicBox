package com.minecraftcitiesnetwork.musicbox.db;

import com.minecraftcitiesnetwork.musicbox.db.types.SQLite;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSong;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSongManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataRetentionTest {
    @TempDir Path directory;
    private SQLite database;
    private String url;
    private MockedStatic<Bukkit> bukkit;
    private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000197");

    @BeforeEach void setup() throws Exception {
        bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::isPrimaryThread).thenReturn(false);
        var file = directory.resolve("synthetic.db").toFile();
        database = new SQLite(file);
        url = "jdbc:sqlite:" + file;
        songs(List.of());
        execute("INSERT INTO playlists(id,owner,name) VALUES(7,'" + OWNER + "','Synthetic playlist')");
    }
    @AfterEach void cleanup() throws Exception { songs(List.of()); bukkit.close(); }

    private void songs(List<MusicBoxSong> songs) throws Exception {
        var field = MusicBoxSongManager.class.getDeclaredField("allSongs");
        field.setAccessible(true);
        field.set(null, songs);
    }
    private void execute(String sql) throws Exception {
        try (var c = DriverManager.getConnection(url); var s = c.createStatement()) { s.executeUpdate(sql); }
    }
    private List<Integer> storedHashes() throws Exception {
        try (var c = DriverManager.getConnection(url); var s = c.createStatement();
             var r = s.executeQuery("SELECT song_hash FROM playlist_song WHERE playlists_id=7 ORDER BY pos")) {
            List<Integer> result = new ArrayList<>();
            while (r.next()) result.add(r.getInt(1));
            return result;
        }
    }
    private int count(String table) throws Exception {
        try (var c = DriverManager.getConnection(url); var s = c.createStatement();
             var r = s.executeQuery("SELECT count(*) FROM " + table)) { r.next(); return r.getInt(1); }
    }

    @Test void completelyMissingSongsRemainOnReadAndResave() throws Exception {
        execute("INSERT INTO playlist_song VALUES(7,-301,0),(7,902,1)");
        var model = database.getPlayListById(7);
        assertNotNull(model);
        assertTrue(model.getSongs().isEmpty());
        assertEquals(List.of(-301,902), storedHashes());
        assertEquals(1, database.getPlayLists(OWNER).size());
        model.setName("Renamed while files unavailable");
        database.savePlayList(model);
        assertEquals(List.of(-301,902), storedHashes());
        assertEquals(1, count("playlists"));
    }

    @Test void partiallyResolvedResaveRetainsUnknownHashesInOrder() throws Exception {
        var first = mock(MusicBoxSong.class); when(first.getHash()).thenReturn(111);
        var last = mock(MusicBoxSong.class); when(last.getHash()).thenReturn(333);
        songs(List.of(first,last));
        execute("INSERT INTO playlist_song VALUES(7,-301,0),(7,111,1),(7,902,2),(7,333,3)");
        var model = database.getPlayListById(7);
        assertEquals(List.of(first,last), model.getSongs());
        database.savePlayList(model);
        assertEquals(List.of(-301,111,902,333), storedHashes());
        model.getSongs().remove(first);
        database.savePlayList(model);
        assertEquals(List.of(-301,902,333), storedHashes());
        database.savePlayList(model);
        assertEquals(List.of(-301,902,333), storedHashes());
        var reloaded = database.getPlayListById(7);
        database.savePlayList(reloaded);
        assertEquals(List.of(-301,902,333), storedHashes());
    }

    @Test void sparsePersistedPositionsKeepMissingSongBeforeItsSurvivingNeighbour() throws Exception {
        var song = mock(MusicBoxSong.class); when(song.getHash()).thenReturn(222);
        songs(List.of(song));
        execute("INSERT INTO playlist_song VALUES(7,-301,10),(7,222,20),(7,902,40)");
        var model = database.getPlayListById(7);
        database.savePlayList(model);
        assertEquals(List.of(-301,222,902), storedHashes());
        database.savePlayList(database.getPlayListById(7));
        assertEquals(List.of(-301,222,902), storedHashes());
    }

    @Test void emptyPlaylistStillExists() {
        assertNotNull(database.getPlayListById(7));
        assertEquals(1,database.getPlayLists(OWNER).size());
    }

    @Test void signReadsRetainMissingWorldAndOnlyAcknowledgedRowIsRemoved() throws Exception {
        var world = mock(World.class); when(world.getName()).thenReturn("synthetic_world");
        bukkit.when(() -> Bukkit.getWorld("synthetic_world")).thenReturn(world);
        execute("INSERT INTO signs VALUES('synthetic_world|10.5|65.0|-3.5'),('absent_world|1.0|2.0|3.0')");
        var locations = database.getPreventedSigns();
        assertEquals(1,locations.size());
        assertEquals(2,count("signs"));
        database.savePreventedSigns(locations);
        database.savePreventedSigns(locations);
        assertEquals(2,count("signs"));
        database.acknowledgePreventedSign(locations.getFirst());
        assertEquals(1,count("signs"));
        assertTrue(database.getPreventedSigns().isEmpty());
        assertEquals(1,count("signs"));
    }
}
