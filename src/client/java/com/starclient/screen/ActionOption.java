package com.starclient.screen;

import org.jspecify.annotations.NonNull;

public record ActionOption(@NonNull String label, Runnable action) implements MenuControl {
}
