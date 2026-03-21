package com.starclient;

import java.awt.*;

public class StarClientOptions {
    public static boolean forceTagMob = true;
    public static boolean forceTagHostile = true;
    public static boolean forcedTagPlayer = true;
    public static boolean forceTagItem = true;

    public static double forceTagDistance = 18 * 64;

    public static int pendingNameTagBgColor = new Color(25, 25, 25, 165).getRGB();

    public static float shootingStarSpawnDensity = 10.0f;
    public static float shootingStarMinSize = 0.75f;
    public static float shootingStarMaxSize = 2.75f;
    public static float shootingStarHueMin = 0.0f;
    public static float shootingStarHueMax = 1.0f;
}
