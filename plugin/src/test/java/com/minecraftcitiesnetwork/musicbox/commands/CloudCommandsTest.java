package com.minecraftcitiesnetwork.musicbox.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CloudCommandsTest {
    static class Manager extends CommandManager<CommandSourceStack> {
        Manager() { super(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler()); }
        @Override public boolean hasPermission(CommandSourceStack source, String permission) {
            return source.getSender().hasPermission(permission);
        }
    }
    @Test void cloudDispatchesNoArgsAndGreedyArgumentsToExistingCommand() {
        var command = mock(SubCommand.class);
        var sender = mock(CommandSender.class);
        when(sender.hasPermission("musicbox.use")).thenReturn(true);
        when(command.canExecute(sender)).thenReturn(true);
        var source = mock(CommandSourceStack.class); when(source.getSender()).thenReturn(sender);
        var manager = new Manager();
        new MusicBoxExecutor(Map.of("play",command), () -> true).register(manager);
        manager.commandExecutor().executeCommand(source,"musicbox play").join();
        verify(command).execute(eq(sender),aryEq(new String[0]));
        manager.commandExecutor().executeCommand(source,"musicbox play PlayerOne a song with spaces").join();
        verify(command).execute(eq(sender),aryEq(new String[]{"PlayerOne","a","song","with","spaces"}));
    }
    @Test void deniedSubcommandOrRootPermissionCannotDispatch() {
        var command = mock(SubCommand.class);
        var sender = mock(CommandSender.class);
        var source = mock(CommandSourceStack.class); when(source.getSender()).thenReturn(sender);
        var manager = new Manager();
        new MusicBoxExecutor(Map.of("reload",command), () -> true).register(manager);
        assertThrows(Exception.class, () -> manager.commandExecutor().executeCommand(source,"musicbox reload").join());
        when(sender.hasPermission("musicbox.use")).thenReturn(true);
        assertThrows(Exception.class, () -> manager.commandExecutor().executeCommand(source,"musicbox reload").join());
        verify(command,never()).execute(any(),any());
    }
    @Test void administratorCanReloadAfterFailedLoad() {
        var command = mock(SubCommand.class);
        var sender = mock(CommandSender.class);
        when(sender.hasPermission("musicbox.use")).thenReturn(true);
        when(sender.hasPermission("musicbox.admin")).thenReturn(true);
        when(command.canExecute(sender)).thenReturn(true);
        var source = mock(CommandSourceStack.class); when(source.getSender()).thenReturn(sender);
        var manager = new Manager();
        new MusicBoxExecutor(Map.of("reload",command), () -> false).register(manager);
        manager.commandExecutor().executeCommand(source,"musicbox reload").join();
        verify(command).execute(eq(sender),aryEq(new String[0]));
    }
    private static String[] aryEq(String[] expected) { return org.mockito.AdditionalMatchers.aryEq(expected); }
}
