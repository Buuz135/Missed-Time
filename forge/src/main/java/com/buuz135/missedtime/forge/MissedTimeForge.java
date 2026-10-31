package com.buuz135.missedtime.forge;

import com.buuz135.missedtime.MissedTime;
import com.buuz135.missedtime.command.MissedTimeCommands;
import com.buuz135.missedtime.world.RecoveryManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.function.BooleanSupplier;

@Mod(MissedTime.MOD_ID)
public final class MissedTimeForge {
    public MissedTimeForge() {
        MissedTime.init();
        RecoveryManager.setAcceleratedTickRunner(server -> {
            BooleanSupplier hasTimeLeft = () -> true;
            MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.START, hasTimeLeft, server));
            try {
                server.getAllLevels().forEach(level -> {
                    MinecraftForge.EVENT_BUS.post(new TickEvent.LevelTickEvent(LogicalSide.SERVER, TickEvent.Phase.START, level, hasTimeLeft));
                    try {
                        level.tick(hasTimeLeft);
                    } finally {
                        MinecraftForge.EVENT_BUS.post(new TickEvent.LevelTickEvent(LogicalSide.SERVER, TickEvent.Phase.END, level, hasTimeLeft));
                    }
                });
            } finally {
                MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.END, hasTimeLeft, server));
            }
        });
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopping);
        MinecraftForge.EVENT_BUS.addListener(this::serverTick);
        MinecraftForge.EVENT_BUS.addListener(this::playerJoined);
        MinecraftForge.EVENT_BUS.addListener(this::playerLeft);
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

    private void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            RecoveryManager.onServerTick(event.getServer());
        }
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
