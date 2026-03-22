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
                MenuSection playerNametag = new MenuSection("player nametag", "player", 0, listOf(
                                toggle("players", () -> StarClientOptions.forcedTagPlayer,
                                                value -> StarClientOptions.forcedTagPlayer = value)));

                MenuSection playerChams = new MenuSection("player chams", "player", 1, listOf(
                                toggle("enabled", () -> StarClientOptions.chamsPlayer,
                                                value -> StarClientOptions.chamsPlayer = value),
                                colorPicker("hue", () -> StarClientOptions.chamsHuePlayer,
                                                value -> StarClientOptions.chamsHuePlayer = (float) Math.max(0.0,
                                                                Math.min(1.0, value)),
                                                () -> StarClientOptions.chamsHuePlayerRainbow,
                                                value -> StarClientOptions.chamsHuePlayerRainbow = value,
                                                () -> StarClientOptions.chamsHuePlayerRainbowSpeed,
                                                value -> StarClientOptions.chamsHuePlayerRainbowSpeed = (float) value),
                                slider("opacity", 0.05, 1.0,
                                                () -> StarClientOptions.chamsAlphaPlayer,
                                                value -> StarClientOptions.chamsAlphaPlayer = (float) value,
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.2f", value)))));

                MenuSection playerDistance = new MenuSection("player distance", "player", 0, listOf(
                                slider("nametag distance", 16.0, 256.0,
                                                () -> Math.sqrt(Math.max(0.0,
                                                                StarClientOptions.forceTagDistancePlayer)),
                                                value -> StarClientOptions.forceTagDistancePlayer = value * value,
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.0f blocks", value))),
                                slider("chams distance", 16.0, 256.0,
                                                () -> Math.sqrt(Math.max(0.0, StarClientOptions.chamsDistancePlayer)),
                                                value -> StarClientOptions.chamsDistancePlayer = value * value,
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.0f blocks", value)))));

                MenuSection playerEffects = new MenuSection("player effects", "player", 0, listOf(
                                toggle("star aura", () -> StarClientOptions.localPlayerStarAura,
                                                value -> StarClientOptions.localPlayerStarAura = value)));

                MenuSection hostileNametag = new MenuSection("hostile nametag", "hostiles", 0, listOf(
                                toggle("hostiles", () -> StarClientOptions.forceTagHostile,
                                                value -> StarClientOptions.forceTagHostile = value)));

                MenuSection hostileChams = new MenuSection("hostile chams", "hostiles", 1, listOf(
                                toggle("enabled", () -> StarClientOptions.chamsHostile,
                                                value -> StarClientOptions.chamsHostile = value),
                                colorPicker("hue", () -> StarClientOptions.chamsHueHostile,
                                                value -> StarClientOptions.chamsHueHostile = (float) Math.max(0.0,
                                                                Math.min(1.0, value)),
                                                () -> StarClientOptions.chamsHueHostileRainbow,
                                                value -> StarClientOptions.chamsHueHostileRainbow = value,
                                                () -> StarClientOptions.chamsHueHostileRainbowSpeed,
                                                value -> StarClientOptions.chamsHueHostileRainbowSpeed = (float) value),
                                slider("opacity", 0.05, 1.0,
                                                () -> StarClientOptions.chamsAlphaHostile,
                                                value -> StarClientOptions.chamsAlphaHostile = (float) value,
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.2f", value)))));

                MenuSection hostileDistance = new MenuSection("hostile distance", "hostiles", 0, listOf(
                                slider("nametag distance", 16.0, 256.0,
                                                () -> Math.sqrt(Math.max(0.0,
                                                                StarClientOptions.forceTagDistanceHostile)),
                                                value -> StarClientOptions.forceTagDistanceHostile = value * value,
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.0f blocks", value))),
                                slider("chams distance", 16.0, 256.0,
                                                () -> Math.sqrt(Math.max(0.0, StarClientOptions.chamsDistanceHostile)),
                                                value -> StarClientOptions.chamsDistanceHostile = value * value,
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.0f blocks", value)))));

                MenuSection mobNametag = new MenuSection("mob nametag", "mobs", 0, listOf(
                                toggle("mobs", () -> StarClientOptions.forceTagMob,
                                                value -> StarClientOptions.forceTagMob = value)));

                MenuSection mobChams = new MenuSection("mob chams", "mobs", 1, listOf(
                                toggle("enabled", () -> StarClientOptions.chamsMob,
                                                value -> StarClientOptions.chamsMob = value),
                                colorPicker("hue", () -> StarClientOptions.chamsHueMob,
                                                value -> StarClientOptions.chamsHueMob = (float) Math.max(0.0,
                                                                Math.min(1.0, value)),
                                                () -> StarClientOptions.chamsHueMobRainbow,
                                                value -> StarClientOptions.chamsHueMobRainbow = value,
                                                () -> StarClientOptions.chamsHueMobRainbowSpeed,
                                                value -> StarClientOptions.chamsHueMobRainbowSpeed = (float) value),
                                slider("opacity", 0.05, 1.0,
                                                () -> StarClientOptions.chamsAlphaMob,
                                                value -> StarClientOptions.chamsAlphaMob = (float) value,
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.2f", value)))));

                MenuSection mobDistance = new MenuSection("mob distance", "mobs", 0, listOf(
                                slider("nametag distance", 16.0, 256.0,
                                                () -> Math.sqrt(Math.max(0.0, StarClientOptions.forceTagDistanceMob)),
                                                value -> StarClientOptions.forceTagDistanceMob = value * value,
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.0f blocks", value))),
                                slider("chams distance", 16.0, 256.0,
                                                () -> Math.sqrt(Math.max(0.0, StarClientOptions.chamsDistanceMob)),
                                                value -> StarClientOptions.chamsDistanceMob = value * value,
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.0f blocks", value)))));

                MenuSection sharedNameTag = new MenuSection("shared nametag", "shared", 0, listOf(
                                slider("bg alpha", 0.0, 255.0,
                                                StarClientMenuScreen::getNameTagBackgroundAlpha,
                                                StarClientMenuScreen::setNameTagBackgroundAlpha,
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.0f", value)))));

                MenuSection itemNametag = new MenuSection("item nametag", "items", 0, listOf(
                                toggle("items", () -> StarClientOptions.forceTagItem,
                                                value -> StarClientOptions.forceTagItem = value)));

                MenuSection itemChams = new MenuSection("item chams", "items", 1, listOf(
                                toggle("enabled", () -> StarClientOptions.chamsItem,
                                                value -> StarClientOptions.chamsItem = value),
                                colorPicker("hue", () -> StarClientOptions.chamsHueItem,
                                                value -> StarClientOptions.chamsHueItem = (float) Math.max(0.0,
                                                                Math.min(1.0, value)),
                                                () -> StarClientOptions.chamsHueItemRainbow,
                                                value -> StarClientOptions.chamsHueItemRainbow = value,
                                                () -> StarClientOptions.chamsHueItemRainbowSpeed,
                                                value -> StarClientOptions.chamsHueItemRainbowSpeed = (float) value),
                                slider("opacity", 0.05, 1.0,
                                                () -> StarClientOptions.chamsAlphaItem,
                                                value -> StarClientOptions.chamsAlphaItem = (float) value,
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.2f", value)))));

                MenuSection itemDistance = new MenuSection("item distance", "items", 0, listOf(
                                slider("nametag distance", 16.0, 256.0,
                                                () -> Math.sqrt(Math.max(0.0, StarClientOptions.forceTagDistanceItem)),
                                                value -> StarClientOptions.forceTagDistanceItem = value * value,
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.0f blocks", value))),
                                slider("chams distance", 16.0, 256.0,
                                                () -> Math.sqrt(Math.max(0.0, StarClientOptions.chamsDistanceItem)),
                                                value -> StarClientOptions.chamsDistanceItem = value * value,
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.0f blocks", value)))));

                MenuSection stars = new MenuSection("shooting stars", "stars", 0, listOf(
                                slider("density", 0.0, 50.0,
                                                () -> StarClientOptions.shootingStarSpawnDensity,
                                                value -> StarClientOptions.shootingStarSpawnDensity = (float) value,
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.1f", value))),
                                slider("min size", 0.25, 6.0,
                                                () -> StarClientOptions.shootingStarMinSize,
                                                value -> {
                                                        StarClientOptions.shootingStarMinSize = (float) value;
                                                        if (StarClientOptions.shootingStarMaxSize < StarClientOptions.shootingStarMinSize) {
                                                                StarClientOptions.shootingStarMaxSize = StarClientOptions.shootingStarMinSize;
                                                        }
                                                },
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.2f", value))),
                                slider("max size", 0.25, 6.0,
                                                () -> StarClientOptions.shootingStarMaxSize,
                                                value -> {
                                                        StarClientOptions.shootingStarMaxSize = (float) value;
                                                        if (StarClientOptions.shootingStarMaxSize < StarClientOptions.shootingStarMinSize) {
                                                                StarClientOptions.shootingStarMinSize = StarClientOptions.shootingStarMaxSize;
                                                        }
                                                },
                                                value -> Objects.requireNonNull(
                                                                String.format(Locale.ROOT, "%.2f", value)))));

                MenuSection colorRange = new MenuSection("color range", "stars", 1, listOf(
                                colorPicker("hue min",
                                                () -> StarClientOptions.shootingStarHueMin,
                                                value -> {
                                                        StarClientOptions.shootingStarHueMin = (float) value;
                                                        if (StarClientOptions.shootingStarHueMax < StarClientOptions.shootingStarHueMin) {
                                                                StarClientOptions.shootingStarHueMax = StarClientOptions.shootingStarHueMin;
                                                        }
                                                },
                                                () -> StarClientOptions.shootingStarHueMinRainbow,
                                                value -> StarClientOptions.shootingStarHueMinRainbow = value,
                                                () -> StarClientOptions.shootingStarHueMinRainbowSpeed,
                                                value -> StarClientOptions.shootingStarHueMinRainbowSpeed = (float) value),
                                colorPicker("hue max",
                                                () -> StarClientOptions.shootingStarHueMax,
                                                value -> {
                                                        StarClientOptions.shootingStarHueMax = (float) value;
                                                        if (StarClientOptions.shootingStarHueMax < StarClientOptions.shootingStarHueMin) {
                                                                StarClientOptions.shootingStarHueMin = StarClientOptions.shootingStarHueMax;
                                                        }
                                                },
                                                () -> StarClientOptions.shootingStarHueMaxRainbow,
                                                value -> StarClientOptions.shootingStarHueMaxRainbow = value,
                                                () -> StarClientOptions.shootingStarHueMaxRainbowSpeed,
                                                value -> StarClientOptions.shootingStarHueMaxRainbowSpeed = (float) value)));

                MenuSection menuTheme = new MenuSection("menu theme", "theme", 0, listOf(
                                colorPicker("menu hue",
                                                () -> StarClientOptions.menuThemeHue,
                                                value -> StarClientOptions.menuThemeHue = (float) Math.max(0.0,
                                                                Math.min(1.0, value)),
                                                () -> StarClientOptions.menuThemeHueRainbow,
                                                value -> StarClientOptions.menuThemeHueRainbow = value,
                                                () -> StarClientOptions.menuThemeHueRainbowSpeed,
                                                value -> StarClientOptions.menuThemeHueRainbowSpeed = (float) value)));

                MenuSection movement = new MenuSection("movement", "movement", 0, listOf(
                                toggle("water walk", () -> StarClientOptions.waterWalk,
                                                value -> StarClientOptions.waterWalk = value)));

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
                                        StarClientOptions.chamsPlayer = true;
                                        StarClientOptions.chamsHostile = true;
                                        StarClientOptions.chamsMob = true;
                                        StarClientOptions.chamsItem = true;
                                        StarClientOptions.chamsDistancePlayer = 32 * 64;
                                        StarClientOptions.chamsDistanceHostile = 32 * 64;
                                        StarClientOptions.chamsDistanceMob = 32 * 64;
                                        StarClientOptions.chamsDistanceItem = 32 * 64;
                                        StarClientOptions.chamsHuePlayer = 0.58f;
                                        StarClientOptions.chamsHueHostile = 0.00f;
                                        StarClientOptions.chamsHueMob = 0.32f;
                                        StarClientOptions.chamsHueItem = 0.14f;
                                        StarClientOptions.chamsHuePlayerRainbow = false;
                                        StarClientOptions.chamsHueHostileRainbow = false;
                                        StarClientOptions.chamsHueMobRainbow = false;
                                        StarClientOptions.chamsHueItemRainbow = false;
                                        StarClientOptions.chamsHuePlayerRainbowSpeed = 0.22f;
                                        StarClientOptions.chamsHueHostileRainbowSpeed = 0.22f;
                                        StarClientOptions.chamsHueMobRainbowSpeed = 0.22f;
                                        StarClientOptions.chamsHueItemRainbowSpeed = 0.22f;
                                        StarClientOptions.chamsAlphaPlayer = 0.35f;
                                        StarClientOptions.chamsAlphaHostile = 0.35f;
                                        StarClientOptions.chamsAlphaMob = 0.35f;
                                        StarClientOptions.chamsAlphaItem = 0.35f;
                                        StarClientOptions.shootingStarHueMinRainbow = false;
                                        StarClientOptions.shootingStarHueMaxRainbow = false;
                                        StarClientOptions.menuThemeHueRainbow = false;
                                        StarClientOptions.shootingStarHueMinRainbowSpeed = 0.22f;
                                        StarClientOptions.shootingStarHueMaxRainbowSpeed = 0.22f;
                                        StarClientOptions.menuThemeHueRainbowSpeed = 0.22f;
                                        StarClientOptions.localPlayerStarAura = false;
                                        StarClientOptions.waterWalk = false;
                                })));

                MenuTab visuals = new MenuTab("visuals",
                                listOf(
                                                playerNametag,
                                                playerChams,
                                                playerDistance,
                                                playerEffects,
                                                hostileNametag,
                                                hostileChams,
                                                hostileDistance,
                                                mobNametag,
                                                mobChams,
                                                mobDistance,
                                                itemNametag,
                                                itemChams,
                                                itemDistance,
                                                sharedNameTag,
                                                stars,
                                                colorRange));
                MenuTab movementTab = new MenuTab("movement", listOf(movement));
                MenuSection miscToggles = new MenuSection("misc toggles", "misc", 0, listOf(
                                toggle("show watermark", () -> StarClientOptions.showWatermark,
                                                value -> StarClientOptions.showWatermark = value)));
                MenuTab misc = new MenuTab("misc", listOf(presets, menuTheme, miscToggles));

                return listOf(visuals, movementTab, misc);
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
