package com.buuz135.missedtime.world;

import com.buuz135.missedtime.config.MissedTimeConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.UUID;
import java.util.function.Consumer;

public final class RecoveryManager {
    private static final double[] SPEEDS = {0, 0.25, 0.5, 1.0, 1.5, 2.0, 2.5, 5, 10, 25};
    private static final double MINIMUM_SMOOTH_RATE = 20.0;
    private static Consumer<MinecraftServer> acceleratedTickRunner = RecoveryManager::tickLevels;
    private static MinecraftServer server;
    private static MissedTimeSavedData data;
    private static UUID promptOwner;
    private static UUID activePlayerId;
    private static RecoveryParticipant participant;
    private static long pendingTicks;
    private static long totalTicks;
    private static int elapsedServerTicks;
    private static int speedIndex = 3;
    private static double tickAccumulator;
    private static boolean paused;
    private static boolean advancing;

    private RecoveryManager() {
    }

    public static void setAcceleratedTickRunner(Consumer<MinecraftServer> runner) {
        acceleratedTickRunner = runner;
    }

    public static void onServerStarted(MinecraftServer minecraftServer) {
        if (!minecraftServer.isSingleplayer()) {
            resetSession();
            server = null;
            data = null;
            return;
        }
        server = minecraftServer;
        data = minecraftServer.overworld().getDataStorage().computeIfAbsent(MissedTimeSavedData::load, MissedTimeSavedData::new, MissedTimeSavedData.NAME);
        resetSession();
    }

    public static void onServerStopping(MinecraftServer minecraftServer) {
        if (participant != null) {
            ServerPlayer player = minecraftServer.getPlayerList().getPlayer(activePlayerId);
            if (player != null) {
                participant.restore(player);
            }
        }
        touchTimestamp();
        resetSession();
        server = null;
        data = null;
    }

    public static void onPlayerJoined(ServerPlayer player) {
        if (!player.getServer().isSingleplayer() || server == null || data == null || activePlayerId != null || promptOwner != null) {
            return;
        }
        long now = System.currentTimeMillis();
        long maximumMillis = MissedTimeConfig.maximumOfflineHours() * 3_600_000L;
        long offlineMillis = Math.min(Math.max(0L, now - data.lastPlayedEpochMillis()), maximumMillis);
        long offlineMinutes = offlineMillis / 60_000L;
        if (offlineMinutes < MissedTimeConfig.minimumOfflineMinutes()) {
            touchTimestamp();
            return;
        }
        long ticksPerMinute = MissedTimeConfig.acceleratedTicksPerOfflineMinute();
        pendingTicks = offlineMinutes > Long.MAX_VALUE / ticksPerMinute ? Long.MAX_VALUE : offlineMinutes * ticksPerMinute;
        if (pendingTicks <= 0) {
            touchTimestamp();
            return;
        }
        totalTicks = pendingTicks;
        promptOwner = player.getUUID();
        player.sendSystemMessage(Component.translatable("message.missedtime.prompt", formatDuration(offlineMillis), pendingTicks).withStyle(ChatFormatting.GOLD));
    }

    public static void onPlayerLeft(ServerPlayer player) {
        if (isParticipant(player)) {
            if (participant != null) {
                participant.restore(player);
                participant = null;
            }
            finish(false);
        }
        if (promptOwner != null && promptOwner.equals(player.getUUID())) {
            promptOwner = null;
            pendingTicks = 0;
            totalTicks = 0;
        }
        if (server != null && server.getPlayerCount() <= 1) {
            touchTimestamp();
        }
    }

    public static void onServerTick(MinecraftServer minecraftServer) {
        if (advancing || !minecraftServer.isSingleplayer()) {
            return;
        }
        if (server == null || data == null) {
            onServerStarted(minecraftServer);
        }
        if (minecraftServer.getTickCount() % 20 == 0 && minecraftServer.getPlayerCount() > 0) {
            touchTimestamp();
        }
        if (activePlayerId == null) {
            return;
        }
        ServerPlayer player = minecraftServer.getPlayerList().getPlayer(activePlayerId);
        if (player == null) {
            finish(false);
            return;
        }
        if (participant != null) {
            participant.tether(player, MissedTimeConfig.spectatorRadius());
        }
        if (paused) {
            if (minecraftServer.getTickCount() % 20 == 0) {
                sendStatus(player);
            }
            return;
        }
        elapsedServerTicks++;
        double rate = calculateRate();
        tickAccumulator += rate / 20.0;
        int ticksThisFrame = (int) Math.min(tickAccumulator, pendingTicks);
        tickAccumulator -= ticksThisFrame;
        advancing = true;
        try {
            for (int index = 0; index < ticksThisFrame && pendingTicks > 0; index++) {
                acceleratedTickRunner.accept(minecraftServer);
                pendingTicks--;
            }
        } finally {
            advancing = false;
        }
        if (minecraftServer.getTickCount() % 20 == 0) {
            sendStatus(player);
        }
        if (pendingTicks <= 0) {
            finish(true);
        }
    }

    private static void tickLevels(MinecraftServer minecraftServer) {
        minecraftServer.getAllLevels().forEach(level -> level.tick(() -> true));
    }

    private static double calculateRate() {
        double speed = SPEEDS[speedIndex];
        if (speed == 0) {
            return 0;
        }
        double targetRate = Math.max(MINIMUM_SMOOTH_RATE, MissedTimeConfig.acceleratedTicksPerSecond() * speed);
        double rampProgress = Math.min(1.0, elapsedServerTicks / (MissedTimeConfig.rampUpSeconds() * 20.0));
        return MINIMUM_SMOOTH_RATE + (targetRate - MINIMUM_SMOOTH_RATE) * rampProgress;
    }

    public static int accept(ServerPlayer player) {
        if (promptOwner == null || !promptOwner.equals(player.getUUID()) || pendingTicks <= 0) {
            player.sendSystemMessage(Component.translatable("message.missedtime.unavailable").withStyle(ChatFormatting.RED));
            return 0;
        }
        promptOwner = null;
        return beginRecovery(player);
    }

    public static int simulate(ServerPlayer player, int offlineMinutes) {
        if (activePlayerId != null) {
            player.sendSystemMessage(Component.translatable("message.missedtime.already_active").withStyle(ChatFormatting.RED));
            return 0;
        }
        long maximumMinutes = MissedTimeConfig.maximumOfflineHours() * 60L;
        long appliedMinutes = Math.min(offlineMinutes, maximumMinutes);
        long ticksPerMinute = MissedTimeConfig.acceleratedTicksPerOfflineMinute();
        pendingTicks = appliedMinutes > Long.MAX_VALUE / ticksPerMinute ? Long.MAX_VALUE : appliedMinutes * ticksPerMinute;
        totalTicks = pendingTicks;
        promptOwner = null;
        player.sendSystemMessage(Component.translatable("message.missedtime.simulation", appliedMinutes, pendingTicks).withStyle(ChatFormatting.GOLD));
        return beginRecovery(player);
    }

    private static int beginRecovery(ServerPlayer player) {
        activePlayerId = player.getUUID();
        participant = MissedTimeConfig.recoveryParticipantEnabled() ? RecoveryParticipant.capture(player) : null;
        elapsedServerTicks = 0;
        speedIndex = 3;
        tickAccumulator = 0;
        paused = false;
        player.sendSystemMessage(Component.translatable("message.missedtime.started", pendingTicks).withStyle(ChatFormatting.GREEN));
        sendStatus(player);
        return 1;
    }

    public static int decline(ServerPlayer player) {
        if (promptOwner == null || !promptOwner.equals(player.getUUID())) {
            return 0;
        }
        promptOwner = null;
        pendingTicks = 0;
        totalTicks = 0;
        touchTimestamp();
        player.sendSystemMessage(Component.translatable("message.missedtime.declined").withStyle(ChatFormatting.GRAY));
        return 1;
    }

    public static int togglePause(ServerPlayer player) {
        if (!isParticipant(player)) {
            return 0;
        }
        paused = !paused;
        player.sendSystemMessage(Component.translatable(paused ? "message.missedtime.paused" : "message.missedtime.resumed"));
        return 1;
    }

    public static int faster(ServerPlayer player) {
        if (!isParticipant(player)) {
            return 0;
        }
        int previousSpeedIndex = speedIndex;
        speedIndex = Math.min(SPEEDS.length - 1, speedIndex + 1);
        if (speedIndex != previousSpeedIndex) {
            player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 0.8F, speedPitch());
        }
        sendStatus(player);
        return 1;
    }

    public static int slower(ServerPlayer player) {
        if (!isParticipant(player)) {
            return 0;
        }
        int previousSpeedIndex = speedIndex;
        speedIndex = Math.max(0, speedIndex - 1);
        if (speedIndex != previousSpeedIndex) {
            player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 0.8F, speedPitch());
        }
        sendStatus(player);
        return 1;
    }

    private static float speedPitch() {
        double multiplier = SPEEDS[speedIndex];
        double maximumMultiplier = SPEEDS[SPEEDS.length - 1];
        double normalizedMultiplier = Math.log1p(multiplier) / Math.log1p(maximumMultiplier);
        return (float) (0.5 + normalizedMultiplier * 1.5);
    }

    public static int stop(ServerPlayer player) {
        if (!isParticipant(player)) {
            return 0;
        }
        finish(false);
        return 1;
    }

    public static int status(ServerPlayer player) {
        if (!isParticipant(player)) {
            player.sendSystemMessage(Component.translatable("message.missedtime.inactive"));
            return 0;
        }
        sendStatus(player);
        return 1;
    }

    public static boolean shouldFreezeWorld() {
        return activePlayerId != null && paused && !advancing;
    }

    private static boolean isParticipant(ServerPlayer player) {
        return activePlayerId != null && activePlayerId.equals(player.getUUID());
    }

    private static void finish(boolean completed) {
        if (server != null && activePlayerId != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(activePlayerId);
            if (player != null) {
                if (participant != null) {
                    participant.restore(player);
                }
                player.sendSystemMessage(Component.translatable(completed ? "message.missedtime.finished" : "message.missedtime.stopped").withStyle(completed ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
            }
        }
        touchTimestamp();
        resetSession();
    }

    private static void sendStatus(ServerPlayer player) {
        long processed = Math.max(0, totalTicks - pendingTicks);
        int percent = totalTicks == 0 ? 100 : (int) (processed * 100 / totalTicks);
        double rate = paused ? 0 : calculateRate();
        String state = paused ? "paused" : String.format("%.1f t/s", rate);
        player.displayClientMessage(Component.translatable("message.missedtime.status", percent, pendingTicks, SPEEDS[speedIndex], state, formatEta(rate)), true);
    }

    private static String formatEta(double rate) {
        if (rate <= 0) {
            return "--";
        }
        long totalSeconds = Math.max(1, (long) Math.ceil(pendingTicks / rate));
        long seconds = totalSeconds % 60;
        long totalMinutes = totalSeconds / 60;
        long minutes = totalMinutes % 60;
        long totalHours = totalMinutes / 60;
        long hours = totalHours % 24;
        long days = totalHours / 24;
        if (days > 0) {
            return days + "d " + hours + "h";
        }
        if (totalHours > 0) {
            return totalHours + "h " + minutes + "m";
        }
        if (totalMinutes > 0) {
            return totalMinutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    private static void touchTimestamp() {
        if (data != null) {
            data.setLastPlayedEpochMillis(System.currentTimeMillis());
        }
    }

    private static void resetSession() {
        promptOwner = null;
        activePlayerId = null;
        participant = null;
        pendingTicks = 0;
        totalTicks = 0;
        elapsedServerTicks = 0;
        speedIndex = 2;
        tickAccumulator = 0;
        paused = false;
        advancing = false;
    }

    private static String formatDuration(long millis) {
        long minutes = millis / 60_000L;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) {
            return days + "d " + hours % 24 + "h";
        }
        if (hours > 0) {
            return hours + "h " + minutes % 60 + "m";
        }
        return minutes + "m";
    }
}
