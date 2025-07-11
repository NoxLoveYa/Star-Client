package com.starclient.mixin.client;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.*;

@Mixin(WorldRenderer.class)
public class BlockXray {
    @Redirect(method = "drawBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexRendering;drawOutline(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/util/shape/VoxelShape;DDDI)V"))
    private void blockXray(MatrixStack matrices, VertexConsumer vertexConsumers, VoxelShape shape, double offsetX, double offsetY, double offsetZ, int color) {
        MatrixStack.Entry entry = matrices.peek();
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            Vector3f vector3f = new Vector3f((float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1)).normalize();

            // Time-based hue rotation for full cycle
            float time = (System.currentTimeMillis() % 10000L) / 5000.0f; // 5s full rotation

            // Pastel color: low saturation (e.g. 0.4) and high brightness (e.g. 1.0)
            float hue = time % 1.0f;
            float saturation = 0.4f;
            float brightness = 1.0f;

            int rgb = Color.HSBtoRGB(hue, saturation, brightness);

            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            // First vertex with pastel color
            vertexConsumers.vertex(entry, (float)(x1 + offsetX), (float)(y1 + offsetY), (float)(z1 + offsetZ))
                    .color(r, g, b, 255)
                    .normal(entry, vector3f);

            // Second vertex with same pastel color
            vertexConsumers.vertex(entry, (float)(x2 + offsetX), (float)(y2 + offsetY), (float)(z2 + offsetZ))
                    .color(r, g, b, 255)
                    .normal(entry, vector3f);
        });
    }
}
