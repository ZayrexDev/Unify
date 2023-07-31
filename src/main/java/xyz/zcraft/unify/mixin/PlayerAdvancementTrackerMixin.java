package xyz.zcraft.unify.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.zcraft.unify.impl.serializer.PlayerAdvancementsSerializer;

import java.nio.file.Path;
import java.util.Map;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
    @Shadow @Final private Map<Advancement, AdvancementProgress> progress;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(DataFixer dataFixer, PlayerManager playerManager, ServerAdvancementLoader advancementLoader, Path filePath, ServerPlayerEntity owner, CallbackInfo ci) {
        PlayerAdvancementsSerializer.progresses.put(owner, progress);
    }
}
