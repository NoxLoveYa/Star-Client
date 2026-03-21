package com.starclient.screen.gui;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public record ColorPickerOption(
                @NonNull String label,
                DoubleSupplier getter,
                DoubleConsumer setter,
                @Nullable BooleanSupplier rainbowGetter,
                @Nullable Consumer<Boolean> rainbowSetter,
                @Nullable DoubleSupplier rainbowSpeedGetter,
                @Nullable DoubleConsumer rainbowSpeedSetter)
                implements MenuControl {
        public ColorPickerOption(@NonNull String label, DoubleSupplier getter, DoubleConsumer setter) {
                this(label, getter, setter, null, null, null, null);
        }

        public ColorPickerOption(@NonNull String label, DoubleSupplier getter, DoubleConsumer setter,
                        @Nullable BooleanSupplier rainbowGetter, @Nullable Consumer<Boolean> rainbowSetter) {
                this(label, getter, setter, rainbowGetter, rainbowSetter, null, null);
        }
}
