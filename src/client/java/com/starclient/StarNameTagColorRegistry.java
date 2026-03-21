package com.starclient;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class StarNameTagColorRegistry {
    private static final Map<Component, Integer> BG_COLORS = new WeakHashMap<>();
    private static final Map<Component, Identifier> HEAD_TEXTURES = new WeakHashMap<>();

    public static void register(Component nameTag, int bgColor, @Nullable Identifier headTexture) {
        BG_COLORS.put(nameTag, bgColor);
        if (headTexture != null) HEAD_TEXTURES.put(nameTag, headTexture);
    }

    public static int get(Component nameTag) {
        return BG_COLORS.getOrDefault(nameTag, -1);
    }

    @Nullable
    public static Identifier getHeadTexture(Component nameTag) {
        return HEAD_TEXTURES.get(nameTag);
    }
}
