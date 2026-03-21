package com.starclient;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.HashMap;

public class StarNameTagColorRegistry {
    public static final Map<String, Integer> BG_COLORS = new HashMap<>();
    private static final Map<String, Identifier> HEAD_TEXTURES = new HashMap<>();

    private static String key(Component nameTag) {
        return nameTag.getString();
    }

    public static void register(Component nameTag, int bgColor, @Nullable Identifier headTexture) {
        String key = key(nameTag);
        BG_COLORS.put(key, bgColor);
        if (headTexture != null) HEAD_TEXTURES.put(key, headTexture);
    }

    public static int get(Component nameTag) {
        return BG_COLORS.getOrDefault(key(nameTag), -1);
    }

    @Nullable
    public static Identifier getHeadTexture(Component nameTag) {
        return HEAD_TEXTURES.get(key(nameTag));
    }

    public static void clearAll() {
        BG_COLORS.clear();
        HEAD_TEXTURES.clear();
    }
}
