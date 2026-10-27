package com.buuz135.missedtime.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ClientRecoveryState {
    private static boolean active;

    private ClientRecoveryState() {
    }

    public static void showPrompt(Component message) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> minecraft.setScreen(new RecoveryPromptScreen(message)));
    }

    public static void accept() {
        sendCommand("missedtime accept");
        active = true;
    }

    public static void decline() {
        sendCommand("missedtime decline");
        active = false;
    }

    public static void finish() {
        active = false;
    }

    public static void start() {
        active = true;
    }

    public static boolean active() {
        return active;
    }

    public static void sendCommand(String command) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.connection.sendCommand(command);
        }
    }
}
