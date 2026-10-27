package com.buuz135.missedtime.mixin;

import com.buuz135.missedtime.client.ClientRecoveryState;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleSystemChat", at = @At("TAIL"))
    private void missedTime$handleSystemChat(ClientboundSystemChatPacket packet, CallbackInfo callback) {
        if (!(packet.content().getContents() instanceof TranslatableContents contents)) {
            return;
        }
        if (contents.getKey().equals("message.missedtime.prompt")) {
            ClientRecoveryState.showPrompt(packet.content());
        } else if (contents.getKey().equals("message.missedtime.started")) {
            ClientRecoveryState.start();
        } else if (contents.getKey().equals("message.missedtime.finished") || contents.getKey().equals("message.missedtime.stopped")) {
            ClientRecoveryState.finish();
        }
    }
}
