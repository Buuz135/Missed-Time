package com.buuz135.missedtime.mixin;

import com.buuz135.missedtime.client.ClientRecoveryState;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("TAIL"))
    private void missedTime$keyPress(long window, int key, int scanCode, int action, int modifiers, CallbackInfo callback) {
        if (action != GLFW.GLFW_PRESS || !ClientRecoveryState.active() || Minecraft.getInstance().screen != null) {
            return;
        }
        if (key == GLFW.GLFW_KEY_P) {
            ClientRecoveryState.sendCommand("missedtime pause");
        } else if (key == GLFW.GLFW_KEY_EQUAL || key == GLFW.GLFW_KEY_KP_ADD) {
            ClientRecoveryState.sendCommand("missedtime faster");
        } else if (key == GLFW.GLFW_KEY_MINUS || key == GLFW.GLFW_KEY_KP_SUBTRACT) {
            ClientRecoveryState.sendCommand("missedtime slower");
        } else if (key == GLFW.GLFW_KEY_END) {
            ClientRecoveryState.sendCommand("missedtime stop");
            ClientRecoveryState.finish();
        }
    }
}
