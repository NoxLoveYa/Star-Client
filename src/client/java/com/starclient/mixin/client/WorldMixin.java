package com.starclient.mixin.client;

import com.starclient.utils.PlayerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext; // Important for drawing 2D GUI elements
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity; // For player entities
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f; // For JOML Matrix4f
import org.joml.Vector3f; // For JOML Vector3f (for projection)
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldMixin {

    private static final float HEAD_OFFSET_Y = 0.5F; // Approx offset from entity origin to head top
    private static final float RENDER_ICON_SIZE_SCREEN_PX = 32.0F; // Size of the head icon on screen

    @Inject(method = "render", at = @At("TAIL"))
    private void starclient_renderEntityHeadOverlay(
            ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci
            // In newer versions, this is often the ProjectionMatrix
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return;
        }

        // DrawContext must be created with the main client buffer builders
        // The one in the original WorldRenderer snippet was .getEntityVertexConsumers(), which might be for 3D.
        // For general 2D GUI overlays, using the main client.getBufferBuilders() is more common.
        // If you still have black squares, experiment with getOutlineVertexConsumers() or getEntityVertexConsumers()
        DrawContext context = new DrawContext(client, client.getBufferBuilders().getEntityVertexConsumers());
        // Alternatively, if you want a simpler DrawContext that directly draws to the framebuffer:
        // DrawContext context = new DrawContext(client, client.getBufferBuilders().get, new MatrixStack()); // No, this is not how it works in 1.20+
        // The above DrawContext(client, VCP) is correct for drawing into buffers.

        Vec3d cameraPos = camera.getPos();

        // Iterate through all entities in the world
        for (Entity entity : client.world.getEntities()) {
            // Only render for players for simplicity and consistent head UVs
            if (!(entity instanceof PlayerEntity player) || player.isRemoved()) {
                continue;
            }

            // Skip rendering for the local player to avoid drawing on self
            if (player.equals(client.player)) {
                continue;
            }

            // --- 1. Calculate 3D Head Position (Interpolated) ---
            double entityX = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * tickCounter.getTickDelta(true);
            double entityY = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * tickCounter.getTickDelta(true);
            double entityZ = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * tickCounter.getTickDelta(true);;

            // Adjust for head position relative to entity origin
            // getEyeHeight gives the Y position of the eyes. We might want a bit above it.
            double headY = entityY + player.getEyeHeight(player.getPose()) + 0.0f;

            // --- 2. Project 3D World Coordinates to 2D Screen Coordinates ---
            // Create a 3D vector for the head position RELATIVE TO THE CAMERA
            Vector3f worldPosRelativeToCamera = new Vector3f(
                    (float)(entityX - cameraPos.x),
                    (float)(headY - cameraPos.y),
                    (float)(entityZ - cameraPos.z)
            );

            Matrix4f combinedMatrix = new Matrix4f(projectionMatrix);
            combinedMatrix.mul(positionMatrix);

            int windowWidth = client.getWindow().getFramebufferWidth();
            int windowHeight = client.getWindow().getFramebufferHeight();
            int[] viewport = new int[]{0, 0, windowWidth, windowHeight}; // [x, y, width, height]
            Vector3f projectedScreenPos = new Vector3f();
            projectedScreenPos = combinedMatrix.project(
                    worldPosRelativeToCamera.x, worldPosRelativeToCamera.y, worldPosRelativeToCamera.z,
                    viewport,
                    projectedScreenPos
            );

            // Check if projection was successful and if the entity is in front of the camera (z between 0 and 1)
            if (projectedScreenPos.z < 0.0 || projectedScreenPos.z > 1.0) {
                continue; // Entity is off-screen, behind camera, or too far
            }

            // Convert projected X, Y to pixel coordinates (they should already be in pixel range 0-width/height)
            int screenX = (int) projectedScreenPos.x;
            int screenY = (int) projectedScreenPos.y;

            // --- 3. Draw 2D Head Icon ---
            Identifier playerSkinTextureIdentifier = PlayerUtils.getLocalPlayerSkinIdentifier().get().texture();

            // Head icon drawing parameters
            int iconWidth = (int) RENDER_ICON_SIZE_SCREEN_PX;
            int iconHeight = (int) RENDER_ICON_SIZE_SCREEN_PX;

            // UV coordinates for the player's head on a standard 64x64 skin texture
            int textureU = 8;
            int textureV = 8;
            int regionWidth = 8;
            int regionHeight = 8;
            int textureTotalWidth = 64;
            int textureTotalHeight = 64;

            // Adjust screenX, screenY so the icon is centered on the projected point
            screenX -= iconWidth / 2;
            screenY -= iconHeight / 2;

            // Draw the texture part using the DrawContext
            context.drawTexture(
                    RenderLayer::getGuiTextured,
                    playerSkinTextureIdentifier,
                    screenX, screenY,
                    iconWidth, iconHeight,
                    textureU, textureV,
                    regionWidth, regionHeight,
                    textureTotalWidth, textureTotalHeight
            );
        }
        // It's good practice to close the DrawContext's buffer after all drawing is done for the frame
        // This is typically handled by Minecraft's rendering loop.
        // If you were manually flushing a buffer, you'd do it here.
    }
}
