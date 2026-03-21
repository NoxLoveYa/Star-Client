package com.starclient.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starclient.StarNameTagColorRegistry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@Mixin(NameTagFeatureRenderer.class)
public class NameTagHeadIconRendererMixin {
    @Unique
    private static final int STAR$FULL_BRIGHT_LIGHT = 0x00F000F0;

    @Inject(method = "render", at = @At("HEAD"))
    private void renderHeadIcons(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource,
            Font font, CallbackInfo ci) {
        NameTagFeatureRenderer.Storage storage = submitNodeCollection.getNameTagSubmits();

        NameTagStorageAccessor accessor = (NameTagStorageAccessor) storage;
        Set<String> renderedKeys = new HashSet<>();
        for (SubmitNodeStorage.NameTagSubmit submit : accessor.star$getNameTagSubmitsSeethrough()) {
            if (renderedKeys.add(buildSubmitKey(submit))) {
                renderHead(submit, bufferSource, font, true);
            }
        }
        for (SubmitNodeStorage.NameTagSubmit submit : accessor.star$getNameTagSubmitsNormal()) {
            if (renderedKeys.add(buildSubmitKey(submit))) {
                renderHead(submit, bufferSource, font, false);
            }
        }

        bufferSource.endBatch(); // flush before text renders
        StarNameTagColorRegistry.clearAll();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderBrightThroughWallText(SubmitNodeCollection submitNodeCollection,
            MultiBufferSource.BufferSource bufferSource, Font font, CallbackInfo ci) {
        NameTagFeatureRenderer.Storage storage = submitNodeCollection.getNameTagSubmits();
        NameTagStorageAccessor accessor = (NameTagStorageAccessor) storage;

        for (SubmitNodeStorage.NameTagSubmit submit : accessor.star$getNameTagSubmitsSeethrough()) {
            font.drawInBatch(
                    submit.text(),
                    submit.x(),
                    submit.y(),
                    -1,
                    false,
                    submit.pose(),
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    STAR$FULL_BRIGHT_LIGHT);
        }
        bufferSource.endBatch();
    }

    @Unique
    private String buildSubmitKey(SubmitNodeStorage.NameTagSubmit submit) {
        return submit.text().getString()
                + "|" + Float.floatToIntBits(submit.x())
                + "|" + Float.floatToIntBits(submit.y());
    }

    @Unique
    private void renderHead(SubmitNodeStorage.NameTagSubmit submit, MultiBufferSource.BufferSource bufferSource,
            Font font, boolean seeThrough) {
        float healthRatio = StarNameTagColorRegistry.getHealthRatio(submit.text());

        Identifier texture = StarNameTagColorRegistry.getHeadTexture(submit.text());
        if (texture == null)
            return;
        StarNameTagColorRegistry.UvRect uvRect = StarNameTagColorRegistry.getHeadUv(submit.text());

        int size = font.lineHeight;
        int headSize = size - 1;
        float x = submit.x() - headSize - 3;
        float y = submit.y(); // adjust this
        float bgZ = -0.01f;

        int bgColor = StarNameTagColorRegistry.get(submit.text());
        if (bgColor != -1) {
            RenderType bgRenderType = seeThrough ? RenderTypes.textBackgroundSeeThrough()
                    : RenderTypes.textBackground();
            VertexConsumer bgConsumer = bufferSource.getBuffer(bgRenderType);
            Matrix4f bgPose = submit.pose();
            bgConsumer.addVertex(bgPose, x - 1, y - 1, bgZ).setColor(bgColor).setLight(STAR$FULL_BRIGHT_LIGHT);
            bgConsumer.addVertex(bgPose, x - 1, y + headSize + 1, bgZ).setColor(bgColor)
                    .setLight(STAR$FULL_BRIGHT_LIGHT);
            bgConsumer.addVertex(bgPose, x + headSize + 1, y + headSize + 1, bgZ).setColor(bgColor)
                    .setLight(STAR$FULL_BRIGHT_LIGHT);
            bgConsumer.addVertex(bgPose, x + headSize + 1, y - 1, bgZ).setColor(bgColor)
                    .setLight(STAR$FULL_BRIGHT_LIGHT);
            bufferSource.endBatch(bgRenderType);
        }

        float u0 = uvRect.u0(), u1 = uvRect.u1();
        float v0 = uvRect.v0(), v1 = uvRect.v1();

        RenderType renderType = seeThrough ? RenderTypes.textSeeThrough(texture)
                : RenderTypes.entityCutoutNoCull(texture);

        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        Matrix4f pose = submit.pose();

        consumer.addVertex(pose, x, y, 0f).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 1)
                .setColor(-1).setLight(STAR$FULL_BRIGHT_LIGHT);
        consumer.addVertex(pose, x, y + headSize, 0f).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 0, 1).setColor(-1).setLight(STAR$FULL_BRIGHT_LIGHT);
        consumer.addVertex(pose, x + headSize, y + headSize, 0f).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 0, 1).setColor(-1).setLight(STAR$FULL_BRIGHT_LIGHT);
        consumer.addVertex(pose, x + headSize, y, 0f).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 0, 1).setColor(-1).setLight(STAR$FULL_BRIGHT_LIGHT);

        if (healthRatio >= 0f) {
            renderHealthBar(submit, bufferSource, font, seeThrough, healthRatio);
        }
    }

    @Unique
    private void renderHealthBar(SubmitNodeStorage.NameTagSubmit submit, MultiBufferSource.BufferSource bufferSource,
            Font font, boolean seeThrough, float healthRatio) {
        int headSize = font.lineHeight - 1;
        float textLeft = submit.x();
        float textWidth = font.width(submit.text());
        float barLeft = textLeft - headSize - 4;
        float barRight = textLeft + textWidth;

        float barTop = submit.y() + headSize + 0.9f;
        float barBottom = barTop + 1.25f;
        float barZ = -0.01f;
        float fillZ = -0.0095f;

        RenderType barRenderType = seeThrough ? RenderTypes.textBackgroundSeeThrough() : RenderTypes.textBackground();
        VertexConsumer barConsumer = bufferSource.getBuffer(barRenderType);
        Matrix4f pose = submit.pose();

        int bgColor = new Color(0, 0, 0, 210).getRGB();
        barConsumer.addVertex(pose, barLeft, barTop, barZ).setColor(bgColor).setLight(STAR$FULL_BRIGHT_LIGHT);
        barConsumer.addVertex(pose, barLeft, barBottom, barZ).setColor(bgColor).setLight(STAR$FULL_BRIGHT_LIGHT);
        barConsumer.addVertex(pose, barRight, barBottom, barZ).setColor(bgColor).setLight(STAR$FULL_BRIGHT_LIGHT);
        barConsumer.addVertex(pose, barRight, barTop, barZ).setColor(bgColor).setLight(STAR$FULL_BRIGHT_LIGHT);

        float clampedRatio = Math.max(0f, Math.min(1f, healthRatio));
        float fillPad = 0.1f;
        float fillLeft = barLeft + fillPad;
        float fillTop = barTop + fillPad;
        float fillBottom = barBottom - fillPad;
        float availableWidth = Math.max(0f, (barRight - barLeft) - (fillPad * 2f));
        float fillWidth = availableWidth * clampedRatio;
        if (clampedRatio > 0f) {
            fillWidth = Math.max(fillWidth, 0.75f);
        }
        float fillRight = Math.min(fillLeft + fillWidth, barRight - fillPad);

        int fillColor = clampedRatio > 0.5f
                ? new Color(40, 200, 70, 220).getRGB()
                : (clampedRatio > 0.25f ? new Color(230, 200, 40, 220).getRGB() : new Color(220, 60, 60, 220).getRGB());

        if (fillRight > fillLeft) {
            barConsumer.addVertex(pose, fillLeft, fillTop, fillZ).setColor(fillColor).setLight(STAR$FULL_BRIGHT_LIGHT);
            barConsumer.addVertex(pose, fillLeft, fillBottom, fillZ).setColor(fillColor)
                    .setLight(STAR$FULL_BRIGHT_LIGHT);
            barConsumer.addVertex(pose, fillRight, fillBottom, fillZ).setColor(fillColor)
                    .setLight(STAR$FULL_BRIGHT_LIGHT);
            barConsumer.addVertex(pose, fillRight, fillTop, fillZ).setColor(fillColor).setLight(STAR$FULL_BRIGHT_LIGHT);
        }

        bufferSource.endBatch(barRenderType);

    }
}
