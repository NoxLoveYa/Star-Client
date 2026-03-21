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

@Mixin(NameTagFeatureRenderer.class)
public class NameTagHeadIconRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void renderHeadIcons(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, Font font, CallbackInfo ci) {
        NameTagFeatureRenderer.Storage storage = submitNodeCollection.getNameTagSubmits();

        NameTagStorageAccessor accessor = (NameTagStorageAccessor) storage;
        for (SubmitNodeStorage.NameTagSubmit submit : accessor.star$getNameTagSubmitsSeethrough()) {
            renderHead(submit, bufferSource, font, true);
        }
        for (SubmitNodeStorage.NameTagSubmit submit : accessor.star$getNameTagSubmitsNormal()) {
            renderHead(submit, bufferSource, font, false);
        }

        bufferSource.endBatch(); // flush before text renders
    }

    @Unique
    private void renderHead(SubmitNodeStorage.NameTagSubmit submit, MultiBufferSource.BufferSource bufferSource, Font font, boolean seeThrough) {
        Identifier texture = StarNameTagColorRegistry.getHeadTexture(submit.text());
        if (texture == null) return;

        int size = font.lineHeight;
        int headSize = size - 1;
        float x = submit.x() - headSize - 3;
        float y = submit.y(); // adjust this

        // background
        int bgColor = StarNameTagColorRegistry.get(submit.text());
        if (bgColor != -1) {
            VertexConsumer bgConsumer = bufferSource.getBuffer(seeThrough ? RenderTypes.textBackgroundSeeThrough() : RenderTypes.textBackground());
            Matrix4f pose = submit.pose();
            bgConsumer.addVertex(pose, x - 1,        y - 1,        0f).setColor(bgColor).setLight(submit.lightCoords());
            bgConsumer.addVertex(pose, x - 1,        y + headSize + 1, 0f).setColor(bgColor).setLight(submit.lightCoords());
            bgConsumer.addVertex(pose, x + headSize + 1, y + headSize + 1, 0f).setColor(bgColor).setLight(submit.lightCoords());
            bgConsumer.addVertex(pose, x + headSize + 1, y - 1,        0f).setColor(bgColor).setLight(submit.lightCoords());
        }

        // player skin face UV: pixels 8-16 on a 64x64 texture
        float u0 = 8f / 64f, u1 = 16f / 64f;
        float v0 = 8f / 64f, v1 = 16f / 64f;

        RenderType renderType = RenderTypes.blockScreenEffect(texture);

        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        Matrix4f pose = submit.pose();

        consumer.addVertex(pose, x,        y,        0f).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 1).setColor(-1).setLight(submit.lightCoords());
        consumer.addVertex(pose, x,        y + headSize, 0f).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 1).setColor(-1).setLight(submit.lightCoords());
        consumer.addVertex(pose, x + headSize, y + headSize, 0f).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 1).setColor(-1).setLight(submit.lightCoords());
        consumer.addVertex(pose, x + headSize, y,        0f).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 1).setColor(-1).setLight(submit.lightCoords());
    }
}
