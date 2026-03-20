package com.starclient;

import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.WeakHashMap;

public class StarNameTagColorRegistry {
    private static final Map<Component, Integer> BG_COLORS = new WeakHashMap<>();

    public static void register(Component nameTag, int bgColor) {
        BG_COLORS.put(nameTag, bgColor);
    }

    public static int get(Component nameTag) {
        return BG_COLORS.getOrDefault(nameTag, -1); // -1 = use original
    }
}
