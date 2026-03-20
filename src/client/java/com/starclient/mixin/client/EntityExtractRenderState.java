package com.starclient.mixin.client;

import com.starclient.EntityRenderStateDuck;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityExtractRenderState<T extends Entity, S extends EntityRenderState> {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void captureEntity(T entity, S entityRenderState, float f, CallbackInfo ci) {
        ((EntityRenderStateDuck) entityRenderState).star$setEntity((Entity)entity);
        ((EntityRenderStateDuck) entityRenderState).star$setNametag((Entity)entity);
    }
}
