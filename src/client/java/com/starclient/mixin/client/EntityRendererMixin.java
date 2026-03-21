package com.starclient.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starclient.EntityRenderStateDuck;
import com.starclient.StarNameTagColorRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method = "submitNameTag", at = @At("HEAD"), cancellable = true)
    private void modifyNameTag(S entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (entityRenderState.nameTag == null || !(((EntityRenderStateDuck) entityRenderState).star$isNametag())) return;

        Entity entity = ((EntityRenderStateDuck) entityRenderState).star$getEntity();
        LivingEntity livingEntity = entity instanceof LivingEntity le ? le : null;

        Component nameTag = buildNameTag(livingEntity, entityRenderState.nameTag);
        StarNameTagColorRegistry.register(nameTag, buildBackgroundColor(livingEntity), ((EntityRenderStateDuck) entityRenderState).star$getTexture());

        submitNodeCollector.submitNameTag(
                poseStack,
                entityRenderState.nameTagAttachment,
                0,
                nameTag,
                !entityRenderState.isDiscrete,
                entityRenderState.lightCoords,
                entityRenderState.distanceToCameraSq,
                cameraRenderState
        );
        ci.cancel();
    }

    @Unique
    private Component buildNameTag(LivingEntity living, Component original) {
        if (living == null) return original; // no need to reconstruct, just reuse

        String healthStr = String.format("%.1f❤ ", living.getHealth());
        ChatFormatting healthColor = getHealthColor(living);

        return Component.empty()
                .append(Component.literal(healthStr).withStyle(healthColor))
                .append(original);
    }

    @Unique
    private ChatFormatting getHealthColor(LivingEntity living) {
        float ratio = living.getHealth() / living.getMaxHealth();
        if (ratio > 0.5f) return ChatFormatting.GREEN;
        if (ratio > 0.25f) return ChatFormatting.YELLOW;
        return ChatFormatting.RED;
    }

    @Unique
    private int buildBackgroundColor(LivingEntity living) {
        if (living == null) return 0x20000000;
        return Color.BLACK.getRGB();
    }
}
