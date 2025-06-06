package com.starclient.render;

import com.starclient.utils.CheatOptions;
import com.starclient.StarClient;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class HudCheat {
    private static final Identifier EXAMPLE_LAYER = Identifier.of(StarClient.MOD_ID, "watermark");
    private static final String WATERMARK_TEXT = "Star-Client";

    public static void initialize() {
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.MISC_OVERLAYS, EXAMPLE_LAYER, HudCheat::render));

        StarClient.LOGGER.info("Registered Star Client Overlay");
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        if (CheatOptions.WatermarkEnabled) { renderWatermark(context, tickCounter); }
        renderLabelEsp(context, tickCounter);
    }

    public static double getCurrentSeconds() {
        return Util.getMeasuringTimeMs() / 1000.0;
    }

    public static int getRainbowTextColor(int timing) {
        double seconds = getCurrentSeconds();
        // HUE
        float hue = (float)(seconds * (((double)timing) / 1000) % 1.0f);
        float saturation = 1.f; // High saturation for vivid colors
        float value = 1.f;      // High value for bright colors
        // color
        int rgbColor = MathHelper.hsvToRgb(hue, saturation, value);
        // add alpha component with bit shifting
        return 0xFF000000 | rgbColor;
    }

    private static void renderWatermark(DrawContext context, RenderTickCounter tickCounter) {
        int backgroundColor = new Color(0, 0, 0, 125).getRGB(); // Transparent black


        //context.fill(5, 4, 65, 15, 0, backgroundColor);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, WATERMARK_TEXT, 35, 6, getRainbowTextColor(400));
    }

    private static void renderLabelEsp(DrawContext context, RenderTickCounter tickCounter) {

    }
}