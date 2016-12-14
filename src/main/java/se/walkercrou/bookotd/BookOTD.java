package se.walkercrou.bookotd;

import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

@Plugin(id = "bookotd", authors = { "windy" })
public class BookOTD {

    @Inject public Logger log;
    @Inject public PluginContainer self;

    @Inject @DefaultConfig(sharedRoot = false) private Path configPath;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    private Path historyPath;

    private Config config;
    private final History history = new History();

    @Listener
    public void onStart(GameStartedServerEvent event) {
        this.log.info("Starting...");
        if (init())
            this.log.info("Started.");
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        this.log.info("Reloading...");
        this.config = null;
        if (init())
            this.log.info("Reloaded.");
    }

    private boolean init() {
        try {
            this.config = Config.load(this.configPath, this.self.getAsset("default.conf").get());

            this.historyPath = this.configDir.resolve("history.conf");
            this.history.load(this.historyPath);

            return true;
        } catch (IOException | ObjectMappingException e) {
            this.log.error("An error occurred while initializing the plugin.", e);
            return false;
        }
    }

    @Listener
    public void onStop(GameStoppedServerEvent event) {
        this.log.info("Stopping...");
        try {
            this.history.save(this.historyPath);
        } catch (IOException | ObjectMappingException e) {
            this.log.info("An error occurred while stopping the plugin.", e);
        }
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        UUID uid = player.getUniqueId();
        Map<UUID, Integer> playerVersionMap = this.history.playerVersionMap;
        int currentVersion = this.config.version;
        if (!playerVersionMap.containsKey(uid) || playerVersionMap.get(uid) < currentVersion) {
            player.sendBookView(this.config.book);
            playerVersionMap.put(uid, currentVersion);
        }
    }

}
