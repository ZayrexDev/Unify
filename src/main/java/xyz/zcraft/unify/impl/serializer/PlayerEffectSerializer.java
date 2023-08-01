package xyz.zcraft.unify.impl.serializer;

import com.alibaba.fastjson2.JSONArray;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.zcraft.unify.api.DataSerializer;

public class PlayerEffectSerializer extends DataSerializer {
    @Override
    public void restoreFromDataString(String data, ServerPlayerEntity player) {
        if(data == null) return;
        try {
            player.getStatusEffects().clear();
            for (Object o : JSONArray.parse(data)) {
                player.addStatusEffect(StatusEffectInstance.fromNbt(StringNbtReader.parse((String) o)));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDataString(ServerPlayerEntity player) {
        JSONArray arr = new JSONArray();
        for (StatusEffectInstance statusEffect : player.getStatusEffects()) {
            arr.add(statusEffect.writeNbt(new NbtCompound()).asString());
        }
        return arr.toString();
    }
}
