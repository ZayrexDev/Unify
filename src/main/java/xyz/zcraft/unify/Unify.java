package xyz.zcraft.unify;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.zcraft.unify.api.DataSerializer;
import xyz.zcraft.unify.api.Lockable;
import xyz.zcraft.unify.api.StorageProvider;
import xyz.zcraft.unify.impl.serializer.PlayerInventorySerializer;
import xyz.zcraft.unify.impl.storage.SQLiteStorageProvider;

import java.io.IOException;
import java.util.HashMap;

public class Unify implements DedicatedServerModInitializer {
    private static final Logger LOG = LogManager.getLogger();
    public static HashMap<String, DataSerializer> DATA_HANDLERS = new HashMap<>();
    public static HashMap<String, StorageProvider> STORAGE_PROVIDERS = new HashMap<>();
    private static StorageProvider STORAGE_PROVIDER;
    private static Lockable STORAGE_LOCKABLE = null;
    private Config config;

    @Override
    public void onInitializeServer() {
        try {
            config = Config.loadConfig(FabricLoader.getInstance().getConfigDir().resolve("unify").resolve("config.yaml"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> DATA_HANDLERS.forEach((s, dataSerializer) -> {
            long start = System.currentTimeMillis();
            if(STORAGE_LOCKABLE != null && STORAGE_LOCKABLE.isLocked(handler.player.getUuidAsString())) {
                while(STORAGE_LOCKABLE.isLocked(handler.player.getUuidAsString()) && System.currentTimeMillis() - start < 10 * 1000) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (STORAGE_LOCKABLE != null) {
                if(STORAGE_LOCKABLE.isLocked(handler.player.getUuidAsString())) {
                    handler.disconnect(Text.literal("Can't sync player data"));
                    LOG.error("Cannot sync player data for {}", handler.getPlayer().getUuidAsString());
                    return;
                }
                STORAGE_LOCKABLE.lock(config.getServerUUID(), handler.player.getUuidAsString());
            }
            String dataString = STORAGE_PROVIDER.get(handler.player.getUuidAsString(), s);
            dataSerializer.restoreFromDataString(dataString, handler.player);
        }));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if(STORAGE_LOCKABLE == null || STORAGE_LOCKABLE.isLockedBy(config.getServerUUID(), handler.player.getUuidAsString())) {
                DATA_HANDLERS.forEach((s, dataSerializer) -> {
                    STORAGE_PROVIDER.save(handler.player.getUuidAsString(), s, dataSerializer.getDataString(handler.player));
                });
                STORAGE_LOCKABLE.unlock(config.getServerUUID(), handler.player.getUuidAsString());
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            final StorageProvider storageProvider = STORAGE_PROVIDERS.get(config.getDataStorage().getProvider());
            if (storageProvider != null) {
                if(storageProvider instanceof Lockable lockable) STORAGE_LOCKABLE = lockable;
                STORAGE_PROVIDER = storageProvider;
                storageProvider.init(config.getDataStorage().getParameters(), DATA_HANDLERS.keySet());
                LOG.info("Data storage {} has been successfully loaded", config.getDataStorage().getProvider());
            } else {
                throw new RuntimeException("Storage provider not found:" + config.getDataStorage().getProvider());
            }
        });

        STORAGE_PROVIDERS.put("sqlite", new SQLiteStorageProvider());
        DATA_HANDLERS.put("inv", new PlayerInventorySerializer());
    }
}
