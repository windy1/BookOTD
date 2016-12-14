package se.walkercrou.bookotd;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.notExists;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public final class History {

    private static final TypeToken<Map<UUID, Integer>> TOKEN_MAP = new TypeToken<Map<UUID, Integer>>() {};

    final Map<UUID, Integer> playerVersionMap = Maps.newHashMap();

    public History load(Path path) throws IOException, ObjectMappingException {
        if (notExists(path)) {
            createDirectories(path.getParent());
            createFile(path);
        }
        this.playerVersionMap.clear();
        Map<UUID, Integer> data = createLoader(path).load().getValue(TOKEN_MAP);
        if (data != null)
            this.playerVersionMap.putAll(data);
        return this;
    }

    public History save(Path path) throws IOException, ObjectMappingException {
        if (notExists(path)) {
            createDirectories(path.getParent());
            createFile(path);
        }
        HoconConfigurationLoader loader = createLoader(path);
        ConfigurationNode root = loader.createEmptyNode().setValue(TOKEN_MAP, this.playerVersionMap);
        loader.save(root);
        return this;
    }

    private HoconConfigurationLoader createLoader(Path path) {
        return HoconConfigurationLoader.builder().setPath(path).build();
    }

}
