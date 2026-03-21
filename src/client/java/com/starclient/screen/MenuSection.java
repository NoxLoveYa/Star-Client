package com.starclient.screen;

import org.jspecify.annotations.NonNull;

import java.util.List;

public record MenuSection(
        @NonNull String title,
        @NonNull String subTab,
        int column,
        List<@NonNull MenuControl> controls) {
}
