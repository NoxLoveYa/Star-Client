package com.starclient.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.util.SkinTextures;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PlayerUtils {
    /**
     * Gets the PlayerEntityModel for the local player.
     * This model is used by the game to render the player.
     *
     * @return The PlayerEntityModel instance, or null if the client/player/renderer is not available.
     */
    public static LoadedEntityModels getAllLoadedEntityModels() {
        return LoadedEntityModels.copy();
    }

    /**
     * Gets the Identifier for the local player's main skin texture.
     * This Identifier acts as the "skin supplier" for rendering.
     *
     * @return The Identifier of the player's skin, or null if the player is not available.
     */
    @Nullable
    public static Supplier<SkinTextures> getLocalPlayerSkinIdentifier() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player != null) {
            return new Supplier<SkinTextures>() {
                @Override
                public SkinTextures get() {
                    return player.getSkinTextures();
                };
            };
        }
        return null;
    }
}
