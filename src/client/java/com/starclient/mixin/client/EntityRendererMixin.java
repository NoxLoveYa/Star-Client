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
import net.minecraft.world.entity.EntityType;
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
        StarNameTagColorRegistry.UvRect uvRect = resolveUv(entity);

        Component nameTag = buildNameTag(livingEntity, entityRenderState.nameTag);
        if (texture != null && uvRect != null) {
            StarNameTagColorRegistry.register(nameTag, Color.BLACK.getRGB(), texture, uvRect);
        } else {
            StarNameTagColorRegistry.register(nameTag, Color.BLACK.getRGB(), null);
        }

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
    private StarNameTagColorRegistry.UvRect resolveUv(Entity entity) {
        if (entity instanceof AbstractClientPlayer) {
            return StarNameTagColorRegistry.UvRect.playerFace();
        }
        EntityType<?> type = entity.getType();

        if (type == EntityType.ZOMBIE || type == EntityType.HUSK || type == EntityType.DROWNED || type == EntityType.ZOMBIE_VILLAGER
                || type == EntityType.PLAYER || type == EntityType.PIGLIN || type == EntityType.PIGLIN_BRUTE || type == EntityType.ZOMBIFIED_PIGLIN) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 8f, 8f, 16f, 16f);
        }

        if (type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.WITHER_SKELETON || type == EntityType.CREEPER || type == EntityType.ENDERMAN) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 8f, 8f, 16f, 16f);
        }

        if (type == EntityType.COW || type == EntityType.MOOSHROOM) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 6f, 6f, 14f, 14f);
        }

        if (type == EntityType.SHEEP) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 8f, 8f, 14f, 14f);
        }

        if (type == EntityType.PIG) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 8f, 8f, 16f, 16f);
        }

        if (type == EntityType.CHICKEN) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 3f, 3f, 7f, 9f);
        }

        if (type == EntityType.VILLAGER || type == EntityType.WANDERING_TRADER) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 8f, 8f, 16f, 18f);
        }

        return null;
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
