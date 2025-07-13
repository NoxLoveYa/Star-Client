package com.starclient.utils;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.awt.color.ColorSpace;

public class ColorUtils extends Color {

    public ColorUtils(int r, int g, int b) {
        super(r, g, b);
    }

    public ColorUtils(int r, int g, int b, int a) {
        super(r, g, b, a);
    }

    public ColorUtils(int rgb) {
        super(rgb);
    }

    public ColorUtils(int rgba, boolean hasalpha) {
        super(rgba, hasalpha);
    }

    public ColorUtils(float r, float g, float b) {
        super(r, g, b);
    }

    public ColorUtils(float r, float g, float b, float a) {
        super(r, g, b, a);
    }

    public ColorUtils(ColorSpace cspace, float[] components, float alpha) {
        super(cspace, components, alpha);
    }

    public double getCurrentSeconds() {
        return Util.getMeasuringTimeMs() / 1000.0;
    }

    public int getRainbowTextColor(int timing) {
        double seconds = getCurrentSeconds();
        // HUE
        float hue = (float)(seconds * (((double)timing) / 250) % 1.0f);
        float saturation = 1.f; // High saturation for vivid colors
        float value = 1.f;      // High value for bright colors
        // color
        int rgbColor = MathHelper.hsvToRgb(hue, saturation, value);
        // add alpha component with bit shifting
        return 0xFF000000 | rgbColor;
    }

    public int getLerpedColor(Color startColor, Color endColor, double factor) {
        int r = (int) (startColor.getRed() + factor * (endColor.getRed() - startColor.getRed()));
        int g = (int) (startColor.getGreen() + factor * (endColor.getGreen() - startColor.getGreen()));
        int b = (int) (startColor.getBlue() + factor * (endColor.getBlue() - startColor.getBlue()));
        int a = (int) (startColor.getAlpha() + factor * (endColor.getAlpha() - startColor.getAlpha()));
        return new Color(r, g, b, a).getRGB();
    }
}
