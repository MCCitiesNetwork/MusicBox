package com.minecraftcitiesnetwork.musicbox.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.minecraftcitiesnetwork.musicbox.Lang;
import com.minecraftcitiesnetwork.musicbox.MusicBox;
import com.minecraftcitiesnetwork.musicbox.commands.subcommands.*;
import com.minecraftcitiesnetwork.musicbox.gui.GUIActions;
import com.minecraftcitiesnetwork.musicbox.players.PlayerWrapper;
import com.minecraftcitiesnetwork.musicbox.utils.ArrayUtils;

import java.util.*;
import java.util.stream.Collectors;

public class MusicBoxExecutor {
    private final Map<String, SubCommand> subs = new HashMap<>();
    private final java.util.function.BooleanSupplier loaded;

    MusicBoxExecutor(Map<String, SubCommand> commands, java.util.function.BooleanSupplier loaded) {
        this.subs.putAll(commands);
        this.loaded = loaded;
    }

    public MusicBoxExecutor() {
        this.loaded = () -> MusicBox.getInstance().isLoaded();

        subs.put("shop", new ShopExecutor());
        subs.put("give", new GiveExecutor());
        subs.put("give_single", new GiveSingleExecutor());
        subs.put("playlist", new PlaylistExecutor());
        subs.put("play", new PlayExecutor());
        subs.put("shutup", new ShutUp(this));
        subs.put("reload", new ReloadExecutor());
        subs.put("silent", new SilentExecutor());
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean recoveryReload = args.length > 0 && args[0].equalsIgnoreCase("reload")
                && sender.hasPermission("musicbox.admin");
        if (!loaded.getAsBoolean() && !recoveryReload) {
            sender.sendMessage(ChatColor.RED + "Plugin not loaded, please wait or check console if wait too long");
            return true;
        }

        if (!sender.hasPermission("musicbox.use")) {
            sender.sendMessage(Lang.NO_PEX.toString());
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player)
                GUIActions.openDefaultInventory(PlayerWrapper.getInstance((Player) sender));
            else
                sendHelp(sender);
            return true;
        }
        SubCommand executor = subs.get(args[0].toLowerCase(Locale.ROOT));
        if (executor == null) {
            sendHelp(sender);
            return true;
        }
        if (executor.canExecute(sender)) {
            executor.execute(sender, ArrayUtils.removeFirst(String.class, args));
        } else {
            sender.sendMessage(Lang.NO_PEX.toString());
        }
        return true;
    }

    public void register(MusicBox plugin) {
        var manager = org.incendo.cloud.paper.PaperCommandManager.builder()
                .executionCoordinator(org.incendo.cloud.execution.ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(plugin);
        register(manager);
    }

    void register(org.incendo.cloud.CommandManager<io.papermc.paper.command.brigadier.CommandSourceStack> manager) {
        manager.command(manager.commandBuilder("musicbox")
                .permission("musicbox.use")
                .handler(ctx -> onCommand(ctx.sender().getSender(), null, "musicbox", new String[0])));
        for (var entry : subs.entrySet()) {
            String name = entry.getKey();
            var builder = manager.commandBuilder("musicbox").literal(name)
                    .permission(source -> org.incendo.cloud.permission.PermissionResult.of(
                            source.getSender().hasPermission("musicbox.use") && entry.getValue().canExecute(source.getSender()),
                            org.incendo.cloud.permission.Permission.of("musicbox.use")));
            manager.command(builder.handler(ctx -> onCommand(ctx.sender().getSender(), null, "musicbox", new String[]{name})));
            manager.command(builder.required("arguments", org.incendo.cloud.parser.standard.StringParser.greedyStringParser(),
                    (ctx, input) -> {
                        String rest = input.remainingInput();
                        String[] args = (name + " " + rest).split(" ", -1);
                        return java.util.concurrent.CompletableFuture.completedFuture(onTabComplete(ctx.sender().getSender(), null, "musicbox", args).stream()
                                .map(org.incendo.cloud.suggestion.Suggestion::suggestion).toList());
                    })
                    .handler(ctx -> onCommand(ctx.sender().getSender(), null, "musicbox",
                            (name + " " + ctx.<String>get("arguments")).split(" "))));
        }
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage(Lang.COMMAND_HELP.toArray());
        if (sender.hasPermission("musicbox.shop")) {
            sender.sendMessage(Lang.COMMAND_HELP_SHOP.toString());
        }
        if (sender.hasPermission("musicbox.give")) {
            sender.sendMessage(Lang.COMMAND_HELP_GIVE.toString());
        }
        if (sender.hasPermission("musicbox.admin")) {
            sender.sendMessage(Lang.ADMIN_HELP.toArray());
        }
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length <= 1) {
            List<String> tabComplete = new LinkedList<>();
            if (sender.hasPermission("musicbox.use")) {
                tabComplete.add("play");
                tabComplete.add("silent");
                if (sender instanceof Player)
                    tabComplete.add("playlist");
            }
            if (sender.hasPermission("musicbox.shop"))
                tabComplete.add("shop");
            if (sender.hasPermission("musicbox.give"))
                tabComplete.add("give");
            if (sender.hasPermission("musicbox.admin")) {
                tabComplete.add("give_single");
                tabComplete.add("shutup");
                tabComplete.add("reload");
            }
            if (args.length == 1)
                return tabComplete
                        .stream()
                        .filter(s -> s.startsWith(args[0]))
                        .collect(Collectors.toList());
            else
                return tabComplete;
        } else {
            SubCommand executor = subs.get(args[0].toLowerCase());
            if (executor != null) {
                if (executor.canExecute(sender))
                    return executor.tabComplete(sender, ArrayUtils.removeFirst(String.class, args));
            }
        }
        return Collections.emptyList();
    }
}
