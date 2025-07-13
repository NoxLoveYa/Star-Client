package com.starclient.gui.widgets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ButtonWidget extends PressableWidget {
    public static final int DEFAULT_WIDTH_SMALL = 120;
    public static final int DEFAULT_WIDTH = 150;
    public static final int field_49479 = 200;
    public static final int DEFAULT_HEIGHT = 20;
    public static final int field_46856 = 8;
    protected static final com.starclient.gui.widgets.ButtonWidget.NarrationSupplier DEFAULT_NARRATION_SUPPLIER = textSupplier -> (MutableText)textSupplier.get();
    protected final com.starclient.gui.widgets.ButtonWidget.PressAction onPress;
    protected final  com.starclient.gui.widgets.ButtonWidget.NarrationSupplier narrationSupplier;

    public static com.starclient.gui.widgets.ButtonWidget.Builder builder(Text message, com.starclient.gui.widgets.ButtonWidget.PressAction onPress) {
        return new com.starclient.gui.widgets.ButtonWidget.Builder(message, onPress);
    }

    protected ButtonWidget(int x, int y, int width, int height, Text message, com.starclient.gui.widgets.ButtonWidget.PressAction onPress, com.starclient.gui.widgets.ButtonWidget.NarrationSupplier narrationSupplier) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.narrationSupplier = narrationSupplier;
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

    @Environment(EnvType.CLIENT)
    public static class Builder {
        private final Text message;
        private final com.starclient.gui.widgets.ButtonWidget.PressAction onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private com.starclient.gui.widgets.ButtonWidget.NarrationSupplier narrationSupplier = com.starclient.gui.widgets.ButtonWidget.DEFAULT_NARRATION_SUPPLIER;

        public Builder(Text message, com.starclient.gui.widgets.ButtonWidget.PressAction onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public com.starclient.gui.widgets.ButtonWidget.Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public com.starclient.gui.widgets.ButtonWidget.Builder width(int width) {
            this.width = width;
            return this;
        }

        public com.starclient.gui.widgets.ButtonWidget.Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public com.starclient.gui.widgets.ButtonWidget.Builder dimensions(int x, int y, int width, int height) {
            return this.position(x, y).size(width, height);
        }

        public com.starclient.gui.widgets.ButtonWidget.Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public com.starclient.gui.widgets.ButtonWidget.Builder narrationSupplier(com.starclient.gui.widgets.ButtonWidget.NarrationSupplier narrationSupplier) {
            this.narrationSupplier = narrationSupplier;
            return this;
        }

        public com.starclient.gui.widgets.ButtonWidget build() {
            com.starclient.gui.widgets.ButtonWidget buttonWidget = new com.starclient.gui.widgets.ButtonWidget(this.x, this.y, this.width, this.height, this.message, this.onPress, this.narrationSupplier);
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
        void onPress(com.starclient.gui.widgets.ButtonWidget button);
    }
}
