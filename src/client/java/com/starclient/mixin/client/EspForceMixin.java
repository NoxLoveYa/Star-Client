package com.starclient.mixin.client;

import com.starclient.utils.CheatOptions;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EspForceMixin {
    @Shadow @Final protected EntityRenderDispatcher dispatcher;

    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void forceBlToTrue(Entity entity, EntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (!CheatOptions.NameEnabled){
            return;
        }
        state.displayName = entity.getDisplayName();
    }

}
