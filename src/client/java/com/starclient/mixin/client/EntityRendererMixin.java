package com.starclient.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starclient.EntityRenderStateDuck;
import com.starclient.StarClientOptions;
import com.starclient.StarNameTagColorRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Unique
    private static final int STAR$FULL_BRIGHT_LIGHT = 0x00F000F0;

    @Inject(method = "submitNameTag", at = @At("HEAD"), cancellable = true)
    private void modifyNameTag(S entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (entityRenderState.nameTag == null)
            return;

        EntityRenderStateDuck duck = (EntityRenderStateDuck) entityRenderState;

        if (!duck.star$isNametag())
            return;

        Entity entity = duck.star$getEntity();
        LivingEntity livingEntity = entity instanceof LivingEntity le ? le : null;
        Identifier texture = resolveTexture(entity, duck);
        StarNameTagColorRegistry.UvRect uvRect = resolveUv(entity, texture);
        float healthRatio = livingEntity != null && livingEntity.getMaxHealth() > 0f
                ? (livingEntity.getHealth() / livingEntity.getMaxHealth())
                : -1f;

        Component nameTag = buildNameTag(livingEntity, entityRenderState.nameTag);
        if (texture != null && uvRect != null) {
            StarNameTagColorRegistry.register(nameTag, StarClientOptions.pendingNameTagBgColor, texture, uvRect,
                    healthRatio);
        } else {
            StarNameTagColorRegistry.register(nameTag, StarClientOptions.pendingNameTagBgColor, null,
                    StarNameTagColorRegistry.UvRect.playerFace(), healthRatio);
        }

        submitNodeCollector.submitNameTag(
                poseStack,
                entityRenderState.nameTagAttachment,
                0,
                nameTag,
                !entityRenderState.isDiscrete,
                STAR$FULL_BRIGHT_LIGHT,
                entityRenderState.distanceToCameraSq,
                cameraRenderState);
        ci.cancel();
    }

    @Unique
    private Identifier resolveTexture(Entity entity, EntityRenderStateDuck duck) {
        if (entity instanceof AbstractClientPlayer player) {
            return player.getSkin().body().texturePath();
        }

        EntityType<?> type = entity.getType();
        String className = entity.getClass().getName().toLowerCase();

        if (type == EntityType.MOOSHROOM || className.contains("mooshroom")) {
            return Identifier.withDefaultNamespace("textures/entity/cow/red_mooshroom.png");
        }
        if (type == EntityType.COW || className.contains("cow")) {
            return Identifier.withDefaultNamespace("textures/entity/cow/temperate_cow.png");
        }
        if (type == EntityType.CHICKEN || className.contains("chicken")) {
            return Identifier.withDefaultNamespace("textures/entity/chicken/temperate_chicken.png");
        }
        if (type == EntityType.PIG || (className.contains("pig") && !className.contains("piglin"))) {
            return Identifier.withDefaultNamespace("textures/entity/pig/temperate_pig.png");
        }
        if (type == EntityType.ARMADILLO || className.contains("armadillo")) {
            return Identifier.withDefaultNamespace("textures/entity/armadillo.png");
        }
        if (type == EntityType.SLIME || className.contains("slime")) {
            return Identifier.withDefaultNamespace("textures/entity/slime/slime.png");
        }
        if (type == EntityType.MAGMA_CUBE || className.contains("magmacube") || className.contains("magma_cube")) {
            return Identifier.withDefaultNamespace("textures/entity/slime/magmacube.png");
        }
        if (type == EntityType.WOLF || className.contains("wolf")) {
            return Identifier.withDefaultNamespace("textures/entity/wolf/wolf.png");
        }
        if (type == EntityType.DONKEY || className.contains("donkey")) {
            return Identifier.withDefaultNamespace("textures/entity/horse/donkey.png");
        }
        if (type == EntityType.MULE || className.contains("mule")) {
            return Identifier.withDefaultNamespace("textures/entity/horse/mule.png");
        }
        if (type == EntityType.HORSE || className.contains("horse")) {
            return Identifier.withDefaultNamespace("textures/entity/horse/horse_brown.png");
        }
        if (type == EntityType.LLAMA || type == EntityType.TRADER_LLAMA || className.contains("llama")) {
            return Identifier.withDefaultNamespace("textures/entity/llama/creamy.png");
        }
        if (type == EntityType.CAMEL || className.contains("camel")) {
            return Identifier.withDefaultNamespace("textures/entity/camel/camel.png");
        }
        if (type == EntityType.GOAT || className.contains("goat")) {
            return Identifier.withDefaultNamespace("textures/entity/goat/goat.png");
        }
        if (type == EntityType.SHEEP || className.contains("sheep")) {
            return Identifier.withDefaultNamespace("textures/entity/sheep/sheep.png");
        }
        if (type == EntityType.POLAR_BEAR || className.contains("polarbear") || className.contains("polarbear")) {
            return Identifier.withDefaultNamespace("textures/entity/bear/polarbear.png");
        }
        if (type == EntityType.PANDA || className.contains("panda")) {
            return Identifier.withDefaultNamespace("textures/entity/panda/panda.png");
        }
        if (type == EntityType.FOX || className.contains("fox")) {
            return Identifier.withDefaultNamespace("textures/entity/fox/fox.png");
        }
        if (type == EntityType.CAT || className.contains("cat")) {
            return Identifier.withDefaultNamespace("textures/entity/cat/tabby.png");
        }
        if (type == EntityType.OCELOT || className.contains("ocelot")) {
            return Identifier.withDefaultNamespace("textures/entity/cat/ocelot.png");
        }
        if (type == EntityType.RABBIT || className.contains("rabbit")) {
            return Identifier.withDefaultNamespace("textures/entity/rabbit/brown.png");
        }
        if (type == EntityType.BEE || className.contains("bee")) {
            return Identifier.withDefaultNamespace("textures/entity/bee/bee.png");
        }
        if (type == EntityType.TURTLE || className.contains("turtle")) {
            return Identifier.withDefaultNamespace("textures/entity/turtle/big_sea_turtle.png");
        }
        if (type == EntityType.FROG || className.contains("frog")) {
            return Identifier.withDefaultNamespace("textures/entity/frog/temperate_frog.png");
        }
        if (type == EntityType.STRIDER || className.contains("strider")) {
            return Identifier.withDefaultNamespace("textures/entity/strider/strider.png");
        }
        if (type == EntityType.HOGLIN || className.contains("hoglin")) {
            return Identifier.withDefaultNamespace("textures/entity/hoglin/hoglin.png");
        }
        if (type == EntityType.ZOGLIN || className.contains("zoglin")) {
            return Identifier.withDefaultNamespace("textures/entity/hoglin/zoglin.png");
        }
        if (type == EntityType.SNIFFER || className.contains("sniffer")) {
            return Identifier.withDefaultNamespace("textures/entity/sniffer/sniffer.png");
        }

        if (type == EntityType.BAT || className.contains("bat")) {
            return Identifier.withDefaultNamespace("textures/entity/bat.png");
        }
        if (type == EntityType.PARROT || className.contains("parrot")) {
            return Identifier.withDefaultNamespace("textures/entity/parrot/parrot_blue.png");
        }
        if (type == EntityType.ALLAY || className.contains("allay")) {
            return Identifier.withDefaultNamespace("textures/entity/allay/allay.png");
        }
        if (type == EntityType.VEX || className.contains("vex")) {
            return Identifier.withDefaultNamespace("textures/entity/illager/vex.png");
        }

        if (type == EntityType.COD) {
            return Identifier.withDefaultNamespace("textures/entity/fish/cod.png");
        }
        if (type == EntityType.SALMON) {
            return Identifier.withDefaultNamespace("textures/entity/fish/salmon.png");
        }
        if (type == EntityType.TROPICAL_FISH) {
            return Identifier.withDefaultNamespace("textures/entity/fish/tropical_a.png");
        }
        if (type == EntityType.PUFFERFISH) {
            return Identifier.withDefaultNamespace("textures/entity/fish/pufferfish.png");
        }
        if (type == EntityType.SQUID) {
            return Identifier.withDefaultNamespace("textures/entity/squid/squid.png");
        }
        if (type == EntityType.GLOW_SQUID) {
            return Identifier.withDefaultNamespace("textures/entity/squid/glow_squid.png");
        }
        if (type == EntityType.AXOLOTL) {
            return Identifier.withDefaultNamespace("textures/entity/axolotl/axolotl_wild.png");
        }
        if (type == EntityType.DOLPHIN) {
            return Identifier.withDefaultNamespace("textures/entity/dolphin.png");
        }

        if (type == EntityType.BLAZE) {
            return Identifier.withDefaultNamespace("textures/entity/blaze.png");
        }
        if (type == EntityType.GHAST) {
            return Identifier.withDefaultNamespace("textures/entity/ghast/ghast.png");
        }
        if (type == EntityType.PHANTOM) {
            return Identifier.withDefaultNamespace("textures/entity/phantom.png");
        }
        if (type == EntityType.SHULKER) {
            return Identifier.withDefaultNamespace("textures/entity/shulker/shulker.png");
        }
        if (type == EntityType.GUARDIAN) {
            return Identifier.withDefaultNamespace("textures/entity/guardian.png");
        }
        if (type == EntityType.ELDER_GUARDIAN) {
            return Identifier.withDefaultNamespace("textures/entity/guardian_elder.png");
        }
        if (type == EntityType.WARDEN) {
            return Identifier.withDefaultNamespace("textures/entity/warden/warden.png");
        }
        if (type == EntityType.RAVAGER) {
            return Identifier.withDefaultNamespace("textures/entity/illager/ravager.png");
        }
        if (type == EntityType.WITHER) {
            return Identifier.withDefaultNamespace("textures/entity/wither/wither.png");
        }
        if (type == EntityType.ENDER_DRAGON) {
            return Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon.png");
        }

        if (type == EntityType.SPIDER) {
            return Identifier.withDefaultNamespace("textures/entity/spider/spider.png");
        }
        if (type == EntityType.CAVE_SPIDER) {
            return Identifier.withDefaultNamespace("textures/entity/spider/cave_spider.png");
        }
        if (type == EntityType.SILVERFISH) {
            return Identifier.withDefaultNamespace("textures/entity/silverfish.png");
        }
        if (type == EntityType.ENDERMITE) {
            return Identifier.withDefaultNamespace("textures/entity/endermite.png");
        }

        if (type == EntityType.EVOKER) {
            return Identifier.withDefaultNamespace("textures/entity/illager/evoker.png");
        }
        if (type == EntityType.ILLUSIONER) {
            return Identifier.withDefaultNamespace("textures/entity/illager/illusioner.png");
        }
        if (type == EntityType.PILLAGER) {
            return Identifier.withDefaultNamespace("textures/entity/illager/pillager.png");
        }
        if (type == EntityType.VINDICATOR) {
            return Identifier.withDefaultNamespace("textures/entity/illager/vindicator.png");
        }
        if (type == EntityType.VEX) {
            return Identifier.withDefaultNamespace("textures/entity/illager/vex.png");
        }
        if (type == EntityType.VILLAGER) {
            return Identifier.withDefaultNamespace("textures/entity/villager/villager.png");
        }
        if (type == EntityType.WANDERING_TRADER) {
            return Identifier.withDefaultNamespace("textures/entity/wandering_trader.png");
        }
        if (type == EntityType.ZOMBIE) {
            return Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");
        }
        if (type == EntityType.HUSK) {
            return Identifier.withDefaultNamespace("textures/entity/zombie/husk.png");
        }
        if (type == EntityType.DROWNED) {
            return Identifier.withDefaultNamespace("textures/entity/zombie/drowned.png");
        }
        if (type == EntityType.SKELETON) {
            return Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png");
        }
        if (type == EntityType.STRAY) {
            return Identifier.withDefaultNamespace("textures/entity/skeleton/stray.png");
        }
        if (type == EntityType.WITHER_SKELETON) {
            return Identifier.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png");
        }
        if (type == EntityType.BOGGED) {
            return Identifier.withDefaultNamespace("textures/entity/skeleton/bogged.png");
        }
        if (type == EntityType.PIGLIN) {
            return Identifier.withDefaultNamespace("textures/entity/piglin/piglin.png");
        }
        if (type == EntityType.PIGLIN_BRUTE) {
            return Identifier.withDefaultNamespace("textures/entity/piglin/piglin_brute.png");
        }
        if (type == EntityType.ZOMBIFIED_PIGLIN) {
            return Identifier.withDefaultNamespace("textures/entity/piglin/zombified_piglin.png");
        }
        if (type == EntityType.CREEPER) {
            return Identifier.withDefaultNamespace("textures/entity/creeper/creeper.png");
        }
        if (type == EntityType.ENDERMAN) {
            return Identifier.withDefaultNamespace("textures/entity/enderman/enderman.png");
        }

        Identifier captured = duck.star$getTexture();
        if (captured != null) {
            return captured;
        }

        return null;
    }

    @Unique
    private StarNameTagColorRegistry.UvRect resolveUv(Entity entity, Identifier texture) {
        if (entity instanceof AbstractClientPlayer || entity instanceof Avatar) {
            return StarNameTagColorRegistry.UvRect.playerFace();
        }

        if (texture != null) {
            String path = texture.getPath();
            if (path.contains("/cow/") || path.contains("mooshroom")) {
                return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 6f, 6f, 14f, 14f);
            }
            if (path.contains("/chicken/")) {
                return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 3f, 3f, 7f, 9f);
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
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 32f, 4f, 40f, 12f);
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

        if (type == EntityType.BOGGED || type == EntityType.BREEZE || type == EntityType.EVOKER
                || type == EntityType.ILLUSIONER
                || type == EntityType.PILLAGER || type == EntityType.VINDICATOR || type == EntityType.WITCH) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 8f, 8f, 16f, 16f);
        }

        if (type == EntityType.SLIME) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 32f, 8f, 8f, 16f, 16f);
        }

        if (type == EntityType.ARMADILLO) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 45f, 17f, 48f, 22f);
        }

        if (type == EntityType.VILLAGER || type == EntityType.WANDERING_TRADER) {
            return StarNameTagColorRegistry.UvRect.pixels(64f, 64f, 8f, 8f, 16f, 18f);
        }

        return null;
    }

    @Unique
    private Component buildNameTag(LivingEntity living, Component original) {
        if (living == null)
            return original; // no need to reconstruct, just reuse

        String healthStr = String.format("%.1f❤ ", living.getHealth());
        ChatFormatting healthColor = getHealthColor(living);

        return Component.empty()
                .append(Component.literal(healthStr).withStyle(healthColor))
                .append(original);
    }

    @Unique
    private ChatFormatting getHealthColor(LivingEntity living) {
        float ratio = living.getHealth() / living.getMaxHealth();
        if (ratio > 0.5f)
            return ChatFormatting.GREEN;
        if (ratio > 0.25f)
            return ChatFormatting.YELLOW;
        return ChatFormatting.RED;
    }
}
