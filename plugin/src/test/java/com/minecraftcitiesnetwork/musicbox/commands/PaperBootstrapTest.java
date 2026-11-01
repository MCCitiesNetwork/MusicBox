package com.minecraftcitiesnetwork.musicbox.commands;

import com.minecraftcitiesnetwork.musicbox.MusicBox;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.NamespacedKey;
import org.mockito.MockedStatic;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;
import org.incendo.cloud.bukkit.internal.MinecraftArgumentTypes;
import org.incendo.cloud.bukkit.parser.ItemStackParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Exercises the actual eager Cloud mappings, rather than a standalone command-manager substitute. */
@Tag("paper-runtime")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaperBootstrapTest {
    private static MockedStatic<Bukkit> bukkit;
    private static Server server;
    @BeforeAll static void initializeMinecraftRegistriesWithoutStartingServer() throws Exception {
        Class.forName("net.minecraft.SharedConstants").getMethod("tryDetectVersion").invoke(null);
        Class.forName("net.minecraft.server.Bootstrap").getMethod("bootStrap").invoke(null);
        // Supply the real CraftServer type/version without constructing a server instance.
        server = (Server) mock(Class.forName("org.bukkit.craftbukkit.CraftServer"));
        when(server.getMinecraftVersion()).thenReturn("26.3");
        bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getServer).thenReturn(server);
    }

    @AfterAll static void releaseBukkitBoundary() { if (bukkit != null) bukkit.close(); }

    @Test @Order(1) void uuidMappingResolvesFromRealMinecraftRegistry() {
        assertEquals("net.minecraft.commands.arguments.UuidArgument",
                MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("uuid")).getName());
        assertEquals(26, CraftBukkitReflection.MAJOR_REVISION);
    }

    @Test @Order(2) void itemParserInitializesRealCraftItemStackConversion() {
        assertNotNull(new ItemStackParser<>());
    }

    @Test @Order(3) @SuppressWarnings("unchecked")
    void nativePaperManagerAndMusicBoxCommandsInitialize() {
        var plugin = mock(MusicBox.class);
        var metadata = mock(PluginMeta.class);
        when(metadata.getName()).thenReturn("MusicBox");
        when(plugin.getPluginMeta()).thenReturn(metadata);
        when(plugin.isEnabled()).thenReturn(true);
        when(plugin.getName()).thenReturn("MusicBox");
        var pluginManager = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(pluginManager.getPlugin("MusicBox")).thenReturn(plugin);
        when(plugin.getLifecycleManager()).thenReturn(mock(LifecycleEventManager.class));
        var manager = PaperCommandManager.builder()
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(plugin);
        new MusicBoxExecutor().register(manager);
        assertTrue(manager.hasBrigadierManager());
        assertEquals(17, manager.commands().size());
    }
}
