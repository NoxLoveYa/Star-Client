package com.starclient.mixin.client;

import com.starclient.EntityRenderStateDuck;
import com.starclient.StarClientOptions;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateDuck {
    @Unique
    private Entity star$entity;

    @Unique
    private boolean star$nameTag = false;

    @Unique
    private Identifier star$texture;

    @Override
    public void star$setEntity(Entity entity) { this.star$entity = entity; }

    @Override
    public Entity star$getEntity() { return this.star$entity; }

    @Override
    public void star$setNametag(Entity entity) {
        boolean isHostile = entity instanceof net.minecraft.world.entity.monster.Enemy;
        boolean isPlayer = entity.getType() == EntityType.PLAYER || entity instanceof Avatar;
        boolean isItem = entity.getType() == EntityType.ITEM;
        boolean isNonHostile = !isItem && !isHostile && !isPlayer;

        star$nameTag = (isHostile && StarClientOptions.forceTagHostile) || (isPlayer && StarClientOptions.forcedTagPlayer) || (isItem && StarClientOptions.forceTagItem) || (isNonHostile && StarClientOptions.forceTagMob);
    }

    @Override
    public boolean star$isNametag() { return this.star$nameTag; }

    @Override
    public void star$setTexture(Identifier texture) { this.star$texture = texture; }

    @Override
    public Identifier star$getTexture() { return this.star$texture; }
}