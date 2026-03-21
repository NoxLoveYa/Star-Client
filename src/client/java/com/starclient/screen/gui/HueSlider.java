package com.starclient.screen.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

final class HueSlider extends AbstractSliderButton {
    private static final int TRACK_LEFT_PADDING = 6;
    private static final int TRACK_RIGHT_PADDING = 22;
    private static final double WHEEL_HUE_STEP = 0.015;
    private static final double WHEEL_SPEED_STEP = 0.03;
    private static final double WHEEL_ACCELERATION_EXPONENT = 1.35;
    private static final double MIN_RAINBOW_SPEED = 0.01;
    private static final double MAX_RAINBOW_SPEED = 3.0;
    private final DoubleConsumer setter;
    private final DoubleSupplier getter;
    private final Consumer<Boolean> rainbowModeSetter;
    private final DoubleSupplier rainbowSpeedGetter;
    private final DoubleConsumer rainbowSpeedSetter;
    private boolean rainbowMode;

    HueSlider(int x, int y, int width, int height, DoubleSupplier getter, DoubleConsumer setter,
            BooleanSupplier rainbowModeGetter, Consumer<Boolean> rainbowModeSetter,
            DoubleSupplier rainbowSpeedGetter, DoubleConsumer rainbowSpeedSetter) {
        super(x, y, width, height, Component.empty(), 0.0);
        this.setter = setter;
        this.getter = getter;
        this.rainbowModeSetter = rainbowModeSetter;
        this.rainbowSpeedGetter = rainbowSpeedGetter;
        this.rainbowSpeedSetter = rainbowSpeedSetter;
        this.rainbowMode = rainbowModeGetter.getAsBoolean();
        syncFromOption();
    }

    private void syncFromOption() {
        this.value = clamp(getter.getAsDouble());
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.empty());
    }

    @Override
    protected void applyValue() {
        setter.accept(clamp(this.value));
        syncFromOption();
    }

    @Override
    public void onClick(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        if (mouseButtonEvent.button() == 1) {
            setRainbowMode(!rainbowMode);
            return;
        }

        if (mouseButtonEvent.button() == 0) {
            this.setValueFromTrack(mouseButtonEvent.x());
        }
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        if (!this.active || !this.visible || !this.isMouseOver(mouseButtonEvent.x(), mouseButtonEvent.y())) {
            return false;
        }

        if (mouseButtonEvent.button() == 1) {
            setRainbowMode(!rainbowMode);
            return true;
        }

        return super.mouseClicked(mouseButtonEvent, doubleClick);
    }

    @Override
    protected void onDrag(@NonNull MouseButtonEvent mouseButtonEvent, double dragX, double dragY) {
        if (mouseButtonEvent.button() != 0) {
            return;
        }
        this.setValueFromTrack(mouseButtonEvent.x());
    }

    @Override
    public void renderWidget(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        syncFromOption();
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.active || !this.visible || !this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        double primaryAmount = verticalAmount != 0.0 ? verticalAmount : horizontalAmount;
        if (primaryAmount == 0.0) {
            return false;
        }

        double scrollUnits = Math.abs(verticalAmount) + Math.abs(horizontalAmount);
        if (scrollUnits <= 0.0) {
            return false;
        }

        double amount = Math.signum(primaryAmount) * scrollUnits;
        double acceleratedAmount = applyScrollAcceleration(amount);

        if (rainbowMode) {
            double speed = clampRainbowSpeed(
                    rainbowSpeedGetter.getAsDouble() + (acceleratedAmount * WHEEL_SPEED_STEP));
            rainbowSpeedSetter.accept(speed);
        } else {
            double nextValue = wrapHue(getter.getAsDouble() + (acceleratedAmount * WHEEL_HUE_STEP));
            setter.accept(nextValue);
            this.value = clamp(nextValue);
        }

        return true;
    }

    private void setRainbowMode(boolean enabled) {
        this.rainbowMode = enabled;
        this.rainbowModeSetter.accept(enabled);
    }

    private void setValueFromTrack(double mouseX) {
        int trackStart = this.getX() + TRACK_LEFT_PADDING;
        int trackEnd = this.getX() + this.getWidth() - TRACK_RIGHT_PADDING;
        int trackWidth = Math.max(2, trackEnd - trackStart);
        double normalized = (mouseX - trackStart) / (trackWidth - 1.0);
        this.setValue(clamp(normalized));
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static double clampRainbowSpeed(double speed) {
        return Math.max(MIN_RAINBOW_SPEED, Math.min(MAX_RAINBOW_SPEED, speed));
    }

    private static double wrapHue(double hue) {
        double wrapped = hue % 1.0;
        return wrapped < 0.0 ? wrapped + 1.0 : wrapped;
    }

    private static double applyScrollAcceleration(double amount) {
        double magnitude = Math.abs(amount);
        if (magnitude <= 1.0) {
            return amount;
        }
        return Math.copySign(Math.pow(magnitude, WHEEL_ACCELERATION_EXPONENT), amount);
    }
}
