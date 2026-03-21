package com.starclient;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Unique;

public interface EntityRenderStateDuck {
    void star$setEntity(Entity entity);
    Entity star$getEntity();

    void star$setNametag(Entity entity);

    boolean star$isNametag();

    void star$setTexture(Identifier textureLocation);

    @Unique
    Identifier star$getTexture();
}
