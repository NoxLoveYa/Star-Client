package com.starclient;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import java.awt.*;

public class HudCheat {
    private static final Identifier EXAMPLE_LAYER = Identifier.of(StarClient.MOD_ID, "watermark");
    private static final String WATERMARK_TEXT = "Star-Client";

    public static void initialize() {
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.MISC_OVERLAYS, EXAMPLE_LAYER, HudCheat::render));

        StarClient.LOGGER.info("Registered Star Client Overlay");
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!CheatOptions.WatermarkEnabled) { return; }
        int backgroundColor = new Color(0, 0, 0, 125).getRGB(); // Transparent black
        int textColor = Color.white.getRGB(); // White

        context.fill(5, 4, 65, 15, 0, backgroundColor);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, WATERMARK_TEXT, 35, 6, textColor);
    }

    // A set to keep track of entities whose glowing state we temporarily changed.
    // This is crucial to ensure we only revert the state for entities we modified.
}