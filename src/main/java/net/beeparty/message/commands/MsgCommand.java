package net.beeparty.message.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class MsgCommand implements SimpleCommand {

    private final ProxyServer proxy;
    private final ConcurrentHashMap<UUID, Boolean> playersMessageDisabled;

    public MsgCommand(ProxyServer proxy) {
        this.proxy = proxy;
        playersMessageDisabled = new ConcurrentHashMap<>();
    }

    private static void sendCommandUsage(Player source) {
        Component message = Component.text("/msg <Player|toggle> <Message>", NamedTextColor.YELLOW);
        source.sendMessage(message);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        var args = invocation.arguments();
        if (args.length > 1) return CompletableFuture.completedFuture(Collections.emptyList());

        var list = new ArrayList<>(List.of("toggle"));
        Stream<String> stream = proxy
                .getAllPlayers()
                .stream()
                .map(Player::getUsername);

        if(args.length == 1) {
            stream = stream.filter(str -> str.toLowerCase().startsWith(args[0].toLowerCase()));
        }

        list.addAll(stream.toList());
        return CompletableFuture.completedFuture(list);
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player source)) {
            invocation.source().sendMessage(Component.text(
                    "This command can only be used by a player",
                    NamedTextColor.RED
            ));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 2) {
            if (args.length == 1 && args[0].equals("toggle")) {
                var currentToggleState = playersMessageDisabled.getOrDefault(source.getUniqueId(), false);
                playersMessageDisabled.put(source.getUniqueId(), !currentToggleState);

                String oldToggleStateStr = currentToggleState ? "enabled" : "disabled";
                String newToggleStateStr = !currentToggleState ? "enabled" : "disabled";

                source.sendMessage(Component.text("§7Changed message toggle state from §e" + oldToggleStateStr +
                        " §7to §e" + newToggleStateStr));
                return;
            }

            source.sendMessage(Component.text("/msg <Player> <Message> | " + source.getClass(), NamedTextColor.YELLOW));
            return;
        }

        String playerName = args[0];
        String rawMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        var currentToggleState = playersMessageDisabled.getOrDefault(source.getUniqueId(), false);
        if (currentToggleState) {
            source.sendMessage(Component.text("§7To send private messages you have to enable them again by using §e/msg toggle"));
            return;
        }

        if (playerName == null || playerName.isBlank() || rawMessage.isEmpty()) {
            sendCommandUsage(source);
            return;
        }

        proxy.getPlayer(playerName).ifPresentOrElse(player -> {
            var playerToggleState = playersMessageDisabled.getOrDefault(player.getUniqueId(), false);
            if (playerToggleState) {
                source.sendMessage(Component.text("§7The player §e" + playerName + " §7has disabled private messages"));
                return;
            }

            player.sendMessage(Component.text("§8[§eMSG§8] §7From §e" + source.getUsername() + "§7: §e" + rawMessage));
            source.sendMessage(Component.text("§8[§eMSG§8] §7To §e" + player.getUsername() + "§7: §e" + rawMessage));
        }, () -> source.sendMessage(Component.text("Player " + playerName + " was not found", NamedTextColor.RED)));
    }

}
