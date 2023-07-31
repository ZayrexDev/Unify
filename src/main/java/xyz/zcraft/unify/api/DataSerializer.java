package xyz.zcraft.unify.api;

import net.minecraft.server.network.ServerPlayerEntity;

public abstract class DataSerializer {
    public abstract boolean restoreFromDataString(String data, ServerPlayerEntity player);
    public abstract String getDataString(ServerPlayerEntity player);
}
