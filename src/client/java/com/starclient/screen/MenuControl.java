package com.starclient.screen;

import org.jspecify.annotations.NonNull;

public sealed interface MenuControl permits ToggleOption, SliderOption, ActionOption, ColorPickerOption {
    @NonNull
    String label();
}
