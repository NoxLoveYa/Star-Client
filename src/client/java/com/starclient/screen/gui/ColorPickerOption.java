package com.starclient.screen.gui;

import org.jspecify.annotations.NonNull;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public record ColorPickerOption(@NonNull String label, DoubleSupplier getter, DoubleConsumer setter)
        implements MenuControl {
}
