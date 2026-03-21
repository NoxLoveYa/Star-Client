package com.starclient.mixin.client;

import com.starclient.StarNameTagColorRegistry;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Mixin(NameTagFeatureRenderer.Storage.class)
public class NametagFeatureRendererMixin {
    @ModifyArgs(
            method = "add",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeStorage$NameTagSubmit;<init>(Lorg/joml/Matrix4f;FFLnet/minecraft/network/chat/Component;IIID)V", // adjust to actual constructor target
                    ordinal = 1 // the see through submit
            )
    )
    private void modifyBackgroundColor(Args args) {
        Component component = args.get(3);
        int registered = StarNameTagColorRegistry.get(component);
        if (registered != -1) {
            args.set(6, registered);
        }
    }
}

