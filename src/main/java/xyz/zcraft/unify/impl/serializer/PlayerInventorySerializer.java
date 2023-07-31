package xyz.zcraft.unify.impl.serializer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import xyz.zcraft.unify.api.DataSerializer;

public class PlayerInventorySerializer extends DataSerializer {
    @Override
    public boolean restoreFromDataString(String data, ServerPlayerEntity player) {
        if(data == null) return true;
        final JSONObject inventoryData = JSON.parseObject(data);
        JSONArray mainJson = inventoryData.getJSONArray("main");
        JSONArray armorJSON = inventoryData.getJSONArray("armor");
        JSONArray offHandJSON = inventoryData.getJSONArray("offHand");

        DefaultedList<ItemStack> main = player.getInventory().main;
        DefaultedList<ItemStack> armor = player.getInventory().armor;
        DefaultedList<ItemStack> offHand = player.getInventory().offHand;

        try {
            for (int i = 0; i < mainJson.size(); i++) {
                main.set(i, ItemStack.fromNbt(StringNbtReader.parse(mainJson.get(i).toString())));
            }
            for (int i = 0; i < armorJSON.size(); i++) {
                armor.set(i, ItemStack.fromNbt(StringNbtReader.parse(armorJSON.get(i).toString())));
            }
            for (int i = 0; i < offHandJSON.size(); i++) {
                offHand.set(i, ItemStack.fromNbt(StringNbtReader.parse(offHandJSON.get(i).toString())));
            }
        } catch (CommandSyntaxException e) {
            return false;
        }

        return true;
    }

    @Override
    public String getDataString(ServerPlayerEntity player) {
        final JSONObject invJson = new JSONObject();

        final JSONArray mainJson = new JSONArray();
        for (ItemStack itemStack : player.getInventory().main) {
            mainJson.add(itemStack.writeNbt(new NbtCompound()).asString());
        }
        invJson.put("main", mainJson);

        final JSONArray armorJson = new JSONArray();
        for (ItemStack itemStack : player.getInventory().armor) {
            armorJson.add(itemStack.writeNbt(new NbtCompound()).asString());
        }
        invJson.put("armor", armorJson);

        final JSONArray offHandJson = new JSONArray();
        for (ItemStack itemStack : player.getInventory().offHand) {
            offHandJson.add(itemStack.writeNbt(new NbtCompound()).asString());
        }
        invJson.put("offHand", offHandJson);

        return invJson.toString();
    }
}
