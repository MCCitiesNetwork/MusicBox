package com.minecraftcitiesnetwork.musicbox.song;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyntheticSongTest {
    @TempDir Path directory;
    private void number(DataOutputStream out,int value,int bytes) throws IOException {
        for(int i=0;i<bytes;i++) out.writeByte(value >>> (8*i));
    }
    private void text(DataOutputStream out,String text) throws IOException {
        byte[] bytes=text.getBytes(StandardCharsets.UTF_8); number(out,bytes.length,4); out.write(bytes);
    }
    @Test void decodesOriginalSyntheticNbsAndRetainsPathBasedIdentifiers() throws Exception {
        var file=directory.resolve("synthetic.nbs");
        try(var out=new DataOutputStream(Files.newOutputStream(file))) {
            number(out,2,2); number(out,1,2);
            text(out,"Synthetic melody"); text(out,"Test author"); text(out,"Original author"); text(out,"One synthetic note");
            number(out,1000,2); out.write(new byte[]{0,10,4});
            for(int i=0;i<5;i++) number(out,0,4);
            text(out,""); number(out,1,2); number(out,1,2); out.write(new byte[]{0,45});
            number(out,0,2); number(out,0,2); text(out,"Test layer"); out.writeByte(100); out.writeByte(0);
        }
        try(var bukkit=mockStatic(Bukkit.class)) {
            var server=mock(Server.class); when(server.getVersion()).thenReturn("Synthetic Paper (MC: 26.3)");
            bukkit.when(Bukkit::getServer).thenReturn(server);
            bukkit.when(Bukkit::getVersion).thenReturn("Synthetic Paper (MC: 26.3)");
            com.minecraftcitiesnetwork.musicbox.Lang.reload(directory.resolve("lang").toFile(),"en");
            var relative=Path.of("").toAbsolutePath().relativize(directory).toFile();
            var container=new MusicBoxSongContainer(relative,null);
            assertEquals(relative.getPath().hashCode(),container.getHash());
            assertEquals(1,container.getSongs().size());
            var song=container.getSongs().getFirst();
            assertEquals("Synthetic melody",song.getName());
            assertEquals(2,song.getLength()); assertEquals(10f,song.getSpeed());
            assertEquals(new File(relative,"synthetic.nbs").getPath().hashCode(),song.getHash());
            assertNotEquals(file.toFile().getPath().hashCode(),song.getHash());
            var note=song.getSong().getLayerHashMap().get(0).getNote(0);
            assertNotNull(note); assertEquals(45,note.getKey()); assertEquals(0,note.getInstrument());
        }
    }
}
