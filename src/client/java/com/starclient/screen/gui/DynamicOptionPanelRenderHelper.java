package com.starclient.screen.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.awt.*;

final class DynamicOptionPanelRenderHelper {
    private DynamicOptionPanelRenderHelper() {
    }

    static void drawMagnifierIcon(@NonNull GuiGraphics context, int centerX, int centerY, int color) {
        int radius = 3;
        int left = centerX - radius;
        int top = centerY - radius;
        int right = centerX + radius;
        int bottom = centerY + radius;

        context.fill(left + 1, top, right, top + 1, color);
        context.fill(left + 1, bottom, right, bottom + 1, color);
        context.fill(left, top + 1, left + 1, bottom, color);
        context.fill(right, top + 1, right + 1, bottom, color);
        context.fill(centerX + 2, centerY + 2, centerX + 5, centerY + 3, color);
        context.fill(centerX + 3, centerY + 3, centerX + 4, centerY + 5, color);
    }

    static void drawGroupBox(@NonNull GuiGraphics context, @NonNull Font font, int x, int y, int width, int height,
            @NonNull String title, int groupColor, int panelInnerBorderColor, int subtitleColor) {
        context.fill(x, y, x + width, y + height, groupColor);
        context.fill(x, y, x + width, y + 1, panelInnerBorderColor);
        context.fill(x, y + height - 1, x + width, y + height, panelInnerBorderColor);
        context.fill(x, y, x + 1, y + height, panelInnerBorderColor);
        context.fill(x + width - 1, y, x + width, y + height, panelInnerBorderColor);
        context.drawString(font, Component.literal(title), x + 8, y + 7, subtitleColor, false);
    }

    static void drawResizeGrip(@NonNull GuiGraphics context, int panelX, int panelY, int panelWidth, int panelHeight,
            int handleSize, int lineColor, int panelColor) {
        int handleX = panelX + panelWidth - handleSize;
        int handleY = panelY + panelHeight - handleSize;
        context.fill(handleX, handleY, panelX + panelWidth - 1, panelY + panelHeight - 1, panelColor);

        int endX = panelX + panelWidth - 3;
        int endY = panelY + panelHeight - 3;
        for (int i = 0; i < 3; i++) {
            int startX = endX - 2 - (i * 3);
            int startY = endY;
            int diagonalLength = 3 + i;
            for (int step = 0; step < diagonalLength; step++) {
                int px = startX + step;
                int py = startY - step;
                context.fill(px, py, px + 1, py + 1, lineColor);
            }
        }
    }

    static void drawToggleControl(@NonNull GuiGraphics context, @NonNull Font font, @NonNull String label, int x,
            int y, int width, int height, boolean enabled, int controlTextColor, int groupColor,
            int controlBorderColor, int controlAccentColor) {
        context.drawString(font, Component.literal(label), x + 6, y + 6, controlTextColor, false);

        int toggleSize = 10;
        int toggleX = x + width - toggleSize - 6;
        int toggleY = y + (height - toggleSize) / 2;
        context.fill(toggleX, toggleY, toggleX + toggleSize, toggleY + toggleSize, groupColor);
        context.fill(toggleX, toggleY, toggleX + toggleSize, toggleY + 1, controlBorderColor);
        context.fill(toggleX, toggleY + toggleSize - 1, toggleX + toggleSize, toggleY + toggleSize,
                controlBorderColor);
        context.fill(toggleX, toggleY, toggleX + 1, toggleY + toggleSize, controlBorderColor);
        context.fill(toggleX + toggleSize - 1, toggleY, toggleX + toggleSize, toggleY + toggleSize,
                controlBorderColor);

        if (enabled) {
            context.fill(toggleX + 2, toggleY + 2, toggleX + toggleSize - 2, toggleY + toggleSize - 2,
                    controlAccentColor);
        }
    }

    static void drawActionControl(@NonNull GuiGraphics context, @NonNull Font font, @NonNull String label, int x,
            int y, int width, int controlTextColor, int controlValueColor) {
        context.drawString(font, Component.literal(label), x + 6, y + 6, controlTextColor, false);
        context.drawString(font, Component.literal("+"), x + width - 10, y + 6, controlValueColor, false);
    }

    static void drawSliderControl(@NonNull GuiGraphics context, @NonNull Font font, @NonNull String label,
            @NonNull String valueText, int x, int y, int width, double progress, int controlTextColor,
            int controlValueColor, int controlBorderColor, int controlAccentColor) {
        context.drawString(font, Component.literal(label), x + 6, y + 3, controlTextColor, false);

        int valueTextWidth = font.width(valueText);
        context.drawString(font, Component.literal(valueText), x + width - valueTextWidth - 6, y + 3,
                controlValueColor, false);

        int trackX = x + 6;
        int trackY = y + 14;
        int trackWidth = width - 12;
        context.fill(trackX, trackY, trackX + trackWidth, trackY + 2, controlBorderColor);

        int fillWidth = (int) Math.round(trackWidth * Math.max(0.0, Math.min(1.0, progress)));
        context.fill(trackX, trackY, trackX + fillWidth, trackY + 2, controlAccentColor);
    }

    static void drawHuePickerControl(@NonNull GuiGraphics context, @NonNull Font font, @NonNull String label, int x,
            int y, int width, double hueValue, int controlTextColor, int controlBorderColor) {
        context.drawString(font, Component.literal(label), x + 6, y + 3, controlTextColor, false);

        int gradientX = x + 6;
        int gradientY = y + 14;
        int gradientWidth = width - 28;
        int gradientHeight = 2;

        if (gradientWidth > 0) {
            for (int i = 0; i < gradientWidth; i++) {
                float hue = i / (float) Math.max(1, gradientWidth - 1);
                int color = (0xFF << 24) | (Color.HSBtoRGB(hue, 0.65f, 0.95f) & 0x00FFFFFF);
                context.fill(gradientX + i, gradientY, gradientX + i + 1, gradientY + gradientHeight, color);
            }
        }

        context.fill(gradientX, gradientY - 1, gradientX + gradientWidth, gradientY, controlBorderColor);
        context.fill(gradientX, gradientY + gradientHeight, gradientX + gradientWidth, gradientY + gradientHeight + 1,
                controlBorderColor);

        double clampedHue = Math.max(0.0, Math.min(1.0, hueValue));
        int handleX = gradientX + (int) Math.round(clampedHue * Math.max(0, gradientWidth - 1));
        context.fill(handleX - 1, gradientY - 2, handleX + 1, gradientY + gradientHeight + 2, 0xFFFFFFFF);

        int swatchColor = (0xFF << 24) | (Color.HSBtoRGB((float) clampedHue, 0.65f, 0.95f) & 0x00FFFFFF);
        int swatchX = x + width - 16;
        int swatchY = y + 5;
        context.fill(swatchX, swatchY, swatchX + 10, swatchY + 10, swatchColor);
        context.fill(swatchX, swatchY, swatchX + 10, swatchY + 1, controlBorderColor);
        context.fill(swatchX, swatchY + 9, swatchX + 10, swatchY + 10, controlBorderColor);
        context.fill(swatchX, swatchY, swatchX + 1, swatchY + 10, controlBorderColor);
        context.fill(swatchX + 9, swatchY, swatchX + 10, swatchY + 10, controlBorderColor);
    }

    static void drawSeparatorControl(@NonNull GuiGraphics context, @NonNull Font font, @NonNull String label, int x,
            int y, int width, int subtitleColor, int borderColor) {
        int labelWidth = font.width(label);
        int textX = x + 6;
        int centerY = y + 10;
        context.drawString(font, Component.literal(label), textX, y + 4, subtitleColor, false);

        int lineStart = textX + labelWidth + 8;
        int lineEnd = x + width - 6;
        if (lineEnd > lineStart) {
            context.fill(lineStart, centerY, lineEnd, centerY + 1, borderColor);
        }
    }
}
