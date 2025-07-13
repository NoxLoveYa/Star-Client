package com.starclient.gui.widgets;

import com.google.common.collect.Lists;
import com.starclient.StarClient;
import com.starclient.utils.CheatOptions;
import com.starclient.utils.ColorUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ButtonGroupWidget extends PressableWidget {
    public static final int DEFAULT_WIDTH_SMALL = 120;
    public static final int DEFAULT_WIDTH = 150;
    public static final int field_49479 = 200;
    public static final int DEFAULT_HEIGHT = 20;
    public static final int field_46856 = 8;
    protected static final com.starclient.gui.widgets.ButtonGroupWidget.NarrationSupplier DEFAULT_NARRATION_SUPPLIER = textSupplier -> (MutableText)textSupplier.get();
    protected final com.starclient.gui.widgets.ButtonGroupWidget.PressAction onPress;
    protected final  com.starclient.gui.widgets.ButtonGroupWidget.NarrationSupplier narrationSupplier;
    protected List<ButtonWidget> buttons = Lists.<ButtonWidget>newArrayList();
    private int lastY = 0;
    private boolean opened = true;

    public static com.starclient.gui.widgets.ButtonGroupWidget.Builder builder(Text message) {
        return new com.starclient.gui.widgets.ButtonGroupWidget.Builder(message);
    }

    protected ButtonGroupWidget(int x, int y, int width, int height, Text message, com.starclient.gui.widgets.ButtonGroupWidget.NarrationSupplier narrationSupplier) {
        super(x, y, width, height, message);
        this.onPress = (btn) -> {
            btn.opened = !btn.opened;
            for (ButtonWidget button : btn.buttons) {
                button.visible = btn.opened;
            }
        };
        this.narrationSupplier = narrationSupplier;
        this.backgroundColor = CheatOptions.MainColor;
        this.backgroundAlpha = 0.45F;
        this.lastY = y + height;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected MutableText getNarrationMessage() {
        return this.narrationSupplier.createNarrationMessage(() -> super.getNarrationMessage());
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        context.drawText(minecraftClient.textRenderer, this.opened? "[-]" : "[+]", getX() + 2, getY() + (int)(((float)this.height) / 3.5), ColorUtils.white.getRGB(), true);
    }

    public void appendButtonWidget(ButtonWidget button) {
        buttons.add(button);

        //Update tab width to match the widest button
        if (this.width < button.getWidth()) {
            this.width = button.getWidth();
        }

        //Update button width to match the widest button
        button.setWidth(this.width);

        //Update position of the button
        button.setX(this.getX());
        button.setY(lastY);

        //Update render height
        lastY += button.getHeight();
    }

    @Environment(EnvType.CLIENT)
    public static class Builder {
        private final Text message;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private com.starclient.gui.widgets.ButtonGroupWidget.NarrationSupplier narrationSupplier = com.starclient.gui.widgets.ButtonGroupWidget.DEFAULT_NARRATION_SUPPLIER;

        public Builder(Text message) {
            this.message = message;
        }

        public com.starclient.gui.widgets.ButtonGroupWidget.Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public com.starclient.gui.widgets.ButtonGroupWidget.Builder width(int width) {
            this.width = width;
            return this;
        }

        public com.starclient.gui.widgets.ButtonGroupWidget.Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public com.starclient.gui.widgets.ButtonGroupWidget.Builder dimensions(int x, int y, int width, int height) {
            return this.position(x, y).size(width, height);
        }

        public com.starclient.gui.widgets.ButtonGroupWidget.Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public com.starclient.gui.widgets.ButtonGroupWidget.Builder narrationSupplier(com.starclient.gui.widgets.ButtonGroupWidget.NarrationSupplier narrationSupplier) {
            this.narrationSupplier = narrationSupplier;
            return this;
        }

        public com.starclient.gui.widgets.ButtonGroupWidget build() {
            com.starclient.gui.widgets.ButtonGroupWidget buttonWidget = new com.starclient.gui.widgets.ButtonGroupWidget(this.x, this.y, this.width, this.height, this.message, this.narrationSupplier);
            buttonWidget.setTooltip(this.tooltip);
            return buttonWidget;
        }
    }

    @Environment(EnvType.CLIENT)
    public interface NarrationSupplier {
        MutableText createNarrationMessage(Supplier<MutableText> textSupplier);
    }

    @Environment(EnvType.CLIENT)
    public interface PressAction {
        void onPress(com.starclient.gui.widgets.ButtonGroupWidget button);
    }
}
