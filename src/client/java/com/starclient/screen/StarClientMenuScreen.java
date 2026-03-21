package com.starclient.screen;

import com.starclient.StarClientOptions;
import java.awt.Color;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StarClientMenuScreen extends DynamicOptionPanelScreen {
    public StarClientMenuScreen(@Nullable Screen previousScreen) {
        super(previousScreen, Component.literal("StarClient"), buildTabs());
    }

    private static List<@NonNull MenuTab> buildTabs() {
        MenuSection visibility = new MenuSection("visibility", 0, listOf(
                toggle("players", () -> StarClientOptions.forcedTagPlayer,
                        value -> StarClientOptions.forcedTagPlayer = value),
                toggle("hostiles", () -> StarClientOptions.forceTagHostile,
                        value -> StarClientOptions.forceTagHostile = value),
                toggle("mobs", () -> StarClientOptions.forceTagMob,
                        value -> StarClientOptions.forceTagMob = value),
                toggle("items", () -> StarClientOptions.forceTagItem,
                        value -> StarClientOptions.forceTagItem = value)));

        MenuSection appearance = new MenuSection("distance / appearance", 1, listOf(
                slider("distance", 16.0, 256.0,
                        () -> Math.sqrt(Math.max(0.0, StarClientOptions.forceTagDistance)),
                        value -> StarClientOptions.forceTagDistance = value * value,
                        value -> Objects.requireNonNull(String.format(Locale.ROOT, "%.0f blocks", value))),
                slider("bg alpha", 0.0, 255.0,
                        StarClientMenuScreen::getNameTagBackgroundAlpha,
                        StarClientMenuScreen::setNameTagBackgroundAlpha,
                        value -> Objects.requireNonNull(String.format(Locale.ROOT, "%.0f", value)))));

        MenuSection stars = new MenuSection("shooting stars", 0, listOf(
                slider("density", 0.0, 50.0,
                        () -> StarClientOptions.shootingStarSpawnDensity,
                        value -> StarClientOptions.shootingStarSpawnDensity = (float) value,
                        value -> Objects.requireNonNull(String.format(Locale.ROOT, "%.1f", value))),
                slider("min size", 0.25, 6.0,
                        () -> StarClientOptions.shootingStarMinSize,
                        value -> {
                            StarClientOptions.shootingStarMinSize = (float) value;
                            if (StarClientOptions.shootingStarMaxSize < StarClientOptions.shootingStarMinSize) {
                                StarClientOptions.shootingStarMaxSize = StarClientOptions.shootingStarMinSize;
                            }
                        },
                        value -> Objects.requireNonNull(String.format(Locale.ROOT, "%.2f", value))),
                slider("max size", 0.25, 6.0,
                        () -> StarClientOptions.shootingStarMaxSize,
                        value -> {
                            StarClientOptions.shootingStarMaxSize = (float) value;
                            if (StarClientOptions.shootingStarMaxSize < StarClientOptions.shootingStarMinSize) {
                                StarClientOptions.shootingStarMinSize = StarClientOptions.shootingStarMaxSize;
                            }
                        },
                        value -> Objects.requireNonNull(String.format(Locale.ROOT, "%.2f", value)))));

        MenuSection colorRange = new MenuSection("color range", 1, listOf(
                slider("hue min", 0.0, 1.0,
                        () -> StarClientOptions.shootingStarHueMin,
                        value -> {
                            StarClientOptions.shootingStarHueMin = (float) value;
                            if (StarClientOptions.shootingStarHueMax < StarClientOptions.shootingStarHueMin) {
                                StarClientOptions.shootingStarHueMax = StarClientOptions.shootingStarHueMin;
                            }
                        },
                        value -> Objects.requireNonNull(String.format(Locale.ROOT, "%.2f", value))),
                slider("hue max", 0.0, 1.0,
                        () -> StarClientOptions.shootingStarHueMax,
                        value -> {
                            StarClientOptions.shootingStarHueMax = (float) value;
                            if (StarClientOptions.shootingStarHueMax < StarClientOptions.shootingStarHueMin) {
                                StarClientOptions.shootingStarHueMin = StarClientOptions.shootingStarHueMax;
                            }
                        },
                        value -> Objects.requireNonNull(String.format(Locale.ROOT, "%.2f", value)))));

        MenuSection presets = new MenuSection("misc presets", 0, listOf(
                action("reset star visuals", () -> {
                    StarClientOptions.shootingStarSpawnDensity = 10.0f;
                    StarClientOptions.shootingStarMinSize = 0.75f;
                    StarClientOptions.shootingStarMaxSize = 2.75f;
                    StarClientOptions.shootingStarHueMin = 0.0f;
                    StarClientOptions.shootingStarHueMax = 1.0f;
                }),
                action("reset nametag options", () -> {
                    StarClientOptions.forceTagMob = true;
                    StarClientOptions.forceTagHostile = true;
                    StarClientOptions.forcedTagPlayer = true;
                    StarClientOptions.forceTagItem = true;
                    StarClientOptions.forceTagDistance = 32 * 64;
                    StarClientOptions.pendingNameTagBgColor = new Color(25, 25, 25, 165).getRGB();
                })));

        MenuTab nametags = new MenuTab("nametags", listOf(visibility, appearance));
        MenuTab visuals = new MenuTab("visuals", listOf(stars, colorRange));
        MenuTab misc = new MenuTab("misc", listOf(presets));

        return listOf(nametags, visuals, misc);
    }

    private static double getNameTagBackgroundAlpha() {
        return (StarClientOptions.pendingNameTagBgColor >>> 24) & 0xFF;
    }

    private static void setNameTagBackgroundAlpha(double alphaValue) {
        int alpha = (int) Math.round(Math.max(0.0, Math.min(255.0, alphaValue)));
        int rgb = StarClientOptions.pendingNameTagBgColor & 0x00FFFFFF;
        StarClientOptions.pendingNameTagBgColor = (alpha << 24) | rgb;
    }

    @Override
    protected void applyBackgroundEffects() {
        shootingStarsRenderer.setSpawnDensity(StarClientOptions.shootingStarSpawnDensity);
        shootingStarsRenderer.setSizeRange(
                StarClientOptions.shootingStarMinSize,
                StarClientOptions.shootingStarMaxSize);
        shootingStarsRenderer.setHueRange(
                StarClientOptions.shootingStarHueMin,
                StarClientOptions.shootingStarHueMax);
    }
}