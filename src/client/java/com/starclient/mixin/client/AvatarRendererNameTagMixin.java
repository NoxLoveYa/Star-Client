package com.starclient.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starclient.EntityRenderStateDuck;
import com.starclient.StarClientOptions;
import com.starclient.StarNameTagColorRegistry;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.starclient.helper.EntityTextureHelper;

import java.util.Objects;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererNameTagMixin {
    @Unique
    private static final int STAR$FULL_BRIGHT_LIGHT = 0x00F000F0;

    @Inject(method = "submitNameTag", at = @At("HEAD"), cancellable = true)
    private void submitAvatarNameTag(AvatarRenderState avatarRenderState, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (avatarRenderState == null || avatarRenderState.nameTag == null)
            return;

        EntityRenderStateDuck duck = (EntityRenderStateDuck) avatarRenderState;
        if (!duck.star$isNametag())
            return;

        LivingEntity living = duck.star$getEntity() instanceof LivingEntity le ? le : null;
        Identifier texture = EntityTextureHelper.resolveTexture(duck.star$getEntity());
        StarNameTagColorRegistry.UvRect uvRect = StarNameTagColorRegistry.UvRect.playerFace();
        float healthRatio = living != null && living.getMaxHealth() > 0f
                ? (living.getHealth() / living.getMaxHealth())
                : -1f;

        Component nameTag = buildNameTag(living, avatarRenderState.nameTag);
        if (texture != null) {
            StarNameTagColorRegistry.register(nameTag, StarClientOptions.pendingNameTagBgColor, texture, uvRect,
                    healthRatio);
        } else {
            StarNameTagColorRegistry.register(nameTag, StarClientOptions.pendingNameTagBgColor, null, uvRect,
                    healthRatio);
        }

        submitNodeCollector.submitNameTag(
                Objects.requireNonNull(poseStack),
                avatarRenderState.nameTagAttachment,
                0,
                Objects.requireNonNull(nameTag),
                !avatarRenderState.isDiscrete,
                STAR$FULL_BRIGHT_LIGHT,
                avatarRenderState.distanceToCameraSq,
                Objects.requireNonNull(cameraRenderState));
        ci.cancel();
    }

    @Unique
    private Component buildNameTag(LivingEntity living, Component original) {
        if (living == null)
            return original;

        String healthStr = String.format("%.1f❤ ", living.getHealth());
        int healthColor = getHealthColorRgb(living);

        return Component.empty()
                .append(Component.literal(Objects.requireNonNull(healthStr))
                        .withStyle(style -> style.withColor(healthColor)))
                .append(Objects.requireNonNull(original));
    }

    @Unique
    private int getHealthColorRgb(LivingEntity living) {
        float maxHealth = living.getMaxHealth();
        if (maxHealth <= 0f) {
            return 0xFFDC3C3C;
        }

        float ratio = Math.max(0f, Math.min(1f, living.getHealth() / maxHealth));

        int startR = 220;
        int startG = 60;
        int startB = 60;
        int endR = 40;
        int endG = 200;
        int endB = 70;

        int r = Math.round(startR + ((endR - startR) * ratio));
        int g = Math.round(startG + ((endG - startG) * ratio));
        int b = Math.round(startB + ((endB - startB) * ratio));
        return (r << 16) | (g << 8) | b;
    }
}
