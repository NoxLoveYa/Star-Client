package com.starclient.render;

import com.starclient.StarClientOptions;
import com.starclient.helper.GetTargetedObject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import java.awt.Color;
import java.util.Objects;

public final class StarClientInfoBoxRenderer {
    // Layout record for hit detection (center-based logic)
    private record InfoBoxLayout(int width, int height) {
    }

    // Center coordinates for the info box
    private static int x = 120; // Center X
    private static int y = 80; // Top Y

    // Returns the layout (width/height) for the current info box, for hit detection
    // and clamping
    private static InfoBoxLayout layout(Minecraft client) {
        final int minWidth = 160, minHeight = 36, padding = 6, gap = 4, iconSize = 16;
        var font = client.font;
        Object targeted = GetTargetedObject.getTargetedObject(client, 0.0f);
        int width = minWidth, height = minHeight;
        if (targeted instanceof LivingEntity livingEntity) {
            String name = livingEntity.getName().getString();
            float health = livingEntity.getHealth();
            float maxHealth = livingEntity.getMaxHealth();
            int hearts = (int) Math.ceil(health / 2.0f);
            int maxHearts = (int) Math.ceil(maxHealth / 2.0f);
            StringBuilder heartsBuilder = new StringBuilder();
            for (int i = 0; i < hearts; i++)
                heartsBuilder.append("\u2764");
            for (int i = hearts; i < maxHearts; i++)
                heartsBuilder.append("\u2661");
            String heartsStr = heartsBuilder.toString();
            String typeStr = livingEntity.getType().toShortString();
            int contentWidth = Math.max(Math.max(font.width(name), font.width(heartsStr)), font.width(typeStr));
            width = padding * 2 + contentWidth;
            height = padding * 2 + (3 + livingEntity.getActiveEffects().size()) * (font.lineHeight + 1);
        } else if (targeted instanceof BlockHitResult blockHit) {
            BlockState state = Objects.requireNonNull(client.level).getBlockState(blockHit.getBlockPos());
            Block block = state.getBlock();
            String title = block.getName().getString();
            String namespace = "minecraft";
            try {
                namespace = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getNamespace();
            } catch (Exception ignored) {
            }
            int contentWidth = iconSize + gap + Math.max(font.width(title), font.width(namespace));
            width = padding * 2 + contentWidth;
            height = padding * 2 + 2 * (font.lineHeight + 1);
        }
        width = Math.max(width, minWidth);
        height = Math.max(height, minHeight);
        return new InfoBoxLayout(width, height);
    }

    // Begin dragging logic, now using center as anchor
    public static boolean beginDragging(double mouseX, double mouseY) {
        Minecraft client = Minecraft.getInstance();
        InfoBoxLayout layout = layout(client);
        int width = layout.width();
        int height = layout.height();
        int left = x - width / 2;
        int top = y;
        if (!(mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height)) {
            return false;
        }
        dragging = true;
        dragOffsetX = (int) Math.round(mouseX) - x;
        dragOffsetY = (int) Math.round(mouseY) - y;
        return true;
    }

    private static final int HEADER_COLOR = new Color(16, 16, 22, 245).getRGB();
    private static final int TITLE_COLOR = new Color(240, 238, 255, 255).getRGB();
    private static boolean dragging = false;
    private static int dragOffsetX = 0;
    private static int dragOffsetY = 0;

    private StarClientInfoBoxRenderer() {
    }

    public static void render(GuiGraphics context) {
        if (!StarClientOptions.showInfoBox)
            return;
        Minecraft client = Minecraft.getInstance();
        var font = client.font;
        clampToWindow(client);

        // If menu is open, show dummy info for dragging/visualization
        boolean showDummy = client.screen != null
                && client.screen.getClass().getSimpleName().equals("StarClientMenuScreen");
        Object targeted = showDummy ? null : GetTargetedObject.getTargetedObject(client, 0.0f);
        if (!showDummy && targeted == null)
            return;

        int padding = 6;
        int gap = 4;
        int iconSize = 16;
        int panelBorderColor = getPanelBorderColor();
        int panelInnerBorderColor = getPanelInnerBorderColor();

        // --- Dynamic width/height calculation ---
        int width, height;
        int baseLines = 3;
        int lineHeight = font.lineHeight + 1;
        int minHeight = 36;
        String name = null, heartsStr = null, typeStr = null;
        if (showDummy) {
            name = "Dummy Entity";
            int hearts = 5, maxHearts = 10;
            StringBuilder heartsBuilder = new StringBuilder();
            for (int i = 0; i < hearts; i++)
                heartsBuilder.append("\u2764");
            for (int i = hearts; i < maxHearts; i++)
                heartsBuilder.append("\u2661");
            heartsStr = heartsBuilder.toString();
            typeStr = "minecraft:zombie";
            int contentWidth = Math.max(Math.max(font.width(name), font.width(heartsStr)), font.width(typeStr));
            width = padding * 2 + contentWidth;
            height = padding * 2 + baseLines * lineHeight;
            height = Math.max(height, minHeight);
        } else if (targeted instanceof LivingEntity livingEntity) {
            name = livingEntity.getName().getString();
            float health = livingEntity.getHealth();
            float maxHealth = livingEntity.getMaxHealth();
            int hearts = (int) Math.ceil(health / 2.0f);
            int maxHearts = (int) Math.ceil(maxHealth / 2.0f);
            StringBuilder heartsBuilder = new StringBuilder();
            for (int i = 0; i < hearts; i++)
                heartsBuilder.append("\u2764");
            for (int i = hearts; i < maxHearts; i++)
                heartsBuilder.append("\u2661");
            heartsStr = heartsBuilder.toString();
            typeStr = livingEntity.getType().toShortString();
            int contentWidth = Math.max(Math.max(font.width(name), font.width(heartsStr)), font.width(typeStr));
            width = padding * 2 + contentWidth;
            height = padding * 2 + (baseLines + livingEntity.getActiveEffects().size()) * lineHeight;
            height = Math.max(height, minHeight);
        } else if (targeted instanceof BlockHitResult blockHit) {
            BlockState state = Objects.requireNonNull(client.level).getBlockState(blockHit.getBlockPos());
            Block block = state.getBlock();
            String title = block.getName().getString();
            String namespace = "minecraft";
            try {
                namespace = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getNamespace();
            } catch (Exception ignored) {
            }
            int nameWidth = font.width(title);
            int namespaceWidth = font.width(namespace);
            int contentWidth = iconSize + gap + Math.max(nameWidth, namespaceWidth);
            width = padding * 2 + contentWidth;
            height = padding * 2 + 2 * lineHeight;
            height = Math.max(height, minHeight);
        } else {
            width = 160;
            height = minHeight;
        }

        int left = x - width / 2;
        int top = y;
        context.fill(left, top, left + width, top + height, HEADER_COLOR);
        context.fill(left, top, left + width, top + 1, panelBorderColor);
        context.fill(left, top + height - 1, left + width, top + height, panelBorderColor);
        context.fill(left, top, left + 1, top + height, panelBorderColor);
        context.fill(left + width - 1, top, left + width, top + height, panelBorderColor);
        context.fill(left + 1, top + 2, left + width - 1, top + 3, panelInnerBorderColor);

        int drawX = left + padding;
        int textY = top + padding;

        if (showDummy) {
            context.drawString(font, Component.literal(name), drawX, textY, TITLE_COLOR, false);
            int heartsY = textY + font.lineHeight + 2;
            context.drawString(font, Component.literal(heartsStr), drawX, heartsY, Color.RED.getRGB(), false);
            context.drawString(font, Component.literal(typeStr), drawX, top + height - font.lineHeight - 4,
                    Color.BLUE.getRGB(), false);
        } else if (targeted instanceof LivingEntity livingEntity) {
            context.drawString(font, Component.literal(name), drawX, textY, TITLE_COLOR, false);
            int heartsY = textY + font.lineHeight + 2;
            context.drawString(font, Component.literal(heartsStr), drawX, heartsY, Color.RED.getRGB(), false);
            int effectY = heartsY + font.lineHeight + 2;
            for (var effect : livingEntity.getActiveEffects()) {
                var mobEffect = effect.getEffect();
                String effectName = mobEffect.value().getDisplayName().getString();
                int amplifier = effect.getAmplifier();
                int duration = effect.getDuration() / 20;
                String durationStr = String.format("%02d:%02d", duration / 60, duration % 60);
                int color = mobEffect.value().getCategory().getTooltipFormatting().getColor();
                String ampStr = amplifier > 0 ? " " + (amplifier + 1) : "";
                String effectText = effectName + ampStr + " (" + durationStr + ")";
                context.drawString(font, Component.literal(effectText), drawX, effectY, color, false);
                effectY += font.lineHeight + 2;
            }
            context.drawString(font, Component.literal(typeStr), drawX, top + height - font.lineHeight - 4,
                    Color.BLUE.getRGB(), false);
        } else if (targeted instanceof BlockHitResult blockHit) {
            BlockState state = Objects.requireNonNull(client.level).getBlockState(blockHit.getBlockPos());
            Block block = state.getBlock();
            // Block texture as ItemStack
            ItemStack stack = new ItemStack(block);
            int iconY = textY - 2;
            context.renderItem(stack, drawX, iconY);

            // Block name (large, white)
            String title = block.getName().getString();
            int nameX = drawX + iconSize + gap;
            context.drawString(font, Component.literal(title), nameX, textY, TITLE_COLOR, false);

            // Mod/source (namespace, blue, italic)
            String namespace = "minecraft";
            try {
                // Try to get the namespace (modid) for the block
                namespace = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getNamespace();
            } catch (Exception ignored) {
            }
            context.drawString(font, Component.literal(namespace).withStyle(style -> style.withItalic(true)), nameX,
                    textY + 16, Color.BLUE.getRGB(), false);
        }
    }

    public static void dragTo(double mouseX, double mouseY) {
        if (!dragging)
            return;
        Minecraft client = Minecraft.getInstance();
        int windowWidth = client.getWindow().getGuiScaledWidth();
        int centerX = windowWidth / 2;
        int threshold = 16; // Snap threshold in pixels
        int newX = (int) Math.round(mouseX) - dragOffsetX;
        // Snap to center if close enough
        if (Math.abs(newX - centerX) <= threshold) {
            x = centerX;
        } else {
            x = newX;
        }
        y = (int) Math.round(mouseY) - dragOffsetY;
        clampToWindow(client);
    }

    public static void endDragging() {
        dragging = false;
    }

    public static boolean isDragging() {
        return dragging;
    }

    private static void clampToWindow(Minecraft client) {
        InfoBoxLayout layout = layout(client);
        int width = layout.width();
        int height = layout.height();
        int windowWidth = client.getWindow().getGuiScaledWidth();
        int windowHeight = client.getWindow().getGuiScaledHeight();
        int minX = width / 2;
        int maxX = windowWidth - width / 2;
        int minY = 0;
        int maxY = windowHeight - height;
        x = Math.max(minX, Math.min(maxX, x));
        y = Math.max(minY, Math.min(maxY, y));
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
}
