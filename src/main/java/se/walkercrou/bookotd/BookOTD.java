package se.walkercrou.bookotd;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.BookView;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Plugin(id = "bookotd",
        name = "BookOTD",
        version = "1.0.0",
        description = "Simple plugin for displaying a \"message of the day\" to players as a book.",
        authors = { "windy", "Zidane", "gabizou" },
        dependencies = {
            @Dependency(id = "tabcompletemagic", version = "2.0.0"),
            @Dependency(id = "org.kitteh.spectastic", version = "1.0.0"),
            @Dependency(id = "worldedit", version = "1.0.0")
        }
)
@SuppressWarnings("NullableProblems")
public class BookOTD {

    @Inject public Logger log;
    @Inject public PluginContainer self;
    @Inject @DefaultConfig(sharedRoot = true) private Path configPath;

    private ConfigWrapper config;
    protected BookView motd;
    protected int currentVersion;
    protected Map<UUID, Integer> playerVersionMap;

    @Listener
    public void onStart(GameStartedServerEvent event) {
        this.log.info("Initializing...");
        this.config = new ConfigWrapper(this);
        this.config.load();
        this.log.info("Initialized " + this.self.getName() + " [v" + this.self.getVersion().get() + "].");
    }

    @Listener
    public void onStop(GameStoppedServerEvent event) {
        this.log.info("Shutting down...");
        this.config.save();
        this.log.info("Done.");
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        UUID uid = player.getUniqueId();
        if (!this.playerVersionMap.containsKey(uid) || this.playerVersionMap.get(uid) < this.currentVersion) {
            player.sendBookView(this.motd);
            this.playerVersionMap.put(uid, this.currentVersion);
        }
    }

    public Path getConfigPath() {
        return this.configPath;
    }

    public Path getConfigDir() {
        return this.configPath.getParent();
    }

}
