package com.starclient;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface EntityRenderStateDuck {
    void star$setEntity(Entity entity);
    Entity star$getEntity();

    void star$setNametag(Entity entity);

    boolean star$isNametag();
}
