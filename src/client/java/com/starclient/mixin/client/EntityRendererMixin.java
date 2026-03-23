package com.starclient.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starclient.EntityRenderStateDuck;
import com.starclient.StarClientOptions;
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
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.starclient.helper.EntityTextureHelper;

import java.util.Objects;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Unique
    private static final int STAR$FULL_BRIGHT_LIGHT = 0x00F000F0;

    @Inject(method = "submitNameTag", at = @At("HEAD"), cancellable = true)
    private void modifyNameTag(S entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (entityRenderState == null || entityRenderState.nameTag == null)
            return;

        EntityRenderStateDuck duck = (EntityRenderStateDuck) entityRenderState;

        if (!duck.star$isNametag())
            return;

        Entity entity = duck.star$getEntity();
        LivingEntity livingEntity = entity instanceof LivingEntity le ? le : null;
        Identifier texture = EntityTextureHelper.resolveTexture(entity);
        StarNameTagColorRegistry.UvRect uvRect = EntityTextureHelper.resolveUv(entity, texture);
        if (entity instanceof ItemEntity itemEntity) {
            EntityTextureHelper.ItemIcon itemIcon = EntityTextureHelper
                    .resolveItemIcon(itemEntity);
            if (itemIcon != null) {
                texture = itemIcon.texture;
                uvRect = itemIcon.uvRect;
            }
        }
        float healthRatio = livingEntity != null && livingEntity.getMaxHealth() > 0f
                ? (livingEntity.getHealth() / livingEntity.getMaxHealth())
                : -1f;

        Component nameTag = buildNameTag(entity, entityRenderState.nameTag);
        if (texture != null && uvRect != null) {
            StarNameTagColorRegistry.register(nameTag, StarClientOptions.pendingNameTagBgColor, texture, uvRect,
                    healthRatio);
        } else {
            StarNameTagColorRegistry.register(nameTag, StarClientOptions.pendingNameTagBgColor, null,
                    StarNameTagColorRegistry.UvRect.playerFace(), healthRatio);
        }

        submitNodeCollector.submitNameTag(
                Objects.requireNonNull(poseStack),
                entityRenderState.nameTagAttachment,
                0,
                Objects.requireNonNull(nameTag),
                !entityRenderState.isDiscrete,
                STAR$FULL_BRIGHT_LIGHT,
                entityRenderState.distanceToCameraSq,
                Objects.requireNonNull(cameraRenderState));
        ci.cancel();
    }

    @Unique
    private Component buildNameTag(Entity entity, Component original) {
        if (entity instanceof ItemEntity itemEntity) {
            int count = itemEntity.getItem().getCount();
            if (count > 1) {
                return Component.empty()
                        .append(Component.literal(count + "x ").withStyle(ChatFormatting.GOLD))
                        .append(Objects.requireNonNull(original));
            }
            return original;
        }

        LivingEntity living = entity instanceof LivingEntity le ? le : null;
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
