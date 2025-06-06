package com.starclient.mixin.client;

import com.starclient.utils.CheatOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(Entity.class)
public class EspMixin {
    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void forceGlow(CallbackInfoReturnable<Boolean> cir) {
        if (!CheatOptions.GlowEnabled) {
            return;
        }
        Entity self = (Entity)(Object)this;

        if (self instanceof HostileEntity || self instanceof PlayerEntity) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    private void forceGlowColor(CallbackInfoReturnable<Integer> cir) {
        if (!CheatOptions.GlowEnabled) {
            return;
        }
        Entity self = (Entity)(Object)this;

        if (self instanceof HostileEntity) {
            cir.setReturnValue(Color.red.getRGB());
            cir.cancel();
        } else if (self instanceof  PlayerEntity) {
            cir.setReturnValue(Color.pink.getRGB());
            cir.cancel();
        }
    }

    @Inject(method = "isCustomNameVisible", at = @At("HEAD"), cancellable = true)
    private void forceNametag(CallbackInfoReturnable<Boolean> cir) {
        if (CheatOptions.NameEnabled) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}