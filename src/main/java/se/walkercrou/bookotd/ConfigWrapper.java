package se.walkercrou.bookotd;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.BookView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ConfigWrapper {

    private static final String NODE_MOTD = "motd";
    private static final String NODE_BOOK = "book";
    private static final String NODE_VERSION = "version";
    private static final String NODE_PLAYER_DATA = "playerData";

    private static final String ASSET_DEFAULT_CONF = "default.conf";

    private static final TypeToken<BookView> TOKEN_BOOK_VIEW = TypeToken.of(BookView.class);
    private static final TypeToken<Map<UUID, Integer>> TOKEN_PLAYER_DATA = new TypeToken<Map<UUID, Integer>>() {};

    private final BookOTD plugin;
    private Optional<HoconConfigurationLoader> loader = Optional.empty();
    private Optional<CommentedConfigurationNode> root = Optional.empty();

    public ConfigWrapper(BookOTD plugin) {
        this.plugin = plugin;
    }

    public boolean load() {
        try {
            this.plugin.log.info("Loading config.");
            Path path = this.plugin.getConfigPath();
            if (Files.notExists(path)) {
                this.plugin.self.getAsset(ASSET_DEFAULT_CONF).get().copyToFile(path);
            }

            this.loader = Optional.of(HoconConfigurationLoader.builder().setPath(path).build());
            this.root = Optional.of(this.loader.get().load());

            BookView.builder().build(); // Make sure BookView is statically initialized
            ConfigurationNode motdNode = this.root.get().getNode(NODE_MOTD);
            this.plugin.motd = motdNode.getNode(NODE_BOOK).getValue(TOKEN_BOOK_VIEW);
            this.plugin.currentVersion = motdNode.getNode(NODE_VERSION).getInt();
            this.plugin.playerVersionMap = this.root.get().getNode(NODE_PLAYER_DATA)
                    .getValue(TOKEN_PLAYER_DATA, Maps.newHashMap());

        } catch (IOException | ObjectMappingException e) {
            this.plugin.log.error("Failed to load config", e);
            return false;
        }
        return true;
    }

    public boolean save() {
        try {
            this.plugin.log.info("Saving config.");
            this.root.get().getNode(NODE_PLAYER_DATA).setValue(TOKEN_PLAYER_DATA, this.plugin.playerVersionMap);
            this.loader.get().save(this.root.get());
        } catch (IOException | ObjectMappingException e) {
            this.plugin.log.error("Failed to save config", e);
            return false;
        }
        return true;
    }

}
