package me.steven.bodiesbodies.mixin;

import me.steven.bodiesbodies.BodiesBodies;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow @Final public PlayerEntity player;

    @Shadow public abstract boolean isEmpty();

    @Inject(method = "dropAll", at = @At("INVOKE"))
    private void bodiesbodies_saveItems(CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayerEntity && !isEmpty())
            BodiesBodies.createDeadBody(serverPlayerEntity);
    }
}
