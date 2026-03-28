package com.starclient.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starclient.EntityRenderStateDuck;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRenderStateMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends EntityRenderer<T, S> {

    protected LivingEntityRenderStateMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Shadow
    protected abstract Identifier getTextureLocation(S renderState);

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"))
    private void captureTexture(S entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (entityRenderState == null) {
            return;
        }

        EntityRenderStateDuck duck = (EntityRenderStateDuck) entityRenderState;
        Entity entity = duck.star$getEntity();
        if (entity == null) {
            return;
        }

        Identifier texture;
        if (entity instanceof AbstractClientPlayer player) {
            texture = player.getSkin().body().texturePath();
        } else {
            texture = this.getTextureLocation(entityRenderState);
        }
        // Set texture on the entity's duck
        com.starclient.EntityDuck entityDuck = (com.starclient.EntityDuck) entity;
        entityDuck.star$setTexture(texture);
    }
}
