package com.starclient.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starclient.EntityRenderStateDuck;
import com.starclient.StarNameTagColorRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.AbstractClientPlayer;
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

import java.awt.*;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererNameTagMixin {
    @Unique
    private static final int STAR$FULL_BRIGHT_LIGHT = 0x00F000F0;

    @Inject(method = "submitNameTag", at = @At("HEAD"), cancellable = true)
    private void submitAvatarNameTag(AvatarRenderState avatarRenderState, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (avatarRenderState.nameTag == null)
            return;

        EntityRenderStateDuck duck = (EntityRenderStateDuck) avatarRenderState;
        if (!duck.star$isNametag())
            return;

        LivingEntity living = duck.star$getEntity() instanceof LivingEntity le ? le : null;
        Identifier texture = resolvePlayerTexture(duck, living);
        StarNameTagColorRegistry.UvRect uvRect = StarNameTagColorRegistry.UvRect.playerFace();
        float healthRatio = living != null && living.getMaxHealth() > 0f
                ? (living.getHealth() / living.getMaxHealth())
                : -1f;

        Component nameTag = buildNameTag(living, avatarRenderState.nameTag);
        if (texture != null) {
            StarNameTagColorRegistry.register(nameTag, Color.BLACK.getRGB(), texture, uvRect, healthRatio);
        } else {
            StarNameTagColorRegistry.register(nameTag, Color.BLACK.getRGB(), null, uvRect, healthRatio);
        }

        submitNodeCollector.submitNameTag(
                poseStack,
                avatarRenderState.nameTagAttachment,
                0,
                nameTag,
                !avatarRenderState.isDiscrete,
                STAR$FULL_BRIGHT_LIGHT,
                avatarRenderState.distanceToCameraSq,
                cameraRenderState);
        ci.cancel();
    }

    @Unique
    private Identifier resolvePlayerTexture(EntityRenderStateDuck duck, LivingEntity living) {
        if (living instanceof AbstractClientPlayer player) {
            return player.getSkin().body().texturePath();
        }
        return duck.star$getTexture();
    }

    @Unique
    private Component buildNameTag(LivingEntity living, Component original) {
        if (living == null)
            return original;

        String healthStr = String.format("%.1f❤ ", living.getHealth());
        ChatFormatting healthColor = getHealthColor(living);

        return Component.empty()
                .append(Component.literal(healthStr).withStyle(healthColor))
                .append(original);
    }

    @Unique
    private ChatFormatting getHealthColor(LivingEntity living) {
        float ratio = living.getHealth() / living.getMaxHealth();
        if (ratio > 0.5f)
            return ChatFormatting.GREEN;
        if (ratio > 0.25f)
            return ChatFormatting.YELLOW;
        return ChatFormatting.RED;
    }
}
