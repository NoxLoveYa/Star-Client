package com.starclient.mixin.client;

import com.starclient.interfaces.IEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements IEntityRenderState {
    @Unique
    private Entity starclient$entity;

    public Entity starclient$getEntity() {
        return starclient$entity;
    }

    public void starclient$setEntity(Entity entity) {
        this.starclient$entity = entity;
    }
}
