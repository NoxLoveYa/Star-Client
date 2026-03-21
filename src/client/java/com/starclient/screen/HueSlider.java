package com.starclient.screen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

final class HueSlider extends AbstractSliderButton {
    private static final int TRACK_LEFT_PADDING = 6;
    private static final int TRACK_RIGHT_PADDING = 22;
    private final DoubleConsumer setter;
    private final DoubleSupplier getter;

    HueSlider(int x, int y, int width, int height, DoubleSupplier getter, DoubleConsumer setter) {
        super(x, y, width, height, Component.empty(), 0.0);
        this.setter = setter;
        this.getter = getter;
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
        this.setValueFromTrack(mouseButtonEvent.x());
    }

    @Override
    protected void onDrag(@NonNull MouseButtonEvent mouseButtonEvent, double dragX, double dragY) {
        this.setValueFromTrack(mouseButtonEvent.x());
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
}
