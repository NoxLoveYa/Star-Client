package com.starclient.screen.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

final class DynamicSlider extends AbstractSliderButton {
    private final String label;
    private final double min;
    private final double max;
    private final DoubleConsumer setter;
    private final DoubleSupplier getter;
    private final Function<Double, @NonNull String> valueFormatter;

    DynamicSlider(int x, int y, int width, int height, String label, double min, double max,
            DoubleSupplier getter, DoubleConsumer setter, Function<Double, @NonNull String> valueFormatter) {
        super(x, y, width, height, Component.empty(), 0.0);
        this.label = label;
        this.min = min;
        this.max = max;
        this.setter = setter;
        this.getter = getter;
        this.valueFormatter = valueFormatter;
        syncFromOption();
    }

    private void syncFromOption() {
        double current = clamp(getter.getAsDouble(), min, max);
        this.value = (current - min) / (max - min);
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        double current = min + (max - min) * this.value;
        this.setMessage(Component.literal(label + ": " + valueFormatter.apply(current)));
    }

    @Override
    protected void applyValue() {
        double current = min + (max - min) * this.value;
        setter.accept(clamp(current, min, max));
        syncFromOption();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
