package xyz.zcraft.unify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Config {
    public static final int CONFIG_VERSION = 1;
    public DefaultSyncSettings defaultSyncOptions;
    public DataStorage dataStorage;
    public boolean forceSync;
    public String serverUUID;
    public int configVersion;

    public static Config loadConfig(Path configPath) throws IOException {
        configPath = configPath.toAbsolutePath();
        if (!Files.exists(configPath)) {
            final Path parent = configPath.getParent();
            if (!Files.exists(parent)) Files.createDirectories(parent);
            Files.createDirectories(parent);

            String defaultConfigString;
            try (InputStream configStream = Objects.requireNonNull(Unify.class.getResourceAsStream("default.yaml"));
                 BufferedReader br = new BufferedReader(new InputStreamReader(configStream))) {
                StringBuilder sb = new StringBuilder();
                br.lines().forEach(s -> sb.append(s).append("\n"));
                defaultConfigString = sb.toString();
            }
            final String randomUUID = UUID.randomUUID().toString();
            defaultConfigString = defaultConfigString.replaceAll("GENERATED_UUID", randomUUID);
            defaultConfigString = defaultConfigString.replaceAll("GENERATED_VERSION", String.valueOf(CONFIG_VERSION));
            Files.writeString(configPath, defaultConfigString);
        }

        Yaml yaml = new Yaml();
        final HashMap<String, Object> root = yaml.load(Files.newInputStream(configPath));

        Config config = new Config();
        config.setServerUUID((String) root.get("server-uuid"));
        config.setConfigVersion((Integer) root.get("config-version"));


        final HashMap<String, Object> dataStorageNode = (HashMap<String, Object>) root.get("data-storage");
        config.setDataStorage(new DataStorage((String) dataStorageNode.get("provider"), (HashMap<String, Object>) dataStorageNode.get("parameters")));

        final HashMap<String, Object> dso = (HashMap<String, Object>) root.get("default-sync-options");
        config.setDefaultSyncOptions(new DefaultSyncSettings((Boolean) dso.get("sync-inventory"), (Boolean) dso.get("sync-advancements"), (Boolean) dso.get("sync-recipes"), (Boolean) dso.get("sync-statistics")));

        return config;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class DefaultSyncSettings {
        public boolean syncInventory;
        public boolean syncAdvancements;
        public boolean syncStatistics;
        public boolean syncRecipes;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class DataStorage {
        public String provider;
        public HashMap<String, Object> parameters;
    }
}
