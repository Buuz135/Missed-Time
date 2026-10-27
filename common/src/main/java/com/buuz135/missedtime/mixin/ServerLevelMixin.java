package com.buuz135.missedtime.mixin;

import com.buuz135.missedtime.world.RecoveryManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void missedTime$freeze(BooleanSupplier hasTimeLeft, CallbackInfo callback) {
        if (RecoveryManager.shouldFreezeWorld()) {
            callback.cancel();
        }
    }
}
