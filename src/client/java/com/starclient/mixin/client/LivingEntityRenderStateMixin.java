package com.starclient.mixin.client;

import com.starclient.EntityRenderStateDuck;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRenderStateMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> {

    protected LivingEntityRenderStateMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Shadow
    protected abstract Identifier getTextureLocation(S renderState);

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void captureTexture(T entity, S entityRenderState, float f, CallbackInfo ci) {
        Identifier texture;
        if (entity instanceof AbstractClientPlayer player) {
            texture = player.getSkin().body().texturePath();
        } else {
            texture = this.getTextureLocation(entityRenderState);
        }
        ((EntityRenderStateDuck) entityRenderState).star$setTexture(texture);
    }
}
