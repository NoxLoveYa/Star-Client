package com.starclient.screen.gui;

import org.jspecify.annotations.NonNull;

public record ActionOption(@NonNull String label, Runnable action) implements MenuControl {
}
