package com.buuz135.missedtime.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, height / 2 - 45, 0xFFFFFFFF);
        graphics.centeredText(font, prompt, width / 2, height / 2 - 15, 0xFFFFD166);
        graphics.centeredText(font, Component.translatable("screen.missedtime.details"), width / 2, height / 2 + 2, 0xFFB8C0CC);

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
