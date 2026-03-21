package com.starclient.screen;

import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record ToggleOption(@NonNull String label, Supplier<Boolean> getter, Consumer<Boolean> setter)
        implements MenuControl {
}
