package xyz.zcraft.unify.api;

public interface Lockable {
    void lock(String serverUuid, String uuid);
    boolean isLockedBy(String serverUuid, String uuid);
    boolean isLocked(String uuid);
    void unlock(String serverUuid, String uuid);
}
