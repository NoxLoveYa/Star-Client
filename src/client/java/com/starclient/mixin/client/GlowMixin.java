package com.starclient.mixin.client;

import com.starclient.CheatOptions;
import com.starclient.StarClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(Entity.class)
public class GlowMixin {
    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void IDK(CallbackInfoReturnable<Boolean> cir) {
        if (!CheatOptions.GlowEnabled) {
            return;
        }
        Entity self = (Entity)(Object)this;

        if (self instanceof HostileEntity || self instanceof PlayerEntity) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    private void IDK2(CallbackInfoReturnable<Integer> cir) {
        if (!CheatOptions.GlowEnabled) {
            return;
        }
        Entity self = (Entity)(Object)this;

        if (self instanceof HostileEntity) {
            cir.setReturnValue(Color.red.getRGB());
        } else if (self instanceof  PlayerEntity) {
            cir.setReturnValue(Color.pink.getRGB());
        }
    }
}
