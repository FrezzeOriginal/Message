package net.beeparty.message;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.beeparty.message.commands.MsgCommand;

@Plugin(
        id = "message",
        name = "Message",
        version = "1.0"
)
public class Message {

    private final ProxyServer server;

    @Inject
    public Message(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("message")
                .aliases("msg")
                .plugin(this)
                .build();
        var messageCommand = new MsgCommand(server);
        commandManager.register(commandMeta, messageCommand);
    }
}
