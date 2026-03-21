package com.starclient.mixin.client;

import com.starclient.StarClientOptions;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public abstract class NameTagVisibilityMixin<T extends Entity, S extends EntityRenderState> {

        private static double maxForcedDistanceSq() {
                return Math.max(
                                Math.max(StarClientOptions.forceTagDistancePlayer,
                                                StarClientOptions.forceTagDistanceHostile),
                                Math.max(StarClientOptions.forceTagDistanceMob,
                                                StarClientOptions.forceTagDistanceItem));
        }

        private static boolean anyForceOptionEnabled() {
                return StarClientOptions.forceTagHostile
                                || StarClientOptions.forcedTagPlayer
                                || StarClientOptions.forceTagItem
                                || StarClientOptions.forceTagMob;
        }

        @ModifyConstant(method = "extractRenderState", constant = @Constant(doubleValue = 4096.0))
        private double removeNameTagDistanceLimit(double original) {
                if (anyForceOptionEnabled()) {
                        return maxForcedDistanceSq();
                }
                return original;
        }

        @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;shouldShowName(Lnet/minecraft/world/entity/Entity;D)Z"))
        @SuppressWarnings("unchecked")
        private boolean modifyNameTagVisibility(EntityRenderer<?, ?> instance, Entity entity, double distSq) {
                boolean vanilla = distSq < 4096.0
                                && ((EntityRendererAccessor<Entity>) instance).invokeShouldShowName(entity, distSq);

                boolean isHostile = entity instanceof Enemy;
                boolean isPlayer = entity.getType() == EntityType.PLAYER || entity instanceof Avatar;
                boolean isItem = entity.getType() == EntityType.ITEM;
                boolean isNonHostile = !isItem && !isHostile && !isPlayer && entity instanceof LivingEntity;

                boolean forced = (isHostile && StarClientOptions.forceTagHostile)
                                || (isPlayer && StarClientOptions.forcedTagPlayer)
                                || (isItem && StarClientOptions.forceTagItem)
                                || (isNonHostile && StarClientOptions.forceTagMob);

                if (forced) {
                        if (isHostile) {
                                return vanilla || distSq < StarClientOptions.forceTagDistanceHostile;
                        }
                        if (isPlayer) {
                                return vanilla || distSq < StarClientOptions.forceTagDistancePlayer;
                        }
                        if (isItem) {
                                return vanilla || distSq < StarClientOptions.forceTagDistanceItem;
                        }
                        if (isNonHostile) {
                                return vanilla || distSq < StarClientOptions.forceTagDistanceMob;
                        }
                }

                return vanilla;
        }
}
