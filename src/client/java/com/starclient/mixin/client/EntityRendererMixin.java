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
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.player.AbstractClientPlayer;
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
        if (entityRenderState.nameTag == null) return;

        EntityRenderStateDuck duck = (EntityRenderStateDuck) entityRenderState;

        if (!duck.star$isNametag()) return;

        Entity entity = duck.star$getEntity();
        LivingEntity livingEntity = entity instanceof LivingEntity le ? le : null;
        Identifier texture = resolveTexture(entity, duck);

        Component nameTag = buildNameTag(livingEntity, entityRenderState.nameTag);
        StarNameTagColorRegistry.register(nameTag, Color.BLACK.getRGB(), texture);

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
    private Identifier resolveTexture(Entity entity, EntityRenderStateDuck duck) {
        if (entity instanceof AbstractClientPlayer player) {
            return player.getSkin().body().texturePath();
        }
        return duck.star$getTexture();
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
}
