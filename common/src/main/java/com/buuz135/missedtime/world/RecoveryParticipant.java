package com.buuz135.missedtime.world;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

public final class RecoveryParticipant {
    private final UUID playerId;
    private final ResourceKey<Level> dimension;
    private final Vec3 position;
    private final float yRot;
    private final float xRot;
    private final GameType gameMode;
    private final boolean invulnerable;
    private final boolean flying;
    private final boolean mayfly;

    private RecoveryParticipant(ServerPlayer player) {
        playerId = player.getUUID();
        dimension = player.level().dimension();
        position = player.position();
        yRot = player.getYRot();
        xRot = player.getXRot();
        gameMode = player.gameMode.getGameModeForPlayer();
        invulnerable = player.isInvulnerable();
        flying = player.getAbilities().flying;
        mayfly = player.getAbilities().mayfly;
    }

    public static RecoveryParticipant capture(ServerPlayer player) {
        RecoveryParticipant participant = new RecoveryParticipant(player);
        player.setInvulnerable(true);
        player.setGameMode(GameType.ADVENTURE);
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        return participant;
    }

    public boolean owns(ServerPlayer player) {
        return playerId.equals(player.getUUID());
    }

    public void tether(ServerPlayer player, int radius) {
        boolean wrongDimension = player.level().dimension() != dimension;
        boolean outsideRadius = player.position().distanceToSqr(position) > (double) radius * radius;
        if (wrongDimension || outsideRadius) {
            ServerLevel target = player.server.getLevel(dimension);
            if (target != null) {
                player.teleportTo(target, position.x, position.y, position.z, Set.of(), yRot, xRot);
            }
        }
    }

    public void restore(ServerPlayer player) {
        ServerLevel target = player.server.getLevel(dimension);
        if (target != null) {
            player.teleportTo(target, position.x, position.y, position.z, Set.of(), yRot, xRot);
        }
        player.setGameMode(GameType.SURVIVAL);
        player.setInvulnerable(false);
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }
}
