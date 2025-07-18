package com.starclient.gui.widgets;

import com.starclient.utils.CheatOptions;
import com.starclient.utils.ColorUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public abstract class PressableWidget extends ClickableWidget {
    protected static final int field_43050 = 2;
    protected float backgroundAlpha = 0.6F;
    protected ColorUtils backgroundColor = new ColorUtils(20, 20, 20);
    protected ColorUtils enabledBackgroundColor = new ColorUtils(20, 20, 20);
    protected ColorUtils enabledTextColor = CheatOptions.MainColor;
    protected boolean dragging = false;
    protected boolean enabled = false;

    public PressableWidget(int i, int j, int k, int l, Text text) {
        super(i, j, k, l, text);
    }

    public abstract void onPress();

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible)
            return;
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        ColorUtils currentBackgroundColor = this.enabled ? enabledBackgroundColor : backgroundColor;
        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), new ColorUtils(currentBackgroundColor.getRed(), currentBackgroundColor.getGreen(), currentBackgroundColor.getBlue(), MathHelper.ceil(this.alpha * this.backgroundAlpha * 255.0F)).getRGB());
        this.drawMessage(context, minecraftClient.textRenderer, this.enabled ? new ColorUtils(enabledTextColor.getRed(), enabledTextColor.getGreen(), enabledTextColor.getBlue(), MathHelper.ceil(this.alpha * 255.0F)).getRGB() : new ColorUtils(255, 255, 255, MathHelper.ceil(this.alpha * 255.0F)).getRGB());
    }

    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
        this.drawScrollableText(context, textRenderer, 2, color);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!visible)
            return;
        this.onPress();
        this.enabled = !this.enabled;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.active || !this.visible) {
            return false;
        } else if (KeyCodes.isToggle(keyCode)) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            this.onPress();
            return true;
        } else {
            return false;
        }
    }
}