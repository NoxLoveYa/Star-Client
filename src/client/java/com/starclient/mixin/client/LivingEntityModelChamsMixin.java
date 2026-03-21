package com.starclient.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starclient.EntityRenderStateDuck;
import com.starclient.StarClientOptions;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.Color;
import java.util.Objects;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityModelChamsMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends EntityRenderer<T, S> {
    private static final int STAR$FULL_BRIGHT_LIGHT = 0x00F000F0;

    protected LivingEntityModelChamsMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Shadow
    public abstract M getModel();

    @Shadow
    public abstract Identifier getTextureLocation(S renderState);

    @Shadow
    protected abstract RenderType getRenderType(S renderState, boolean bodyVisible, boolean translucent,
            boolean glowing);

    @Shadow
    protected abstract boolean shouldRenderLayers(S renderState);

    @Redirect(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    private void star$redirectModelSubmit(
            SubmitNodeCollector submitNodeCollector,
            net.minecraft.client.model.Model<?> model,
            Object modelState,
            PoseStack poseStack,
            RenderType renderType,
            int light,
            int overlay,
            int color,
            TextureAtlasSprite sprite,
            int outlineColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (!(modelState instanceof LivingEntityRenderState entityRenderState)) {
            star$submitModelTyped(
                    model,
                    modelState,
                    submitNodeCollector,
                    poseStack,
                    renderType,
                    light,
                    overlay,
                    color,
                    sprite,
                    outlineColor,
                    crumblingOverlay);
            return;
        }

        EntityRenderStateDuck duck = (EntityRenderStateDuck) entityRenderState;
        Entity entity = duck.star$getEntity();

        if (entity != null
                && entity.isAlive()
                && shouldRenderChamsForEntity(entity, entityRenderState.distanceToCameraSq)) {
            float alpha = Math.max(0.05f, Math.min(1.0f, StarClientOptions.mobChamsAlpha));
            int tint = colorTintForEntity(entity, alpha);
            @SuppressWarnings("unchecked")
            S typedRenderState = (S) entityRenderState;
            Identifier texture = Objects.requireNonNull(this.getTextureLocation(typedRenderState));
            RenderType chamsRenderType = this.getRenderType(typedRenderState, true, true, true);
            if (chamsRenderType == null) {
                chamsRenderType = RenderTypes.entityTranslucent(texture);
            }

            star$submitModelTyped(
                    model,
                    modelState,
                    submitNodeCollector,
                    poseStack,
                    chamsRenderType,
                    STAR$FULL_BRIGHT_LIGHT,
                    overlay,
                    tint,
                    sprite,
                    tint,
                    crumblingOverlay);
            return;
        }

        star$submitModelTyped(
                model,
                modelState,
                submitNodeCollector,
                poseStack,
                renderType,
                light,
                overlay,
                color,
                sprite,
                outlineColor,
                crumblingOverlay);
    }

    @Redirect(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;shouldRenderLayers(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)Z"))
    private boolean star$redirectShouldRenderLayers(LivingEntityRenderer<?, ?, ?> instance,
            LivingEntityRenderState renderState) {
        EntityRenderStateDuck duck = (EntityRenderStateDuck) renderState;
        Entity entity = duck.star$getEntity();
        if (entity != null && shouldRenderChamsForEntity(entity, renderState.distanceToCameraSq)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        S typedState = (S) renderState;
        return this.shouldRenderLayers(typedState);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void star$submitModelTyped(
            net.minecraft.client.model.Model<?> model,
            Object modelState,
            SubmitNodeCollector submitNodeCollector,
            PoseStack poseStack,
            RenderType renderType,
            int light,
            int overlay,
            int color,
            TextureAtlasSprite sprite,
            int outlineColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        net.minecraft.client.model.Model<?> nonNullModel = Objects.requireNonNull(model);
        PoseStack nonNullPoseStack = Objects.requireNonNull(poseStack);
        RenderType nonNullRenderType = Objects.requireNonNull(renderType);

        submitNodeCollector.submitModel(
                (net.minecraft.client.model.Model) nonNullModel,
                modelState,
                nonNullPoseStack,
                nonNullRenderType,
                light,
                overlay,
                color,
                sprite,
                outlineColor,
                crumblingOverlay);
    }

    private static boolean shouldRenderChamsForEntity(@NonNull Entity entity, double distSq) {
        boolean isPlayer = entity.getType() == EntityType.PLAYER || entity instanceof Avatar;
        boolean isHostile = entity instanceof Enemy;
        boolean isItem = entity.getType() == EntityType.ITEM;
        boolean isNonHostile = !isItem && !isHostile && !isPlayer && entity instanceof LivingEntity;

        if (!isHostile && !isPlayer && !isNonHostile) {
            return false;
        }

        if (isPlayer) {
            return StarClientOptions.chamsPlayer && distSq < StarClientOptions.chamsDistancePlayer;
        }
        if (isHostile) {
            return StarClientOptions.chamsHostile && distSq < StarClientOptions.chamsDistanceHostile;
        }
        return StarClientOptions.chamsMob && distSq < StarClientOptions.chamsDistanceMob;
    }

    private static int colorTintForEntity(@NonNull Entity entity, float alpha) {
        float hue = hueForEntity(entity);
        int rgb = Color.HSBtoRGB(Math.max(0.0f, Math.min(1.0f, hue)), 0.85f, 0.95f) & 0x00FFFFFF;
        int alphaByte = (int) Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f);
        return (alphaByte << 24) | rgb;
    }

    private static float hueForEntity(@NonNull Entity entity) {
        boolean isPlayer = entity.getType() == EntityType.PLAYER || entity instanceof Avatar;
        boolean isHostile = entity instanceof Enemy;
        if (isPlayer) {
            return StarClientOptions.chamsHuePlayer;
        }
        if (isHostile) {
            return StarClientOptions.chamsHueHostile;
        }
        return StarClientOptions.chamsHueMob;
    }
}
