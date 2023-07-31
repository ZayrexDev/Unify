package xyz.zcraft.unify.impl.serializer;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.zcraft.unify.api.DataSerializer;

import java.util.HashMap;
import java.util.Map;

public class PlayerAdvancementsSerializer extends DataSerializer {
    public static final HashMap<ServerPlayerEntity, Map<Advancement, AdvancementProgress>> progresses = new HashMap<>();
    @Override
    public boolean restoreFromDataString(String data, ServerPlayerEntity player) {
        // TODO FINISH THIS
        return false;
    }

    @Override
    public String getDataString(ServerPlayerEntity player) {
        // TODO FINISH THIS
        return null;
    }
}
