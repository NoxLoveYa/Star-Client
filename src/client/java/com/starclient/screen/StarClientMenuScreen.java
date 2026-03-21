package com.starclient.screen;

import java.awt.*;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StarClientMenuScreen extends Screen {
    @Nullable
    private final Screen previousScreen;

    public StarClientMenuScreen(@Nullable Screen previousScreen) {
        super(Component.literal("StarClient"));
        this.previousScreen = previousScreen;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.previousScreen);
        }
    }

    @Override
    public void render(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, new Color(25, 25, 25, 185).getRGB());
        super.render(context, mouseX, mouseY, delta);
    }
}