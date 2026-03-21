package com.starclient;

import java.awt.*;

public class StarClientOptions {
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

    public static float mobChamsAlpha = 0.35f;

    public static float shootingStarSpawnDensity = 10.0f;
    public static float shootingStarMinSize = 0.75f;
    public static float shootingStarMaxSize = 2.75f;
    public static float shootingStarHueMin = 0.0f;
    public static float shootingStarHueMax = 1.0f;
    public static float menuThemeHue = 0.75f;
}
