package com.starclient.screen.gui;

import org.jspecify.annotations.NonNull;

import java.util.List;

public record MenuTab(@NonNull String label, List<@NonNull MenuSection> sections) {
}
