package com.starclient;

import java.awt.*;

public class StarClientOptions {
    private static final float DEFAULT_RAINBOW_CYCLES_PER_SECOND = 0.22f;

    public static boolean forceTagMob = true;
    public static boolean forceTagHostile = true;
    public static boolean forcedTagPlayer = true;
    public static boolean forceTagItem = true;

    public static double forceTagDistancePlayer = 32 * 64;
    public static double forceTagDistanceHostile = 32 * 64;
    public static double forceTagDistanceMob = 32 * 64;
    public static double forceTagDistanceItem = 32 * 64;

    public static int pendingNameTagBgColor = new Color(25, 25, 25, 165).getRGB();

    public static boolean chamsPlayer = true;
    public static boolean chamsHostile = true;
    public static boolean chamsMob = true;
    public static boolean chamsItem = true;

    public static double chamsDistancePlayer = 32 * 64;
    public static double chamsDistanceHostile = 32 * 64;
    public static double chamsDistanceMob = 32 * 64;
    public static double chamsDistanceItem = 32 * 64;

    public static float chamsHuePlayer = 0.58f;
    public static float chamsHueHostile = 0.00f;
    public static float chamsHueMob = 0.32f;
    public static float chamsHueItem = 0.14f;
    public static boolean chamsHuePlayerRainbow = false;
    public static boolean chamsHueHostileRainbow = false;
    public static boolean chamsHueMobRainbow = false;
    public static boolean chamsHueItemRainbow = false;
    public static float chamsHuePlayerRainbowSpeed = DEFAULT_RAINBOW_CYCLES_PER_SECOND;
    public static float chamsHueHostileRainbowSpeed = DEFAULT_RAINBOW_CYCLES_PER_SECOND;
    public static float chamsHueMobRainbowSpeed = DEFAULT_RAINBOW_CYCLES_PER_SECOND;
    public static float chamsHueItemRainbowSpeed = DEFAULT_RAINBOW_CYCLES_PER_SECOND;

    public static float chamsAlphaPlayer = 1f;
    public static float chamsAlphaHostile = 1f;
    public static float chamsAlphaMob = 1f;
    public static float chamsAlphaItem = 1f;

    public static float shootingStarSpawnDensity = 10.0f;
    public static float shootingStarMinSize = 0.75f;
    public static float shootingStarMaxSize = 2.75f;
    public static float shootingStarHueMin = 0.0f;
    public static float shootingStarHueMax = 1.0f;
    public static boolean shootingStarHueMinRainbow = false;
    public static boolean shootingStarHueMaxRainbow = false;
    public static float shootingStarHueMinRainbowSpeed = DEFAULT_RAINBOW_CYCLES_PER_SECOND;
    public static float shootingStarHueMaxRainbowSpeed = DEFAULT_RAINBOW_CYCLES_PER_SECOND;
    public static float menuThemeHue = 0.75f;
    public static boolean menuThemeHueRainbow = false;
    public static float menuThemeHueRainbowSpeed = DEFAULT_RAINBOW_CYCLES_PER_SECOND;

    public static void tickRainbowHues(double elapsedSeconds) {
        if (elapsedSeconds <= 0.0) {
            return;
        }

        if (chamsHuePlayerRainbow) {
            chamsHuePlayer = advanceHue(chamsHuePlayer, elapsedSeconds, chamsHuePlayerRainbowSpeed);
        }
        if (chamsHueHostileRainbow) {
            chamsHueHostile = advanceHue(chamsHueHostile, elapsedSeconds, chamsHueHostileRainbowSpeed);
        }
        if (chamsHueMobRainbow) {
            chamsHueMob = advanceHue(chamsHueMob, elapsedSeconds, chamsHueMobRainbowSpeed);
        }
        if (chamsHueItemRainbow) {
            chamsHueItem = advanceHue(chamsHueItem, elapsedSeconds, chamsHueItemRainbowSpeed);
        }
        if (shootingStarHueMinRainbow) {
            shootingStarHueMin = advanceHue(shootingStarHueMin, elapsedSeconds, shootingStarHueMinRainbowSpeed);
        }
        if (shootingStarHueMaxRainbow) {
            shootingStarHueMax = advanceHue(shootingStarHueMax, elapsedSeconds, shootingStarHueMaxRainbowSpeed);
        }
        if (menuThemeHueRainbow) {
            menuThemeHue = advanceHue(menuThemeHue, elapsedSeconds, menuThemeHueRainbowSpeed);
        }
    }

    private static float advanceHue(float hue, double elapsedSeconds, float speed) {
        float clampedSpeed = Math.max(0.01f, Math.min(3.0f, speed));
        float delta = (float) (clampedSpeed * elapsedSeconds);
        if (delta <= 0.0f) {
            return hue;
        }
        float next = (hue + delta) % 1.0f;
        return next < 0.0f ? next + 1.0f : next;
    }
}
