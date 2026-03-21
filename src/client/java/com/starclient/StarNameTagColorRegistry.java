package com.starclient;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.HashMap;

public class StarNameTagColorRegistry {
    public static final Map<String, Integer> BG_COLORS = new HashMap<>();
    private static final Map<String, Identifier> HEAD_TEXTURES = new HashMap<>();
    private static final Map<String, UvRect> HEAD_UVS = new HashMap<>();
    private static final Map<String, Float> HEALTH_RATIOS = new HashMap<>();

    public record UvRect(float u0, float v0, float u1, float v1) {
        public static UvRect pixels(float textureWidth, float textureHeight, float minU, float minV, float maxU,
                float maxV) {
            return new UvRect(minU / textureWidth, minV / textureHeight, maxU / textureWidth, maxV / textureHeight);
        }

        public static UvRect playerFace() {
            return new UvRect(8f / 64f, 8f / 64f, 16f / 64f, 16f / 64f);
        }

        public static UvRect mobFaceDefault() {
            return new UvRect(0f, 0f, 8f / 64f, 8f / 32f);
        }
    }

    private static String key(Component nameTag) {
        return nameTag.getString();
    }

    public static void register(Component nameTag, int bgColor, @Nullable Identifier headTexture) {
        register(nameTag, bgColor, headTexture, UvRect.playerFace(), -1f);
    }

    public static void register(Component nameTag, int bgColor, @Nullable Identifier headTexture, UvRect uvRect) {
        register(nameTag, bgColor, headTexture, uvRect, -1f);
    }

    public static void register(Component nameTag, int bgColor, @Nullable Identifier headTexture, UvRect uvRect,
            float healthRatio) {
        String key = key(nameTag);
        BG_COLORS.put(key, bgColor);
        if (headTexture != null) {
            HEAD_TEXTURES.put(key, headTexture);
            HEAD_UVS.put(key, uvRect);
        }
        if (healthRatio >= 0f) {
            HEALTH_RATIOS.put(key, Math.max(0f, Math.min(1f, healthRatio)));
        }
    }

    public static int get(Component nameTag) {
        return BG_COLORS.getOrDefault(key(nameTag), -1);
    }

    @Nullable
    public static Identifier getHeadTexture(Component nameTag) {
        return HEAD_TEXTURES.get(key(nameTag));
    }

    public static UvRect getHeadUv(Component nameTag) {
        return HEAD_UVS.getOrDefault(key(nameTag), UvRect.playerFace());
    }

    public static float getHealthRatio(Component nameTag) {
        return HEALTH_RATIOS.getOrDefault(key(nameTag), -1f);
    }

    public static void clearAll() {
        BG_COLORS.clear();
        HEAD_TEXTURES.clear();
        HEAD_UVS.clear();
        HEALTH_RATIOS.clear();
    }
}
