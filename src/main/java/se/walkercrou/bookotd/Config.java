package se.walkercrou.bookotd;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.notExists;

import com.google.common.base.Objects;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.text.BookView;

import java.io.IOException;
import java.nio.file.Path;

@ConfigSerializable
public final class Config {

    private static final TypeToken<Config> TOKEN_CONFIG = TypeToken.of(Config.class);

    @Setting int version;
    @Setting BookView book;

    private Config() {}

    public static Config load(Path path, Asset defaultConfig) throws IOException, ObjectMappingException {
        if (notExists(path)) {
            createDirectories(path.getParent());
            defaultConfig.copyToFile(path);
        }
        BookView.builder().build();
        return createLoader(path).load().getNode("motd").getValue(TOKEN_CONFIG);
    }

    public Config save(Path path) throws IOException, ObjectMappingException {
        if (notExists(path)) {
            createDirectories(path.getParent());
            createFile(path);
        }
        HoconConfigurationLoader loader = createLoader(path);
        ConfigurationNode root = loader.createEmptyNode().getNode("motd").setValue(TOKEN_CONFIG, this);
        loader.save(root);
        return this;
    }

    private static HoconConfigurationLoader createLoader(Path path) {
        return HoconConfigurationLoader.builder().setPath(path).build();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("version", this.version).add("book", this.book).toString();
    }

}
