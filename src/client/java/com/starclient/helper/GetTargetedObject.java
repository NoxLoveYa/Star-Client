
package com.starclient.helper;

import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/**
 * Utility for getting the block or mob the cursor is pointing at.
 */
public class GetTargetedObject {
    /**
     * Gets the block or mob the cursor is pointing at, using the player's actual
     * reach distance.
     * Returns null if nothing is targeted.
     */
    public static Object getTargetedObject(Minecraft client, float tickDelta) {
        if (client.player == null || client.level == null)
            return null;

        // Camera position and look direction
        var player = client.player;
        var eyePos = Objects.requireNonNull(player).getEyePosition(tickDelta);
        var lookVec = player.getViewVector(tickDelta);
        double reach = player.isCreative() ? 5.0D : 4.5D;
        var reachVec = eyePos.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);

        // Raycast for blocks
        var blockHit =  Objects.requireNonNull(client.level).clip(new net.minecraft.world.level.ClipContext(
                eyePos, reachVec,
                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player));

        // Raycast for entities
        Entity closestEntity = null;
        double closestDist = reach;
        var aabb = player.getBoundingBox().expandTowards(lookVec.scale(reach)).inflate(1.0);
        for (Entity entity : Objects.requireNonNull(client.level).getEntities(player, aabb, e -> e.isPickable() && e != player)) {
            var bb = entity.getBoundingBox().inflate(entity.getPickRadius());
            var result = bb.clip(eyePos, reachVec);
            if (result != null && result.isPresent()) {
                double dist = eyePos.distanceTo(result.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closestEntity = entity;
                }
            }
        }

        // Decide which is closer: block or entity
        if (closestEntity != null) {
            double blockDist = blockHit != null ? eyePos.distanceTo(blockHit.getLocation()) : Double.MAX_VALUE;
            if (closestDist < blockDist) {
                return closestEntity;
            }
        }
        if (blockHit != null && blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
            return blockHit;
        }
        return null;
    }
}