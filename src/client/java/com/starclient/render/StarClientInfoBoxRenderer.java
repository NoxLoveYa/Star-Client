package com.starclient.render;

import com.starclient.StarClientOptions;
import com.starclient.helper.GetTargetedObject;
import com.starclient.StarNameTagColorRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import java.awt.Color;
import java.util.Objects;

public final class StarClientInfoBoxRenderer {
    // Layout record for hit detection, now using center-based logic
    private record InfoBoxLayout(int width, int height) {

    }

    // Center coordinates for the info box
    private static int x = 120; // Center X
    private static int y = 80; // Center Y

    // Returns the layout (width/height) for the current info box, for hit detection
    // and clamping
    private static InfoBoxLayout layout(Minecraft client) {
        var font = client.font;
        // Get targeted object for title/info width
        Object targeted = GetTargetedObject.getTargetedObject(client, 0.0f);
        String title = "Info Box";
        String infoLine = "";
        if (targeted instanceof Entity entity) {
            title = entity.getName().getString();
            infoLine = entity.getType().toShortString();
            if (entity instanceof ItemEntity itemEntity) {
                infoLine = itemEntity.getItem().getHoverName().getString() + " x" + itemEntity.getItem().getCount();
            }
        } else if (targeted instanceof BlockHitResult blockHit) {
            BlockState state = Objects.requireNonNull(client.level).getBlockState(blockHit.getBlockPos());
            Block block = state.getBlock();
            title = block.getName().getString();
            infoLine = block.toString();
        }
        int padding = 6;
        int gap = 4;
        int iconSize = 16;
        int titleWidth = font.width(title);
        int infoWidth = font.width(infoLine);
        int contentWidth = iconSize + gap + Math.max(titleWidth, infoWidth);
        int width = padding * 2 + contentWidth;
        int height = 36;
        return new InfoBoxLayout(width, height);
    }

    // Begin dragging logic, now using center as anchor
    public static boolean beginDragging(double mouseX, double mouseY) {
        Minecraft client = Minecraft.getInstance();
        InfoBoxLayout layout = layout(client);
        int width = layout.width();
        int height = layout.height();
        int left = x - width / 2;
        int top = y - height / 2;
        if (!(mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height)) {
            return false;
        }
        dragging = true;
        dragOffsetX = (int) Math.round(mouseX) - x;
        dragOffsetY = (int) Math.round(mouseY) - y;
        return true;
    }

    private static final int PANEL_COLOR = new Color(10, 10, 12, 232).getRGB();
    private static final int HEADER_COLOR = new Color(16, 16, 22, 245).getRGB();
    private static final int TITLE_COLOR = new Color(240, 238, 255, 255).getRGB();
    private static final int TEXT_COLOR = new Color(232, 228, 246, 255).getRGB();
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

        // Get targeted object (returns Entity or BlockHitResult)
        Object targeted = GetTargetedObject.getTargetedObject(client, 0.0f);
        if (targeted == null)
            return;

        String title = "Info Box";
        String infoLine = "";
        Identifier texture = null;
        StarNameTagColorRegistry.UvRect uvRect = null;
        ItemStack itemStack = null;

        if (targeted instanceof LivingEntity livingEntity) {
            title = livingEntity.getName().getString();
            infoLine = livingEntity.getType().toShortString();
            // texture = EntityTextureHelper.resolveTexture(livingEntity, );
        } else if (targeted instanceof BlockHitResult blockHit) {
            BlockState state = Objects.requireNonNull(client.level).getBlockState(blockHit.getBlockPos());
            Block block = state.getBlock();
            title = block.getName().getString();
            infoLine = block.toString();
            // Optionally: try to get block texture/icon here if desired
        }

        int height = 36;
        int padding = 6;
        int gap = 4;
        int iconSize = 16;
        int titleWidth = font.width(title);
        int infoWidth = font.width(infoLine);
        int contentWidth = iconSize + gap + Math.max(titleWidth, infoWidth);
        int width = padding * 2 + contentWidth;

        int panelBorderColor = getPanelBorderColor();
        int panelInnerBorderColor = getPanelInnerBorderColor();

        int left = x - width / 2;
        int top = y - height / 2;
        context.fill(left, top, left + width, top + height, PANEL_COLOR);
        context.fill(left, top, left + width, top + (height / 2), HEADER_COLOR);
        context.fill(left, top, left + width, top + 1, panelBorderColor);
        context.fill(left, top + height - 1, left + width, top + height, panelBorderColor);
        context.fill(left, top, left + 1, top + height, panelBorderColor);
        context.fill(left + width - 1, top, left + width, top + height, panelBorderColor);
        context.fill(left + 1, top + 2, left + width - 1, top + 3, panelInnerBorderColor);

        int drawX = left + padding;
        int iconY = top + (height - iconSize) / 2;


        // if (texture != null && uvRect != null) {
        //     // context.blit(texture, drawX, iconY, uvRect.u0(), uvRect.v0(), iconSize, iconSize);
        // } else if (itemStack != null) {
        //     context.renderFakeItem(itemStack, drawX, iconY);
        // } else {
        // }

        drawX += iconSize + gap;
        int textY = top + 10;
        context.drawString(font, Component.literal(title), drawX, textY, TITLE_COLOR, false);
        context.drawString(font, Component.literal(infoLine), drawX, textY + 12, TEXT_COLOR, false);
    }

    public static void dragTo(double mouseX, double mouseY) {
        if (!dragging)
            return;
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
        InfoBoxLayout layout = layout(client);
        int width = layout.width();
        int height = layout.height();
        int minX = width / 2;
        int minY = height / 2;
        int maxX = client.getWindow().getGuiScaledWidth() - width / 2;
        int maxY = client.getWindow().getGuiScaledHeight() - height / 2;
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
