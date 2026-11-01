package com.minecraftcitiesnetwork.musicbox.utils;

import com.minecraftcitiesnetwork.musicbox.*;
import com.minecraftcitiesnetwork.musicbox.gui.GUIActions;
import com.minecraftcitiesnetwork.musicbox.players.PlayerWrapper;
import com.minecraftcitiesnetwork.musicbox.song.MusicBoxSong;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EconomyAvailabilityTest {
    @Test void missingProviderDoesNotPoisonUtilityAndLaterRegistrationIsRecognized() {
        try(var bukkit=mockStatic(Bukkit.class); var statics=mockStatic(MusicBox.class)) {
            var plugin=mock(MusicBox.class); var config=mock(MusicBoxConfig.class);
            var settings=mock(MusicBoxConfig.EconomySetting.class);
            statics.when(MusicBox::getInstance).thenReturn(plugin); when(plugin.getConfigObject()).thenReturn(config);
            when(config.getEconomy()).thenReturn(settings); when(settings.isEnable()).thenReturn(true);
            when(settings.getPrice()).thenReturn(12.5);
            var plugins=mock(PluginManager.class); var services=mock(ServicesManager.class);
            bukkit.when(Bukkit::getPluginManager).thenReturn(plugins); bukkit.when(Bukkit::getServicesManager).thenReturn(services);
            when(plugins.isPluginEnabled("Vault")).thenReturn(true);
            assertEquals(12.5,EconomyUtils.getDiscPrice()); assertFalse(EconomyUtils.isEnable());
            var player=mock(Player.class); assertFalse(EconomyUtils.canBuy(player,12.5));
            var wrapper=mock(PlayerWrapper.class); var song=mock(MusicBoxSong.class);
            GUIActions.openShopInventory(wrapper); GUIActions.playerBuyMusic(wrapper,song);
            verifyNoInteractions(wrapper,song);
            var economy=mock(Economy.class);
            RegisteredServiceProvider<Economy> registration=mock(RegisteredServiceProvider.class);
            when(registration.getProvider()).thenReturn(economy);
            when(services.getRegistration(Economy.class)).thenReturn(registration);
            assertTrue(EconomyUtils.isEnable());
            when(settings.isEnable()).thenReturn(false);
            assertFalse(EconomyUtils.buyNoMessage(player,12.5));
            verify(economy,never()).withdrawPlayer(any(Player.class),anyDouble());
        }
    }
}
