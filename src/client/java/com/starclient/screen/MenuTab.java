package com.starclient.screen;

import org.jspecify.annotations.NonNull;

import java.util.List;

public record MenuTab(@NonNull String label, List<@NonNull MenuSection> sections) {
}
