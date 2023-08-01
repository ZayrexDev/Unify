package xyz.zcraft.unify.api;

import java.util.HashMap;
import java.util.Set;

public abstract class StorageProvider {
    public abstract void init(HashMap<String, Object> args, Set<String> fields);

    public abstract void save(String uuid, String fieldName, String data);

    public abstract String get(String uuid, String fieldName);
    public abstract void close();
}
