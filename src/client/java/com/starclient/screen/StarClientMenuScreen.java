package com.starclient.screen;

import com.starclient.StarClientOptions;
import com.starclient.screen.gui.DynamicOptionPanelScreen;
import com.starclient.screen.gui.MenuSection;
import com.starclient.screen.gui.MenuTab;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StarClientMenuScreen extends DynamicOptionPanelScreen {
    public StarClientMenuScreen(@Nullable Screen previousScreen) {
        super(previousScreen, Component.literal("StarClient"), buildTabs());
    }

    private static List<@NonNull MenuTab> buildTabs() {
        MenuSection playerVisibility = new MenuSection("player visibility", "player", 0, listOf(
                toggle("players", () -> StarClientOptions.forcedTagPlayer,
                        value -> StarClientOptions.forcedTagPlayer = value),
                slider("distance", 16.0, 256.0,
                        () -> Math.sqrt(Math.max(0.0, StarClientOptions.forceTagDistancePlayer)),
                        value -> StarClientOptions.forceTagDistancePlayer = value * value,
                        value -> Objects.requireNonNull(String.format(Locale.ROOT, "%.0f blocks", value)))));

        MenuSection hostileVisibility = new MenuSection("hostile visibility", "hostiles", 0, listOf(
                toggle("hostiles", () -> StarClientOptions.forceTagHostile,
                        value -> StarClientOptions.forceTagHostile = value),
                slider("distance", 16.0, 256.0,
                        () -> Math.sqrt(Math.max(0.0, StarClientOptions.forceTagDistanceHostile)),
                        value -> StarClientOptions.forceTagDistanceHostile = value * value,
                        value -> Objects.requireNonNull(String.format(Locale.ROOT, "%.0f blocks", value)))));

        MenuSection mobVisibility = new MenuSection("mob visibility", "mobs", 0, listOf(
                toggle("mobs", () -> StarClientOptions.forceTagMob,
                        value -> StarClientOptions.forceTagMob = value),
                slider("distance", 16.0, 256.0,
                        () -> Math.sqrt(Math.max(0.0, StarClientOptions.forceTagDistanceMob)),
                        value -> StarClientOptions.forceTagDistanceMob = value * value,
                        value -> Objects.requireNonNull(String.format(Locale.ROOT, "%.0f blocks", value)))));

        MenuSection sharedNameTag = new MenuSection("shared nametag", "shared", 0, listOf(
                slider("bg alpha", 0.0, 255.0,
                        StarClientMenuScreen::getNameTagBackgroundAlpha,
                        StarClientMenuScreen::setNameTagBackgroundAlpha,
                        value -> Objects.requireNonNull(String.format(Locale.ROOT, "%.0f", value)))));

        MenuSection itemVisibility = new MenuSection("item visibility", "items", 0, listOf(
                toggle("items", () -> StarClientOptions.forceTagItem,
                        value -> StarClientOptions.forceTagItem = value),
                slider("distance", 16.0, 256.0,
                        () -> Math.sqrt(Math.max(0.0, StarClientOptions.forceTagDistanceItem)),
                        value -> StarClientOptions.forceTagDistanceItem = value * value,
                        value -> Objects.requireNonNull(String.format(Locale.ROOT, "%.0f blocks", value)))));

        MenuSection stars = new MenuSection("shooting stars", "stars", 0, listOf(
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

        MenuSection colorRange = new MenuSection("color range", "stars", 1, listOf(
                colorPicker("hue min",
                        () -> StarClientOptions.shootingStarHueMin,
                        value -> {
                            StarClientOptions.shootingStarHueMin = (float) value;
                            if (StarClientOptions.shootingStarHueMax < StarClientOptions.shootingStarHueMin) {
                                StarClientOptions.shootingStarHueMax = StarClientOptions.shootingStarHueMin;
                            }
                        }),
                colorPicker("hue max",
                        () -> StarClientOptions.shootingStarHueMax,
                        value -> {
                            StarClientOptions.shootingStarHueMax = (float) value;
                            if (StarClientOptions.shootingStarHueMax < StarClientOptions.shootingStarHueMin) {
                                StarClientOptions.shootingStarHueMin = StarClientOptions.shootingStarHueMax;
                            }
                        })));

        MenuSection menuTheme = new MenuSection("menu theme", "theme", 0, listOf(
                colorPicker("menu hue",
                        () -> StarClientOptions.menuThemeHue,
                        value -> StarClientOptions.menuThemeHue = (float) Math.max(0.0, Math.min(1.0, value)))));

        MenuSection presets = new MenuSection("misc presets", "reset", 0, listOf(
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
                    StarClientOptions.forceTagDistancePlayer = 32 * 64;
                    StarClientOptions.forceTagDistanceHostile = 32 * 64;
                    StarClientOptions.forceTagDistanceMob = 32 * 64;
                    StarClientOptions.forceTagDistanceItem = 32 * 64;
                    StarClientOptions.pendingNameTagBgColor = new Color(25, 25, 25, 165).getRGB();
                })));

        MenuTab visuals = new MenuTab("visuals",
                listOf(playerVisibility, hostileVisibility, mobVisibility, itemVisibility, sharedNameTag, stars,
                        colorRange));
        MenuTab misc = new MenuTab("misc", listOf(presets, menuTheme));

        return listOf(visuals, misc);
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
