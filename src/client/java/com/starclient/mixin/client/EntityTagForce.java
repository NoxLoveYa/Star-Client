package com.starclient.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import com.starclient.StarClientOptions;

@Mixin(EntityRenderer.class)
public abstract class EntityTagForce<T extends Entity, S extends EntityRenderState> {

	@Mixin(EntityRenderer.class)
	public interface EntityRendererAccessor<T extends Entity> {
		@Invoker("shouldShowName")
		boolean invokeShouldShowName(T entity, double distSq);
	}

	// Remove the 64 block (4096.0 sq) distance limit
	@ModifyConstant(
			method = "extractRenderState",
			constant = @Constant(doubleValue = 4096.0)
	)
	private double removeNameTagDistanceLimit(double original) {
		return StarClientOptions.forceTagDistance; // always passes the distance check
	}

	@Redirect(
			method = "extractRenderState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;shouldShowName(Lnet/minecraft/world/entity/Entity;D)Z"
			)
	)
	@SuppressWarnings("unchecked")
	private boolean modifyNameTagVisibility(EntityRenderer<?, ?> instance, Entity entity, double distSq) {
		boolean vanilla = distSq < 4096.0 && ((EntityRendererAccessor<Entity>) instance).invokeShouldShowName(entity, distSq);

		boolean isHostile = entity instanceof Enemy;
		boolean isPlayer = entity.getType() == EntityType.PLAYER;
		boolean isItem = entity.getType() == EntityType.ITEM;
		boolean isNonHostile = !isItem && !isHostile;

		boolean forced = (isHostile && StarClientOptions.forceTagHostile)
				|| (isPlayer && StarClientOptions.forcedTagPlayer)
				|| (isItem && StarClientOptions.forceTagItem)
				|| (isNonHostile && StarClientOptions.forceTagMob);

		return vanilla || forced;
	}
}