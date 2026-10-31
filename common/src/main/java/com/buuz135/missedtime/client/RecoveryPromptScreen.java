package com.buuz135.missedtime.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RecoveryPromptScreen extends Screen {
    private final Component prompt;

    public RecoveryPromptScreen(Component prompt) {
        super(Component.translatable("screen.missedtime.title"));
        this.prompt = prompt;
    }

    @Override
    protected void init() {
        int buttonY = height / 2 + 30;
        addRenderableWidget(Button.builder(Component.translatable("screen.missedtime.recover"), button -> {
            ClientRecoveryState.accept();
            onClose();
        }).bounds(width / 2 - 105, buttonY, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.missedtime.skip"), button -> {
            ClientRecoveryState.decline();
            onClose();
        }).bounds(width / 2 + 5, buttonY, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 45, 0xFFFFFF);
        graphics.drawCenteredString(font, prompt, width / 2, height / 2 - 15, 0xFFD166);
        graphics.drawCenteredString(font, Component.translatable("screen.missedtime.details"), width / 2, height / 2 + 2, 0xB8C0CC);

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
