package com.buuz135.missedtime.fabric;

import com.buuz135.missedtime.MissedTime;
import com.buuz135.missedtime.command.MissedTimeCommands;
import com.buuz135.missedtime.world.RecoveryManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class MissedTimeFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MissedTime.init();
        RecoveryManager.setAcceleratedTickRunner(server -> {
            ServerTickEvents.START_SERVER_TICK.invoker().onStartTick(server);
            try {
                server.getAllLevels().forEach(level -> {
                    ServerTickEvents.START_LEVEL_TICK.invoker().onStartTick(level);
                    try {
                        level.tick(() -> true);
                    } finally {
                        ServerTickEvents.END_LEVEL_TICK.invoker().onEndTick(level);
                    }
                });
            } finally {
                ServerTickEvents.END_SERVER_TICK.invoker().onEndTick(server);
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> MissedTimeCommands.register(dispatcher));
        ServerLifecycleEvents.SERVER_STARTED.register(RecoveryManager::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(RecoveryManager::onServerStopping);
        ServerTickEvents.END_SERVER_TICK.register(RecoveryManager::onServerTick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> RecoveryManager.onPlayerJoined(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> RecoveryManager.onPlayerLeft(handler.player));
    }
}
