package com.starclient.mixin.client;

import com.starclient.EntityRenderStateDuck;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRenderStateCaptureMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void captureAvatarState(Avatar avatar, AvatarRenderState avatarRenderState, float tickDelta,
            CallbackInfo ci) {
        EntityRenderStateDuck duck = (EntityRenderStateDuck) avatarRenderState;
        duck.star$setEntity(avatar);
        duck.star$setNametag(avatar);
    }
}
