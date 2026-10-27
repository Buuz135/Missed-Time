package com.buuz135.missedtime.neoforge;

import com.buuz135.missedtime.MissedTime;
import com.buuz135.missedtime.command.MissedTimeCommands;
import com.buuz135.missedtime.world.RecoveryManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.function.BooleanSupplier;

@Mod(MissedTime.MOD_ID)
public final class MissedTimeNeoForge {
    public MissedTimeNeoForge(IEventBus modBus) {
        MissedTime.init();
        RecoveryManager.setAcceleratedTickRunner(server -> {
            BooleanSupplier hasTimeLeft = () -> true;
            NeoForge.EVENT_BUS.post(new ServerTickEvent.Pre(hasTimeLeft, server));
            try {
                server.getAllLevels().forEach(level -> {
                    NeoForge.EVENT_BUS.post(new LevelTickEvent.Pre(hasTimeLeft, level));
                    try {
                        level.tick(hasTimeLeft);
                    } finally {
                        NeoForge.EVENT_BUS.post(new LevelTickEvent.Post(hasTimeLeft, level));
                    }
                });
            } finally {
                NeoForge.EVENT_BUS.post(new ServerTickEvent.Post(hasTimeLeft, server));
            }
        });
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::serverStarted);
        NeoForge.EVENT_BUS.addListener(this::serverStopping);
        NeoForge.EVENT_BUS.addListener(this::serverTick);
        NeoForge.EVENT_BUS.addListener(this::playerJoined);
        NeoForge.EVENT_BUS.addListener(this::playerLeft);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        MissedTimeCommands.register(event.getDispatcher());
    }

    private void serverStarted(ServerStartedEvent event) {
        RecoveryManager.onServerStarted(event.getServer());
    }

    private void serverStopping(ServerStoppingEvent event) {
        RecoveryManager.onServerStopping(event.getServer());
    }

    private void serverTick(ServerTickEvent.Post event) {
        RecoveryManager.onServerTick(event.getServer());
    }

    private void playerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RecoveryManager.onPlayerJoined(player);
        }
    }

    private void playerLeft(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RecoveryManager.onPlayerLeft(player);
        }
    }
}
