package com.starclient.mixin.client;

import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(NameTagFeatureRenderer.Storage.class)
public interface NameTagStorageAccessor {
    @Accessor("nameTagSubmitsSeethrough")
    List<SubmitNodeStorage.NameTagSubmit> star$getNameTagSubmitsSeethrough();

    @Accessor("nameTagSubmitsNormal")
    List<SubmitNodeStorage.NameTagSubmit> star$getNameTagSubmitsNormal();
}
