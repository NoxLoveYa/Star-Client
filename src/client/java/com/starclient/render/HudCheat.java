package com.starclient.render;

import com.starclient.utils.CheatOptions;
import com.starclient.StarClient;
import com.starclient.utils.ColorUtils;
import com.starclient.utils.KeybindsHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class HudCheat {
    private static final Identifier EXAMPLE_LAYER = Identifier.of(StarClient.MOD_ID, "watermark");
    private static final String WATERMARK_TEXT = "Star-Client";

    public static void initialize() {
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.MISC_OVERLAYS, EXAMPLE_LAYER, HudCheat::render));

        KeybindsHandler.registerAction(GLFW.GLFW_KEY_RIGHT_SHIFT, "Open Star Client", () -> {
            MinecraftClient.getInstance().setScreen(new com.starclient.gui.screen.CheatOptions(MinecraftClient.getInstance().currentScreen));
        });
        KeybindsHandler.registerAction(GLFW.GLFW_KEY_DELETE, "Open Star Client From Del", () -> {
            MinecraftClient.getInstance().setScreen(new com.starclient.gui.screen.CheatOptions(MinecraftClient.getInstance().currentScreen));
        });

        StarClient.LOGGER.info("Registered Star Client Overlay");
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        if (CheatOptions.WatermarkEnabled) { renderWatermark(context, tickCounter); }
        renderLabelEsp(context, tickCounter);
    }





    private static void renderWatermark(DrawContext context, RenderTickCounter tickCounter) {
        int backgroundColor = new Color(0, 0, 0, 125).getRGB(); // Transparent black
        ColorUtils textColor = new ColorUtils(0);


        //context.fill(5, 4, 65, 15, 0, backgroundColor);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, WATERMARK_TEXT, 35, 6, textColor.getRainbowTextColor(40));
    }

    private static void renderLabelEsp(DrawContext context, RenderTickCounter tickCounter) {

    }
}