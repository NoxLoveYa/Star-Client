package com.starclient.screen.gui;

import org.jspecify.annotations.NonNull;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

public record SliderOption(
        @NonNull String label,
        double min,
        double max,
        DoubleSupplier getter,
        DoubleConsumer setter,
        Function<Double, @NonNull String> formatter) implements MenuControl {
}
