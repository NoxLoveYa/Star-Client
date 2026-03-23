package com.starclient.mixin.client;

import com.starclient.EntityDuck;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public class EntityMixin implements EntityDuck {
    @Unique
    private net.minecraft.resources.Identifier star$texture;

    @Override
    public void star$setTexture(net.minecraft.resources.Identifier texture) {
        this.star$texture = texture;
    }

    @Override
    public net.minecraft.resources.Identifier star$getTexture() {
        return this.star$texture;
    }
}
