package com.starclient.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starclient.StarClientOptions;
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
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

@Mixin(NameTagFeatureRenderer.class)
public class NameTagIconRendererMixin {
    @Unique
    private static final int STAR$FULL_BRIGHT_LIGHT = 0x00F000F0;
    @Unique
    private static final int STAR$LOW_HEALTH_BAR_COLOR = new Color(220, 60, 60, 220).getRGB();
    @Unique
    private static final int STAR$HIGH_HEALTH_BAR_COLOR = new Color(40, 200, 70, 220).getRGB();

    @Inject(method = "render", at = @At("HEAD"))
    private void renderIcons(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource,
            Font font, CallbackInfo ci) {
        NameTagFeatureRenderer.Storage storage = submitNodeCollection.getNameTagSubmits();

        NameTagStorageAccessor accessor = (NameTagStorageAccessor) storage;
        Set<SubmitNodeStorage.NameTagSubmit> renderedSubmits = Collections.newSetFromMap(new IdentityHashMap<>());
        for (SubmitNodeStorage.NameTagSubmit submit : accessor.star$getNameTagSubmitsSeethrough()) {
            if (renderedSubmits.add(submit)) {
                renderIcon(submit, bufferSource, font, true);
            }
        }
        for (SubmitNodeStorage.NameTagSubmit submit : accessor.star$getNameTagSubmitsNormal()) {
            if (renderedSubmits.add(submit)) {
                renderIcon(submit, bufferSource, font, false);
            }
        }

        bufferSource.endBatch();
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
                    Objects.requireNonNull(bufferSource),
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    STAR$FULL_BRIGHT_LIGHT);
        }
        bufferSource.endBatch();
    }

    @Unique
    private void renderIcon(SubmitNodeStorage.NameTagSubmit submit, MultiBufferSource.BufferSource bufferSource,
            Font font, boolean seeThrough) {
        float healthRatio = StarNameTagColorRegistry.getHealthRatio(submit.text());
        int size = font.lineHeight;
        int iconSize = size - 1;

        if (healthRatio >= 0f) {
            renderHealthBar(submit, bufferSource, font, seeThrough, healthRatio);
        }

        Identifier texture = StarNameTagColorRegistry.getHeadTexture(submit.text());
        if (texture == null)
            return;
        StarNameTagColorRegistry.UvRect uvRect = StarNameTagColorRegistry.getHeadUv(submit.text());

        float x = submit.x() - iconSize - 2.5f;
        float y = submit.y();
        float bgZ = -0.01f;

        int bgColor = StarClientOptions.pendingNameTagBgColor;
        if (bgColor != -1) {
            RenderType bgRenderType = seeThrough ? RenderTypes.textBackgroundSeeThrough()
                    : RenderTypes.textBackground();
            VertexConsumer bgConsumer = bufferSource.getBuffer(bgRenderType);
            Matrix4f bgPose = submit.pose();
            bgConsumer.addVertex(bgPose, x - 1, y - 1, bgZ).setColor(bgColor).setLight(STAR$FULL_BRIGHT_LIGHT);
            bgConsumer.addVertex(bgPose, x - 1, y + iconSize + 1, bgZ).setColor(bgColor)
                    .setLight(STAR$FULL_BRIGHT_LIGHT);
            bgConsumer.addVertex(bgPose, x + iconSize + 1, y + iconSize + 1, bgZ).setColor(bgColor)
                    .setLight(STAR$FULL_BRIGHT_LIGHT);
            bgConsumer.addVertex(bgPose, x + iconSize + 1, y - 1, bgZ).setColor(bgColor)
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
        consumer.addVertex(pose, x, y + iconSize, 0f).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 0, 1).setColor(-1).setLight(STAR$FULL_BRIGHT_LIGHT);
        consumer.addVertex(pose, x + iconSize, y + iconSize, 0f).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 0, 1).setColor(-1).setLight(STAR$FULL_BRIGHT_LIGHT);
        consumer.addVertex(pose, x + iconSize, y, 0f).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 0, 1).setColor(-1).setLight(STAR$FULL_BRIGHT_LIGHT);
    }

    @Unique
    private void renderHealthBar(SubmitNodeStorage.NameTagSubmit submit, MultiBufferSource.BufferSource bufferSource,
            Font font, boolean seeThrough, float healthRatio) {
        int iconSize = font.lineHeight - 1;
        float textLeft = submit.x();
        float textWidth = font.width(submit.text());
        float barLeft = textLeft - iconSize - 3.5f;
        float barRight = textLeft + textWidth;

        float barTop = submit.y() + iconSize + 0.9f;
        float barBottom = barTop + 1.25f;
        float fillZ = -0.0095f;

        RenderType barRenderType = seeThrough ? RenderTypes.textBackgroundSeeThrough() : RenderTypes.textBackground();
        VertexConsumer barConsumer = bufferSource.getBuffer(barRenderType);
        Matrix4f pose = submit.pose();

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

        int fillColor = star$lerpColor(STAR$LOW_HEALTH_BAR_COLOR, STAR$HIGH_HEALTH_BAR_COLOR, clampedRatio);

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

    @Unique
    private int star$lerpColor(int startColor, int endColor, float t) {
        float clamped = Math.max(0f, Math.min(1f, t));

        int sa = (startColor >>> 24) & 0xFF;
        int sr = (startColor >>> 16) & 0xFF;
        int sg = (startColor >>> 8) & 0xFF;
        int sb = startColor & 0xFF;

        int ea = (endColor >>> 24) & 0xFF;
        int er = (endColor >>> 16) & 0xFF;
        int eg = (endColor >>> 8) & 0xFF;
        int eb = endColor & 0xFF;

        int a = Math.round(sa + ((ea - sa) * clamped));
        int r = Math.round(sr + ((er - sr) * clamped));
        int g = Math.round(sg + ((eg - sg) * clamped));
        int b = Math.round(sb + ((eb - sb) * clamped));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
