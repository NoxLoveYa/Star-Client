package com.starclient.mixin.client;

import com.starclient.StarClientOptions;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityWaterWalkMixin {

    @Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
    private void star$canStandOnWater(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        if (!StarClientOptions.waterWalk) {
            return;
        }

        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player)) {
            return;
        }

        if (!fluidState.is(FluidTags.WATER)) {
            return;
        }

        if (self.isUnderWater() || self.isSwimming() || self.isShiftKeyDown()) {
            return;
        }

        cir.setReturnValue(true);
    }

    @Inject(method = "getFrictionInfluencedSpeed", at = @At("HEAD"), cancellable = true)
    private void star$surfaceWaterWalkSpeed(float friction, CallbackInfoReturnable<Float> cir) {
        if (!StarClientOptions.waterWalk) {
            return;
        }

        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player)) {
            return;
        }

        if (!self.isInWater() || self.isUnderWater() || self.isSwimming() || self.isShiftKeyDown()
                || !self.onGround()) {
            return;
        }

        cir.setReturnValue(self.getSpeed());
    }
}
