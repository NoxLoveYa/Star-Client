package com.starclient.helper;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.item.ItemDisplayContext;
import com.starclient.StarNameTagColorRegistry;

import java.util.Objects;

public class EntityTextureHelper {
    public static Identifier resolveTexture(Entity entity) {
        com.starclient.EntityDuck entityDuck = (com.starclient.EntityDuck) entity;
        Identifier captured = entityDuck.star$getTexture();
        if (captured != null) {
            return captured;
        }
        return null;
    }

    public static PlayerSkin resolveLocalPlayerDisplaySkin(Minecraft client, LocalPlayer player) {
        if (player != null) {
            return Objects.requireNonNull(player.getSkin());
        }

        PlayerSkin lookedUp = client.getSkinManager().createLookup(client.getGameProfile(), false).get();
        if (lookedUp != null) {
            return lookedUp;
        }

        return Objects.requireNonNull(DefaultPlayerSkin.get(client.getGameProfile()));
    }

    public static ItemIcon resolveItemIcon(ItemEntity itemEntity) {
        if (itemEntity.getItem().isEmpty()) {
            return null;
        }
        ItemStackRenderState itemRenderState = new ItemStackRenderState();
        Minecraft.getInstance()
                .getItemModelResolver()
                .updateForNonLiving(itemRenderState, itemEntity.getItem(), ItemDisplayContext.GROUND, itemEntity);
        TextureAtlasSprite sprite = itemRenderState.pickParticleIcon(itemEntity.getRandom());
        if (sprite == null) {
            return null;
        }
        return new ItemIcon(
                sprite.atlasLocation(),
                new StarNameTagColorRegistry.UvRect(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1()));
    }

    public static StarNameTagColorRegistry.UvRect resolveUv(Entity entity, Identifier texture) {
        if (entity instanceof net.minecraft.client.player.AbstractClientPlayer
                || entity instanceof net.minecraft.world.entity.Avatar) {
            return StarNameTagColorRegistry.UvRect.playerFace();
        }
        if (texture != null) {
            String path = texture.getPath();
            if (path.contains("/cow/") || path.contains("mooshroom")) {
                return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 8f, 8f, 16f, 16f);
            }
            if (path.contains("/chicken/")) {
                return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 8f, 8f, 16f, 16f);
            }
            if (path.contains("/pig/")) {
                return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 8f, 8f, 16f, 16f);
            }
        }
        EntityType<?> type = entity.getType();
        if (type == EntityType.ZOMBIE || type == EntityType.HUSK || type == EntityType.DROWNED
                || type == EntityType.ZOMBIE_VILLAGER
                || type == EntityType.PLAYER || type == EntityType.PIGLIN || type == EntityType.PIGLIN_BRUTE
                || type == EntityType.ZOMBIFIED_PIGLIN) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 8f, 8f, 16f, 16f);
        }
        if (type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.WITHER_SKELETON
                || type == EntityType.CREEPER || type == EntityType.ENDERMAN) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 8f, 8f, 16f, 16f);
        }
        if (type == EntityType.SHEEP) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 8f, 8f, 14f, 14f);
        }
        if (type == EntityType.PIG) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 8f, 8f, 16f, 16f);
        }
        if (type == EntityType.WOLF) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 4f, 4f, 10f, 10f);
        }
        if (type == EntityType.CAT || type == EntityType.OCELOT) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 5f, 4f, 10f, 8f);
        }
        if (type == EntityType.RABBIT) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 37f, 5f, 42f, 9f);
        }
        if (type == EntityType.FOX) {
            return StarNameTagColorRegistry.UvRect.pixels(48f, 32f, 7f, 11f, 15f, 17f);
        }
        if (type == EntityType.GOAT) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 44f, 56f, 49f, 63f);
        }
        if (type == EntityType.POLAR_BEAR) {
            return StarNameTagColorRegistry.UvRect.pixels(128f, 64f, 7f, 7f, 14f, 14f);
        }
        if (type == EntityType.PANDA) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 9f, 15f, 22f, 25f);
        }
        if (type == EntityType.DONKEY) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 7f, 20f, 13f, 25f);
        }
        if (type == EntityType.HORSE || type == EntityType.MULE) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 7f, 20f, 13f, 25f);
        }
        if (type == EntityType.LLAMA || type == EntityType.TRADER_LLAMA) {
            return StarNameTagColorRegistry.UvRect.pixels(128f, 64f, 9f, 9f, 13f, 13f);
        }
        if (type == EntityType.CAMEL) {
            return StarNameTagColorRegistry.UvRect.pixels(128f, 128f, 0f, 0f, 15f, 12f);
        }
        if (type == EntityType.BEE) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 0f, 0f, 10f, 8f);
        }
        if (type == EntityType.TURTLE) {
            return StarNameTagColorRegistry.UvRect.pixels(128f, 64f, 0f, 0f, 9f, 9f);
        }
        if (type == EntityType.FROG) {
            return StarNameTagColorRegistry.UvRect.pixels(48f, 48f, 0f, 0f, 7f, 7f);
        }
        if (type == EntityType.STRIDER || type == EntityType.HOGLIN || type == EntityType.ZOGLIN) {
            return StarNameTagColorRegistry.UvRect.pixels(128f, 64f, 0f, 0f, 16f, 14f);
        }
        if (type == EntityType.SNIFFER) {
            return StarNameTagColorRegistry.UvRect.pixels(192f, 192f, 0f, 0f, 25f, 29f);
        }
        if (type == EntityType.BAT) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 0f, 0f, 6f, 6f);
        }
        if (type == EntityType.PARROT || type == EntityType.ALLAY || type == EntityType.VEX) {
            return StarNameTagColorRegistry.UvRect.pixels(32f, 32f, 0f, 0f, 8f, 8f);
        }
        if (type == EntityType.COD || type == EntityType.SALMON || type == EntityType.TROPICAL_FISH
                || type == EntityType.PUFFERFISH) {
            return StarNameTagColorRegistry.UvRect.pixels(32f, 32f, 0f, 0f, 8f, 8f);
        }
        if (type == EntityType.SQUID || type == EntityType.GLOW_SQUID) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 0f, 0f, 12f, 16f);
        }
        if (type == EntityType.AXOLOTL) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 0f, 0f, 8f, 5f);
        }
        if (type == EntityType.DOLPHIN) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 0f, 0f, 8f, 7f);
        }
        if (type == EntityType.BLAZE) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 8f, 8f, 16f, 16f);
        }
        if (type == EntityType.GHAST) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 16f, 8f, 32f, 24f);
        }
        if (type == EntityType.PHANTOM) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 0f, 8f, 16f, 24f);
        }
        if (type == EntityType.SHULKER) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 0f, 28f, 16f, 44f);
        }
        if (type == EntityType.GUARDIAN || type == EntityType.ELDER_GUARDIAN) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 0f, 0f, 12f, 12f);
        }
        if (type == EntityType.WARDEN || type == EntityType.RAVAGER) {
            return StarNameTagColorRegistry.UvRect.pixels(128f, 128f, 0f, 0f, 16f, 16f);
        }
        if (type == EntityType.WITHER) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 0f, 0f, 16f, 16f);
        }
        if (type == EntityType.ENDER_DRAGON) {
            return StarNameTagColorRegistry.UvRect.pixels(256f, 256f, 176f, 44f, 204f, 72f);
        }
        if (type == EntityType.SPIDER || type == EntityType.CAVE_SPIDER) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 40f, 12f, 48f, 20f);
        }
        if (type == EntityType.SILVERFISH) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 0f, 0f, 6f, 6f);
        }
        if (type == EntityType.ENDERMITE) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 0f, 0f, 4f, 3f);
        }
        if (type == EntityType.MAGMA_CUBE) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 0f, 0f, 8f, 8f);
        }
        if (type == EntityType.ARMADILLO) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 45f, 17f, 48f, 22f);
        }
        if (type == EntityType.VILLAGER || type == EntityType.WANDERING_TRADER) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 8f, 8f, 16f, 18f);
        }
        return null;
    }

    public static class ItemIcon {
        public final Identifier texture;
        public final StarNameTagColorRegistry.UvRect uvRect;

        public ItemIcon(Identifier texture, StarNameTagColorRegistry.UvRect uvRect) {
            this.texture = texture;
            this.uvRect = uvRect;
        }
    }
}
