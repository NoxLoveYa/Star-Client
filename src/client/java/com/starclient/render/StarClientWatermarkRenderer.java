package com.starclient.render;

import com.starclient.StarClientOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public final class StarClientWatermarkRenderer {
    private static final int PANEL_COLOR = new Color(10, 10, 12, 232).getRGB();
    private static final int HEADER_COLOR = new Color(16, 16, 22, 245).getRGB();
    private static final int TITLE_COLOR = new Color(240, 238, 255, 255).getRGB();
    private static final int TEXT_COLOR = new Color(232, 228, 246, 255).getRGB();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);
    private static int x = 8;
    private static int y = 8;
    private static boolean dragging = false;
    private static int dragOffsetX = 0;
    private static int dragOffsetY = 0;

    private StarClientWatermarkRenderer() {
    }

    public static void render(GuiGraphics context) {
        Minecraft client = Minecraft.getInstance();
        var font = client.font;

        clampToWindow(client);

        LocalPlayer player = client.player;
        String playerName = Objects
                .requireNonNull(player != null ? player.getName().getString() : client.getUser().getName());
        String timeText = Objects.requireNonNull(LocalTime.now().format(TIME_FORMATTER));
        String starText = "✦";
        String brandText = "starclient";
        String separator = " | ";

        int height = 20;
        int padding = 6;
        int gap = 4;
        int faceSize = 12;

        int sepWidth = font.width(separator);
        int nameWidth = font.width(playerName);
        int timeWidth = font.width(timeText);
        int starWidth = font.width(starText);
        int brandWidth = font.width(brandText);

        int contentWidth = starWidth + 3 + brandWidth + sepWidth + faceSize + gap + nameWidth + sepWidth + timeWidth;
        int width = padding * 2 + contentWidth;

        int panelBorderColor = getPanelBorderColor();
        int panelInnerBorderColor = getPanelInnerBorderColor();

        context.fill(x, y, x + width, y + height, PANEL_COLOR);
        context.fill(x, y, x + width, y + (height / 2), HEADER_COLOR);
        context.fill(x, y, x + width, y + 1, panelBorderColor);
        context.fill(x, y + height - 1, x + width, y + height, panelBorderColor);
        context.fill(x, y, x + 1, y + height, panelBorderColor);
        context.fill(x + width - 1, y, x + width, y + height, panelBorderColor);
        context.fill(x + 1, y + 2, x + width - 1, y + 3, panelInnerBorderColor);

        int textY = y + (height - 8) / 2;
        int drawX = x + padding;

        context.drawString(font, Component.literal(starText), drawX, textY, panelBorderColor, false);
        drawX += starWidth + 3;

        context.drawString(font, Component.literal(brandText), drawX, textY, TITLE_COLOR, false);
        drawX += brandWidth;

        context.drawString(font, Component.literal(separator), drawX, textY, panelInnerBorderColor, false);
        drawX += sepWidth;

        int faceY = y + (height - faceSize) / 2;
        if (player != null) {
            PlayerFaceRenderer.draw(context, player.getSkin(), drawX, faceY, faceSize);
        } else {
            context.fill(drawX, faceY, drawX + faceSize, faceY + faceSize, HEADER_COLOR);
            context.fill(drawX, faceY, drawX + faceSize, faceY + 1, panelInnerBorderColor);
            context.fill(drawX, faceY + faceSize - 1, drawX + faceSize, faceY + faceSize, panelInnerBorderColor);
            context.fill(drawX, faceY, drawX + 1, faceY + faceSize, panelInnerBorderColor);
            context.fill(drawX + faceSize - 1, faceY, drawX + faceSize, faceY + faceSize, panelInnerBorderColor);
        }

        drawX += faceSize + gap;
        context.drawString(font, Component.literal(playerName), drawX, textY, TEXT_COLOR, false);
        drawX += nameWidth;

        context.drawString(font, Component.literal(separator), drawX, textY, panelInnerBorderColor, false);
        drawX += sepWidth;

        context.drawString(font, Component.literal(timeText), drawX, textY, TEXT_COLOR, false);
    }

    public static boolean beginDragging(double mouseX, double mouseY) {
        Minecraft client = Minecraft.getInstance();
        WatermarkLayout layout = layout(client);
        if (!layout.contains(mouseX, mouseY)) {
            return false;
        }

        dragging = true;
        dragOffsetX = (int) Math.round(mouseX) - x;
        dragOffsetY = (int) Math.round(mouseY) - y;
        return true;
    }

    public static void dragTo(double mouseX, double mouseY) {
        if (!dragging) {
            return;
        }

        x = (int) Math.round(mouseX) - dragOffsetX;
        y = (int) Math.round(mouseY) - dragOffsetY;
        clampToWindow(Minecraft.getInstance());
    }

    public static void endDragging() {
        dragging = false;
    }

    public static boolean isDragging() {
        return dragging;
    }

    private static void clampToWindow(Minecraft client) {
        WatermarkLayout layout = layout(client);
        int maxX = Math.max(0, client.getWindow().getGuiScaledWidth() - layout.width());
        int maxY = Math.max(0, client.getWindow().getGuiScaledHeight() - layout.height());
        x = Math.max(0, Math.min(maxX, x));
        y = Math.max(0, Math.min(maxY, y));
    }

    private static WatermarkLayout layout(Minecraft client) {
        var font = client.font;
        String playerName = Objects
                .requireNonNull(
                        client.player != null ? client.player.getName().getString() : client.getUser().getName());
        String timeText = Objects.requireNonNull(LocalTime.now().format(TIME_FORMATTER));
        String separator = " | ";
        String starText = "✦";
        String brandText = "starclient";

        int padding = 6;
        int gap = 4;
        int faceSize = 12;
        int height = 20;

        int sepWidth = font.width(separator);
        int nameWidth = font.width(playerName);
        int timeWidth = font.width(timeText);
        int starWidth = font.width(starText);
        int brandWidth = font.width(brandText);

        int contentWidth = starWidth + 3 + brandWidth + sepWidth + faceSize + gap + nameWidth + sepWidth + timeWidth;
        int width = padding * 2 + contentWidth;
        return new WatermarkLayout(width, height);
    }

    private static int themeColor(float saturation, float brightness, int alpha) {
        float hue = (float) Math.max(0.0, Math.min(1.0, StarClientOptions.menuThemeHue));
        int rgb = Color.HSBtoRGB(hue, saturation, brightness) & 0x00FFFFFF;
        int clampedAlpha = Math.max(0, Math.min(255, alpha));
        return (clampedAlpha << 24) | rgb;
    }

    private static int getPanelBorderColor() {
        return themeColor(0.63f, 0.62f, 255);
    }

    private static int getPanelInnerBorderColor() {
        return themeColor(0.50f, 0.33f, 220);
    }

    private record WatermarkLayout(int width, int height) {
        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }
}