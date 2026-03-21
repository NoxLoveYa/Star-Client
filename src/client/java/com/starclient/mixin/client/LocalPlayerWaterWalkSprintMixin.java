package com.starclient.mixin.client;

import com.starclient.StarClientOptions;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerWaterWalkSprintMixin {

    @Redirect(method = "isSprintingPossible", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInShallowWater()Z"))
    private boolean star$allowSprintWhenWaterWalking(LocalPlayer self) {
        if (!StarClientOptions.waterWalk) {
            return self.isInShallowWater();
        }

        if (self.isInWater() && !self.isUnderWater() && !self.isSwimming() && !self.isShiftKeyDown()) {
            return false;
        }

        return self.isInShallowWater();
    }
}
